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
     * Lit le fichier Excel théorique (export Sobrus - Stocks). Chaque code_article (PID) doit
     * être unique : une ligne qui réutilise un PID déjà vu est ignorée et signalée en erreur
     * (la première occurrence est conservée).
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
                        "Ligne $rowNumber ignorée : PID '${article.codeArticle}' déjà présent " +
                            "(première occurrence conservée)."
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

        val codeArticle = value(ExcelColumnMapper.PID)
        if (codeArticle.isBlank()) return null

        return ImportedArticle(
            codeArticle = codeArticle,
            designation = value(ExcelColumnMapper.PRODUIT),
            categorie = value(ExcelColumnMapper.CATEGORIE),
            tva = value(ExcelColumnMapper.TVA),
            emplacement = value(ExcelColumnMapper.ZONE),
            quantiteTheorique = value(ExcelColumnMapper.STOCK).toDoubleOrNull() ?: 0.0,
            datePeremption = value(ExcelColumnMapper.DATE_PEREMPTION),
            stockMin = value(ExcelColumnMapper.STOCK_MIN).toDoubleOrNull() ?: 0.0,
            stockMax = value(ExcelColumnMapper.STOCK_MAX).toDoubleOrNull() ?: 0.0,
            codeBarre1 = value(ExcelColumnMapper.CODE_BARRE_1),
            codeBarre2 = value(ExcelColumnMapper.CODE_BARRE_2),
            prixUnitaire = value(ExcelColumnMapper.PPV).toDoubleOrNull() ?: 0.0,
            dateImport = System.currentTimeMillis()
        )
    }

    /** Exporte les articles d'une session (ou de plusieurs) vers un fichier .xlsx. */
    fun exportSessions(
        outputStream: OutputStream,
        sessions: List<Pair<InventorySessionEntity, List<StockArticleEntity>>>
    ) {
        val header = listOf(
            "numero_inventaire", "date_inventaire", "statut", "pid", "produit", "categorie",
            "tva", "zone", "stock", "date_de_peremption", "stock_min", "stock_max",
            "code_barre_1", "code_barre_2", "ppv", "quantite_reelle", "ecart", "ecart_valide"
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
                        article.designation,
                        article.categorie,
                        article.tva,
                        article.emplacement,
                        article.quantiteTheorique,
                        article.datePeremption,
                        article.stockMin,
                        article.stockMax,
                        article.codeBarre1,
                        article.codeBarre2,
                        article.prixUnitaire,
                        article.quantiteReelle,
                        article.ecart,
                        if (article.ecartValide) "OUI" else "NON"
                    )
                )
            }
        }

        XlsxWriter.write(outputStream, "Inventaire", rows)
    }
}
