package ma.bam.inventaire.ui.home

import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.InventoryStatus
import ma.bam.inventaire.data.repository.SessionCounters
import ma.bam.inventaire.ui.inventorylist.SessionListItem
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeFilterTest {

    private fun item(total: Int, scanned: Int, ecarts: Int = 0, verrouille: Boolean = false) = SessionListItem(
        session = InventorySessionEntity(
            id = "s",
            numero = "INV-1",
            dateCreation = 0L,
            dateFinalisation = null,
            statut = InventoryStatus.EN_COURS,
            nomFichierSource = "stock.xlsx",
            verrouille = verrouille
        ),
        counters = SessionCounters(total, scanned, ecarts, pendingEcarts = ecarts, negativeStock = 0, emptyStock = 0)
    )

    private fun filtersOf(item: SessionListItem) = HomeFilter.entries.filter { it.matches(item) }.toSet()

    @Test
    fun `aucun article compte = en attente`() {
        assertEquals(setOf(HomeFilter.PENDING), filtersOf(item(total = 10, scanned = 0)))
    }

    @Test
    fun `partiellement compte avec ecart = en cours et ecarts`() {
        assertEquals(setOf(HomeFilter.ONGOING, HomeFilter.ECARTS), filtersOf(item(total = 10, scanned = 4, ecarts = 1)))
    }

    @Test
    fun `tout compte = finalise`() {
        assertEquals(setOf(HomeFilter.FINALIZED), filtersOf(item(total = 10, scanned = 10)))
    }

    @Test
    fun `cloture = clotures seulement, meme sans comptage`() {
        assertEquals(setOf(HomeFilter.CLOSED), filtersOf(item(total = 10, scanned = 0, verrouille = true)))
    }

    @Test
    fun `cloture et tout compte = clotures et non finalise`() {
        assertEquals(setOf(HomeFilter.CLOSED), filtersOf(item(total = 10, scanned = 10, verrouille = true)))
    }

    @Test
    fun `cloture avec ecart = clotures et ecarts`() {
        assertEquals(setOf(HomeFilter.CLOSED, HomeFilter.ECARTS), filtersOf(item(total = 10, scanned = 10, ecarts = 2, verrouille = true)))
    }

    @Test
    fun `inventaire vide non cloture = en attente`() {
        assertEquals(setOf(HomeFilter.PENDING), filtersOf(item(total = 0, scanned = 0)))
    }
}
