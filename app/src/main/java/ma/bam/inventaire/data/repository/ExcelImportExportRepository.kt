package ma.bam.inventaire.data.repository

import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.util.ExcelColumnMapper
import ma.bam.inventaire.util.xlsx.XlsxReader
import ma.bam.inventaire.util.xlsx.XlsxWriter
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class ImportResult(
    val articles: List<ImportedArticle>,
    val erreurs: List<String>
)

@Singleton
class ExcelImportExportRepository @Inject constructor() {

    /**
     * Lit le fichier Excel théorique. Chaque code_article doit correspondre à un seul
     * code_barre : une ligne qui réutilise un code_article déjà vu est ignorée et signalée
     * en erreur (la première occurrence est conservée).
     */
    fun importTheoreticalStock(inputStream: InputStream): ImportResult {
        val erreurs = mutableListOf<String>()
        val byCodeArticle = LinkedHashMap<String, ImportedArticle>()

        val allRows = XlsxReader.readFirstSheetRows(inputStream.readBytes())
        if (allRows.isEmpty()) {
            return ImportResult(emptyList(), listOf("Le fichier ne contient aucune ligne."))
        }

        val headers = ExcelColumnMapper.mapHeaders(allRows.first())
        allRows.drop(1).forEachIndexed { index, cellTexts ->
            val rowNumber = index + 2 // +1 pour l'en-tête, +1 pour l'index 0-based
            try {
                val article = parseRow(cellTexts, headers) ?: return@forEachIndexed // ligne vide
                if (byCodeArticle.containsKey(article.codeArticle)) {
                    erreurs.add(
                        "Ligne $rowNumber ignorée : code_article '${article.codeArticle}' déjà présent " +
                            "(un seul code_barre autorisé par article, première occurrence conservée)."
                    )
                } else {
                    byCodeArticle[article.codeArticle] = article
                }
            } catch (e: Exception) {
                erreurs.add("Ligne $rowNumber ignorée : ${e.message}")
            }
        }

        return ImportResult(byCodeArticle.values.toList(), erreurs)
    }

    private fun parseRow(cells: List<String>, headers: Map<String, Int>): ImportedArticle? {
        fun value(key: String): String {
            val index = headers[key] ?: return ""
            return cells.getOrNull(index)?.trim().orEmpty()
        }

        val codeArticle = value(ExcelColumnMapper.CODE_ARTICLE)
        if (codeArticle.isBlank()) return null

        return ImportedArticle(
            codeArticle = codeArticle,
            codeBarre = value(ExcelColumnMapper.CODE_BARRE),
            reference = value(ExcelColumnMapper.REFERENCE),
            designation = value(ExcelColumnMapper.DESIGNATION),
            description = value(ExcelColumnMapper.DESCRIPTION),
            categorie = value(ExcelColumnMapper.CATEGORIE),
            emplacement = value(ExcelColumnMapper.EMPLACEMENT),
            quantiteTheorique = value(ExcelColumnMapper.QUANTITE_THEORIQUE).toDoubleOrNull() ?: 0.0,
            unite = value(ExcelColumnMapper.UNITE),
            prixUnitaire = value(ExcelColumnMapper.PRIX_UNITAIRE).toDoubleOrNull() ?: 0.0,
            dateImport = System.currentTimeMillis()
        )
    }

    /** Exporte les articles d'une session (ou de plusieurs) vers un fichier .xlsx. */
    fun exportSessions(
        outputStream: OutputStream,
        sessions: List<Pair<InventorySessionEntity, List<StockArticleEntity>>>
    ) {
        val header = listOf(
            "numero_inventaire", "date_inventaire", "statut", "code_article", "code_barre",
            "reference", "designation", "description", "categorie", "emplacement",
            "quantite_theorique", "quantite_reelle", "ecart", "ecart_valide", "unite", "prix_unitaire"
        )

        val rows = mutableListOf<List<Any?>>(header)
        sessions.forEach { (session, articles) ->
            articles.forEach { article ->
                rows.add(
                    listOf(
                        session.numero,
                        session.dateCreation.toString(),
                        session.statut.name,
                        article.codeArticle,
                        article.codeBarre,
                        article.reference,
                        article.designation,
                        article.description,
                        article.categorie,
                        article.emplacement,
                        article.quantiteTheorique,
                        article.quantiteReelle,
                        article.ecart,
                        if (article.ecartValide) "OUI" else "NON",
                        article.unite,
                        article.prixUnitaire
                    )
                )
            }
        }

        XlsxWriter.write(outputStream, "Inventaire", rows)
    }
}
