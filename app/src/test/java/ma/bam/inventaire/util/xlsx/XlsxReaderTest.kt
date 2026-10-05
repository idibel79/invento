package ma.bam.inventaire.util.xlsx

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class XlsxReaderTest {

    /** Construit un .xlsx minimal (pas de workbook.xml : XlsxReader retombe sur sheet1.xml). */
    private fun buildMinimalWorkbook(sharedStringsXml: String, sheetXml: String): ByteArray {
        val outputStream = ByteArrayOutputStream()
        ZipOutputStream(outputStream).use { zip ->
            zip.putNextEntry(ZipEntry("xl/sharedStrings.xml"))
            zip.write(sharedStringsXml.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
            zip.write(sheetXml.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
        return outputStream.toByteArray()
    }

    @Test
    fun `repare le texte UTF-8 double-encode des exports type Sobrus`() {
        // "CatÃ©gorie" est ce que donne "Catégorie" une fois ses octets UTF-8 mal décodés en
        // Latin-1 puis réencodés en UTF-8 — exactement le motif observé dans le fichier réel.
        val sharedStrings = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="1" uniqueCount="1">
                <si><t>CatÃ©gorie</t></si>
            </sst>
        """.trimIndent()
        val sheet = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                <sheetData>
                    <row r="1"><c r="A1" t="s"><v>0</v></c></row>
                </sheetData>
            </worksheet>
        """.trimIndent()

        val rows = XlsxReader.readFirstSheetRows(buildMinimalWorkbook(sharedStrings, sheet))

        assertEquals("Catégorie", rows[0][0])
    }

    @Test
    fun `laisse un texte deja correct inchange`() {
        val sharedStrings = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="1" uniqueCount="1">
                <si><t>Catégorie</t></si>
            </sst>
        """.trimIndent()
        val sheet = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                <sheetData>
                    <row r="1"><c r="A1" t="s"><v>0</v></c></row>
                </sheetData>
            </worksheet>
        """.trimIndent()

        val rows = XlsxReader.readFirstSheetRows(buildMinimalWorkbook(sharedStrings, sheet))

        assertEquals("Catégorie", rows[0][0])
    }
}
