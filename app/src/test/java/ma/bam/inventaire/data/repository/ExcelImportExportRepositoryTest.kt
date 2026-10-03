package ma.bam.inventaire.data.repository

import ma.bam.inventaire.testutil.SampleInventoryFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class ExcelImportExportRepositoryTest {

    private val repository = ExcelImportExportRepository()

    @Test
    fun `import fixture produit les articles attendus`() {
        val result = repository.importTheoreticalStock(
            ByteArrayInputStream(SampleInventoryFixture.buildSampleWorkbookBytes())
        )

        // ART-001, ART-002 (première occurrence), ART-003, ART-004 -> 4 articles ;
        // la ligne vide est ignorée, la seconde ligne ART-002 est en erreur (doublon)
        assertEquals(4, result.articles.size)
        assertEquals(1, result.erreurs.size)
    }

    @Test
    fun `un code_article deja vu avec un autre code_barre est signale en erreur`() {
        val result = repository.importTheoreticalStock(
            ByteArrayInputStream(SampleInventoryFixture.buildSampleWorkbookBytes())
        )

        val art002 = result.articles.single { it.codeArticle == "ART-002" }
        assertEquals("6111234500025", art002.codeBarre)
        assertTrue(result.erreurs.single().contains("ART-002"))
    }

    @Test
    fun `chaque article importe a un seul code_barre`() {
        val result = repository.importTheoreticalStock(
            ByteArrayInputStream(SampleInventoryFixture.buildSampleWorkbookBytes())
        )

        val art003 = result.articles.single { it.codeArticle == "ART-003" }
        assertEquals("6111234500049", art003.codeBarre)
    }

    @Test
    fun `quantite et prix manquants sont ramenes a zero sans erreur`() {
        val result = repository.importTheoreticalStock(
            ByteArrayInputStream(SampleInventoryFixture.buildSampleWorkbookBytes())
        )

        val art004 = result.articles.single { it.codeArticle == "ART-004" }
        assertEquals(0.0, art004.quantiteTheorique, 0.0001)
        assertEquals(0.0, art004.prixUnitaire, 0.0001)
    }

    @Test
    fun `export puis relecture conserve les valeurs saisies`() {
        val session = ma.bam.inventaire.data.local.entity.InventorySessionEntity(
            id = "session-1",
            numero = "INV-20261002-001",
            dateCreation = 1759420800000L,
            dateFinalisation = null,
            statut = ma.bam.inventaire.data.local.entity.InventoryStatus.EN_COURS,
            nomFichierSource = "inventaire_exemple.xlsx"
        )
        val article = ma.bam.inventaire.data.local.entity.StockArticleEntity(
            id = 1L,
            sessionId = session.id,
            codeArticle = "ART-001",
            codeBarre = "6111234500018",
            reference = "REF-ENV-A4",
            designation = "Enveloppes A4 blanches",
            description = "Boîte de 500 enveloppes A4 90g",
            categorie = "Fournitures bureau",
            emplacement = "Dépôt A - Rayon 3",
            quantiteTheorique = 120.0,
            unite = "boite",
            prixUnitaire = 45.50,
            dateImport = 1759420800000L,
            quantiteReelle = 118.0,
            ecart = -2.0,
            ecartValide = true,
            dateScan = 1759420900000L
        )

        val outputStream = java.io.ByteArrayOutputStream()
        repository.exportSessions(outputStream, listOf(session to listOf(article)))

        val rows = ma.bam.inventaire.util.xlsx.XlsxReader.readFirstSheetRows(outputStream.toByteArray())
        val header = rows[0]
        val dataRow = rows[1]

        assertEquals("code_article", header[3])
        assertEquals("ART-001", dataRow[3])
        assertEquals("code_barre", header[4])
        assertEquals("6111234500018", dataRow[4])
        assertEquals("quantite_reelle", header[11])
        assertEquals(118.0, dataRow[11].toDouble(), 0.0001)
        assertEquals("ecart", header[12])
        assertEquals(-2.0, dataRow[12].toDouble(), 0.0001)
    }
}
