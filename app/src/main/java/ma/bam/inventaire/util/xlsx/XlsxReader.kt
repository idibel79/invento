package ma.bam.inventaire.util.xlsx

import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.util.zip.ZipInputStream
import javax.xml.parsers.SAXParserFactory

/**
 * Lecteur .xlsx minimal basé uniquement sur `java.util.zip` et SAX (`javax.xml.parsers`),
 * deux API standard disponibles à la fois sur Android et dans les tests JVM — volontairement
 * sans dépendance tierce (voir [XlsxWriter] pour le contexte : les lecteurs OOXML du marché
 * tirent presque tous une dépendance StAX absente d'Android).
 *
 * Ne gère qu'un sous-ensemble suffisant pour nos besoins : une feuille (la première du classeur),
 * cellules texte (inlineStr / shared strings / str) ou numériques, pas de styles ni de formules.
 */
object XlsxReader {

    /** Lit la première feuille du classeur et retourne ses lignes, chaque cellule en texte brut. */
    fun readFirstSheetRows(bytes: ByteArray): List<List<String>> {
        val entries = unzip(bytes)

        val sharedStrings = entries["xl/sharedStrings.xml"]?.let { parseSharedStrings(it) } ?: emptyList()
        val sheetEntryName = resolveFirstSheetEntry(entries)
        val sheetBytes = entries[sheetEntryName] ?: return emptyList()

        return parseSheet(sheetBytes, sharedStrings)
    }

    private fun unzip(bytes: ByteArray): Map<String, ByteArray> {
        val result = mutableMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    result[entry.name] = zip.readBytes()
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return result
    }

    /**
     * Détermine le fichier XML de la première feuille en suivant workbook.xml -> workbook.xml.rels.
     * Retombe sur des conventions de nommage usuelles si la structure est inattendue.
     */
    private fun resolveFirstSheetEntry(entries: Map<String, ByteArray>): String {
        val workbookXml = entries["xl/workbook.xml"]
        val relsXml = entries["xl/_rels/workbook.xml.rels"]
        if (workbookXml != null && relsXml != null) {
            val firstSheetRid = parseFirstSheetRelationshipId(workbookXml)
            val target = firstSheetRid?.let { parseRelationshipTarget(relsXml, it) }
            if (target != null) {
                val normalized = if (target.startsWith("/")) target.removePrefix("/") else "xl/$target"
                if (entries.containsKey(normalized)) return normalized
            }
        }
        return entries.keys
            .filter { it.startsWith("xl/worksheets/") && it.endsWith(".xml") }
            .sorted()
            .firstOrNull() ?: "xl/worksheets/sheet1.xml"
    }

    private fun parseFirstSheetRelationshipId(xml: ByteArray): String? {
        var result: String? = null
        parseXml(xml, object : DefaultHandler() {
            override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {
                if (result == null && qName.substringAfterLast(':') == "sheet") {
                    result = attributes.getValue("r:id") ?: attributes.getValue("id")
                }
            }
        })
        return result
    }

    private fun parseRelationshipTarget(xml: ByteArray, relationshipId: String): String? {
        var result: String? = null
        parseXml(xml, object : DefaultHandler() {
            override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {
                if (qName.substringAfterLast(':') == "Relationship" && attributes.getValue("Id") == relationshipId) {
                    result = attributes.getValue("Target")
                }
            }
        })
        return result
    }

    private fun parseSharedStrings(xml: ByteArray): List<String> {
        val strings = mutableListOf<String>()
        val current = StringBuilder()
        var inSi = false
        var inText = false
        parseXml(xml, object : DefaultHandler() {
            override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {
                when (qName.substringAfterLast(':')) {
                    "si" -> { inSi = true; current.setLength(0) }
                    "t" -> inText = true
                }
            }

            override fun characters(ch: CharArray, start: Int, length: Int) {
                if (inSi && inText) current.append(ch, start, length)
            }

            override fun endElement(uri: String?, localName: String?, qName: String) {
                when (qName.substringAfterLast(':')) {
                    "t" -> inText = false
                    "si" -> { strings.add(repairMojibake(current.toString())); inSi = false }
                }
            }
        })
        return strings
    }

    /**
     * Certains exports (ex: Sobrus) encodent leur texte en UTF-8 deux fois de suite : les octets
     * UTF-8 d'origine sont une première fois mal décodés en Latin-1/CP1252, puis le résultat
     * (déjà corrompu, ex. "Catégorie" -> "CatÃ©gorie") est ré-encodé en UTF-8 pour l'écriture du
     * fichier. On ne peut pas le corriger à la lecture du flux (le XML est un UTF-8 valide), donc
     * on inverse l'opération ici : on réinterprète les caractères du texte lu comme des octets
     * Latin-1, puis on les redécode en UTF-8. Si le résultat n'est pas un UTF-8 valide (texte déjà
     * correct, non corrompu), on garde le texte original.
     */
    private fun repairMojibake(text: String): String {
        if (text.isEmpty() || text.any { it.code > 0xFF }) return text
        return try {
            val bytes = text.toByteArray(Charsets.ISO_8859_1)
            val decoder = Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
            decoder.decode(ByteBuffer.wrap(bytes)).toString()
        } catch (e: Exception) {
            text
        }
    }

    private fun parseSheet(xml: ByteArray, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var currentRow = mutableListOf<String>()
        var cellColIndex = 0
        var cellType = "n"
        val valueBuffer = StringBuilder()
        val inlineTextBuffer = StringBuilder()
        var capturingValue = false
        var capturingInlineText = false

        parseXml(xml, object : DefaultHandler() {
            override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {
                when (qName.substringAfterLast(':')) {
                    "row" -> currentRow = mutableListOf()
                    "c" -> {
                        cellColIndex = columnIndexFromRef(attributes.getValue("r"))
                        cellType = attributes.getValue("t") ?: "n"
                        valueBuffer.setLength(0)
                        inlineTextBuffer.setLength(0)
                    }
                    "v" -> capturingValue = true
                    "t" -> capturingInlineText = true
                }
            }

            override fun characters(ch: CharArray, start: Int, length: Int) {
                if (capturingValue) valueBuffer.append(ch, start, length)
                if (capturingInlineText) inlineTextBuffer.append(ch, start, length)
            }

            override fun endElement(uri: String?, localName: String?, qName: String) {
                when (qName.substringAfterLast(':')) {
                    "v" -> capturingValue = false
                    "t" -> capturingInlineText = false
                    "c" -> {
                        val text = when (cellType) {
                            "s" -> valueBuffer.toString().trim().toIntOrNull()?.let { sharedStrings.getOrNull(it) } ?: ""
                            "inlineStr" -> repairMojibake(inlineTextBuffer.toString())
                            else -> valueBuffer.toString()
                        }
                        while (currentRow.size <= cellColIndex) currentRow.add("")
                        currentRow[cellColIndex] = text
                    }
                    "row" -> rows.add(currentRow)
                }
            }
        })
        return rows
    }

    /** "C5" -> 2 (index 0-based de la colonne C). */
    private fun columnIndexFromRef(cellRef: String?): Int {
        val letters = cellRef.orEmpty().takeWhile { it.isLetter() }
        var index = 0
        for (ch in letters) {
            index = index * 26 + (ch.uppercaseChar() - 'A' + 1)
        }
        return index - 1
    }

    private fun parseXml(bytes: ByteArray, handler: DefaultHandler) {
        val factory = SAXParserFactory.newInstance()
        val parser = factory.newSAXParser()
        parser.parse(InputSource(ByteArrayInputStream(bytes)), handler)
    }
}
