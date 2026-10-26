package ma.bam.inventaire.domain

import ma.bam.inventaire.testutil.testArticle
import org.junit.Assert.assertEquals
import org.junit.Test

class ArticleStatsTest {

    private val articles = listOf(
        testArticle(id = 1, emplacement = "B", categorie = "Med", prixUnitaire = 12.5, quantiteTheorique = 0.0),
        testArticle(
            id = 2,
            emplacement = "A",
            categorie = "Med",
            prixUnitaire = 3.0,
            quantiteReelle = 1.0,
            ecart = 1.0
        ),
        testArticle(id = 3, emplacement = "", categorie = "Para", prixUnitaire = 40.0, quantiteTheorique = -1.0),
        testArticle(id = 4, emplacement = "A", categorie = "", prixUnitaire = 7.0, ecart = 2.0, corrige = true)
    ).mapIndexed { i, a -> a.copy(tva = if (i % 2 == 0) "TVA (20.00%)" else "") }

    private val stats = ArticleStats.of(articles)

    @Test
    fun `compteurs de filtres identiques a ArticleFilters`() {
        assertEquals(ArticleFilters.counts(articles), stats.filterCounts)
    }

    @Test
    fun `zones et categories non vides triees avec leurs compteurs`() {
        assertEquals(listOf("A", "B"), stats.positions)
        assertEquals(mapOf("A" to 2, "B" to 1), stats.positionCounts)
        assertEquals(listOf("Med", "Para"), stats.categories)
        assertEquals(mapOf("Med" to 2, "Para" to 1), stats.categoryCounts)
        assertEquals(listOf("TVA (20.00%)"), stats.tvas)
    }

    @Test
    fun `bornes de prix min et max`() {
        assertEquals(3f..40f, stats.priceBounds)
    }

    @Test
    fun `liste vide donne des agregats vides`() {
        val empty = ArticleStats.of(emptyList())
        assertEquals(0f..0f, empty.priceBounds)
        assertEquals(emptyList<String>(), empty.positions)
        assertEquals(0, empty.filterCounts[ArticleFilter.TOUS])
    }
}
