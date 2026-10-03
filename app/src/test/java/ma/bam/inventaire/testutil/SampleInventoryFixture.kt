package ma.bam.inventaire.testutil

import ma.bam.inventaire.util.xlsx.XlsxWriter
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Construit un classeur .xlsx représentatif du fichier que BAM exporterait de son système de
 * gestion pour alimenter un inventaire (mêmes colonnes que [ma.bam.inventaire.util.ExcelColumnMapper]).
 * Sert à la fois de fixture pour les tests JVM et de générateur de fichier d'exemple pour les tests
 * manuels de l'écran d'import (voir la tâche Gradle `generateSampleFixture`).
 *
 * Le jeu de données couvre volontairement :
 *  - un article simple à code-barres unique (ART-001)
 *  - un code_article dupliqué sur deux lignes avec deux codes-barres différents, qui doit être
 *    signalé en erreur à l'import (un seul code_barre autorisé par article) (ART-002)
 *  - un article simple à code-barres unique (ART-003)
 *  - un article avec quantité théorique et prix unitaire manquants, donc à défaut 0 (ART-004)
 *  - une ligne vide (sans code_article) qui doit être ignorée silencieusement à l'import
 */
object SampleInventoryFixture {

    private val HEADERS = listOf(
        "code_article", "code_barre", "reference", "designation", "description",
        "categorie", "emplacement", "quantite_theorique", "unite", "prix_unitaire", "date_import"
    )

    private val ROWS: List<List<String>> = listOf(
        listOf(
            "ART-001", "6111234500018", "REF-ENV-A4", "Enveloppes A4 blanches",
            "Boîte de 500 enveloppes A4 90g", "Fournitures bureau", "Dépôt A - Rayon 3",
            "120", "boite", "45.50", "2026-10-01"
        ),
        listOf(
            "ART-002", "6111234500025", "REF-TIMB-ORD", "Timbres ordinaires",
            "Carnet de 10 timbres tarif prioritaire", "Philatélie", "Dépôt A - Rayon 1",
            "300", "carnet", "12.00", "2026-10-01"
        ),
        listOf(
            "ART-002", "6111234500032", "REF-TIMB-ORD", "Timbres ordinaires",
            "Carnet de 10 timbres tarif prioritaire", "Philatélie", "Dépôt A - Rayon 1",
            "300", "carnet", "12.00", "2026-10-01"
        ),
        listOf(
            "ART-003", "6111234500049", "REF-CART-COL", "Cartons colis M",
            "Carton d'expédition format M", "Emballage", "Dépôt B - Rayon 2",
            "85", "unite", "8.75", "2026-10-01"
        ),
        listOf(
            "ART-004", "6111234500063", "REF-SCOT", "Rouleau adhésif",
            "Scotch d'emballage 48mm", "Emballage", "Dépôt B - Rayon 2",
            "", "unite", "", "2026-10-01"
        ),
        listOf("", "", "", "", "", "", "", "", "", "", "")
    )

    fun buildSampleWorkbookBytes(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val rows: List<List<Any?>> = listOf(HEADERS) + ROWS
        XlsxWriter.write(outputStream, "Stock theorique", rows)
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
