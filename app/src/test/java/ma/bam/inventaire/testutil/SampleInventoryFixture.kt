package ma.bam.inventaire.testutil

import ma.bam.inventaire.util.xlsx.XlsxWriter
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Construit un classeur .xlsx représentatif d'un export Sobrus - Stocks (mêmes colonnes que
 * [ma.bam.inventaire.util.ExcelColumnMapper]). Sert à la fois de fixture pour les tests JVM et de
 * générateur de fichier d'exemple pour les tests manuels de l'écran d'import (voir la tâche
 * Gradle `generateSampleFixture`).
 *
 * Le jeu de données couvre volontairement :
 *  - un article avec un seul code-barres (ART-001)
 *  - un PID dupliqué sur deux lignes, qui doit être signalé en erreur à l'import (ART-002)
 *  - un article avec deux codes-barres (Code barre 1 et 2) (ART-003)
 *  - un article avec stock et prix manquants, donc à défaut 0 (ART-004)
 *  - une ligne vide (sans PID) qui doit être ignorée silencieusement à l'import
 */
object SampleInventoryFixture {

    private val HEADERS = listOf(
        "PID", "Produit", "Catégorie", "TVA", "PPV", "PPH", "Zone", "Stock",
        "Date de péremption", "Stock min", "Stock max", "Code barre 1", "Code barre 2"
    )

    private val ROWS: List<List<String>> = listOf(
        listOf(
            "ART-001", "Enveloppes A4 blanches", "Fournitures bureau", "TVA (20.00%)",
            "45.50", "37.90", "Dépôt A - Rayon 3", "120", "", "10", "200", "6111234500018", ""
        ),
        listOf(
            "ART-002", "Timbres ordinaires", "Philatélie", "Exonéré (0.00%)",
            "12.00", "10.00", "Dépôt A - Rayon 1", "300", "", "20", "500", "6111234500025", ""
        ),
        listOf(
            "ART-002", "Timbres ordinaires", "Philatélie", "Exonéré (0.00%)",
            "12.00", "10.00", "Dépôt A - Rayon 1", "300", "", "20", "500", "6111234500032", ""
        ),
        listOf(
            "ART-003", "Cartons colis M", "Emballage", "TVA (20.00%)",
            "8.75", "7.29", "Dépôt B - Rayon 2", "85", "", "5", "150", "6111234500049", "6111234500056"
        ),
        listOf(
            "ART-004", "Rouleau adhésif", "Emballage", "TVA (20.00%)",
            "", "", "Dépôt B - Rayon 2", "", "", "", "", "6111234500063", ""
        ),
        listOf("", "", "", "", "", "", "", "", "", "", "", "", "")
    )

    fun buildSampleWorkbookBytes(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val rows: List<List<Any?>> = listOf(HEADERS) + ROWS
        XlsxWriter.write(outputStream, "in", rows)
        return outputStream.toByteArray()
    }
}

/** Point d'entrée pour la tâche Gradle `generateSampleFixture` : écrit le fichier sur disque. */
fun main(args: Array<String>) {
    val outputPath = args.firstOrNull() ?: "samples/inventaire_exemple.xlsx"
    val file = File(outputPath)
    file.parentFile?.mkdirs()
    file.writeBytes(SampleInventoryFixture.buildSampleWorkbookBytes())
    println("Fixture écrite : ${file.absolutePath}")
}
