package ma.bam.inventaire.domain

import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.testutil.testArticle
import org.junit.Assert.assertEquals
import org.junit.Test

class ArticleFiltersTest {

    private val nonScanne = testArticle(id = 1, quantiteTheorique = 5.0)
    private val scanneSansEcart =
        testArticle(id = 2, quantiteTheorique = 3.0, quantiteReelle = 3.0, ecart = 0.0, ecartValide = true)
    private val ecart = testArticle(id = 3, quantiteTheorique = 4.0, quantiteReelle = 6.0, ecart = 2.0)
    private val corrige =
        testArticle(id = 4, quantiteTheorique = 8.0, quantiteReelle = 8.0, ecart = -1.0, ecartValide = true, corrige = true)
    private val negatif = testArticle(id = 5, quantiteTheorique = -2.0)
    private val vide = testArticle(id = 6, quantiteTheorique = 0.0)

    private val all = listOf(nonScanne, scanneSansEcart, ecart, corrige, negatif, vide)

    private fun ids(list: List<StockArticleEntity>) = list.map { it.id }

    private fun filter(f: ArticleFilter, query: String = "", fields: FieldFilters = FieldFilters()) =
        ids(ArticleFilters.applyFilters(all, f, query, fields))

    @Test
    fun `TOUS garde tout dans l'ordre`() {
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L, 6L), filter(ArticleFilter.TOUS))
    }

    @Test
    fun `SCANNES garde les articles avec quantite reelle`() {
        assertEquals(listOf(2L, 3L, 4L), filter(ArticleFilter.SCANNES))
    }

    @Test
    fun `NON_SCANNES garde les articles sans quantite reelle`() {
        assertEquals(listOf(1L, 5L, 6L), filter(ArticleFilter.NON_SCANNES))
    }

    @Test
    fun `ECARTS exclut les corriges et les ecarts nuls`() {
        assertEquals(listOf(3L), filter(ArticleFilter.ECARTS))
    }

    @Test
    fun `CORRIGES garde les articles corriges meme avec ecart`() {
        assertEquals(listOf(4L), filter(ArticleFilter.CORRIGES))
    }

    @Test
    fun `NEGATIFS garde les stocks systeme negatifs`() {
        assertEquals(listOf(5L), filter(ArticleFilter.NEGATIFS))
    }

    @Test
    fun `VIDE garde les stocks systeme a zero`() {
        assertEquals(listOf(6L), filter(ArticleFilter.VIDE))
    }

    @Test
    fun `counts concorde avec chaque filtre`() {
        val counts = ArticleFilters.counts(all)
        ArticleFilter.entries.forEach { f ->
            assertEquals("filtre $f", filter(f).size, counts[f])
        }
    }

    @Test
    fun `recherche sur code-barres, code article et designation sans casse`() {
        val list = listOf(
            testArticle(id = 1, codeBarre1 = "611100"),
            testArticle(id = 2, codeBarre2 = "XY611"),
            testArticle(id = 3, codeArticle = "ABC"),
            testArticle(id = 4, designation = "Doliprane 500"),
            testArticle(id = 5, codeArticle = "ZZZ", designation = "Autre")
        )
        fun search(q: String) = ids(ArticleFilters.applyFilters(list, ArticleFilter.TOUS, q, FieldFilters()))
        assertEquals(listOf(1L, 2L), search("611"))
        assertEquals(listOf(3L), search("abc"))
        assertEquals(listOf(4L), search("DOLI"))
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), search("   "))
        assertEquals(emptyList<Long>(), search("introuvable"))
    }

    @Test
    fun `filtre par zone, categorie et prix cumulables`() {
        val list = listOf(
            testArticle(id = 1, emplacement = "A", categorie = "Med", prixUnitaire = 10.0),
            testArticle(id = 2, emplacement = "A", categorie = "Para", prixUnitaire = 20.0),
            testArticle(id = 3, emplacement = "B", categorie = "Med", prixUnitaire = 30.0)
        )
        fun f(fields: FieldFilters) = ids(ArticleFilters.applyFilters(list, ArticleFilter.TOUS, "", fields))
        assertEquals(listOf(1L, 2L), f(FieldFilters(position = "A")))
        assertEquals(listOf(1L, 3L), f(FieldFilters(category = "Med")))
        assertEquals(listOf(1L, 2L), f(FieldFilters(priceRange = 10f..20f)))
        assertEquals(listOf(1L), f(FieldFilters(position = "A", category = "Med", priceRange = 0f..15f)))
    }

    @Test
    fun `filtre rapide et recherche se cumulent`() {
        val list = listOf(
            testArticle(id = 1, designation = "Alpha", quantiteReelle = 1.0),
            testArticle(id = 2, designation = "Alpha"),
            testArticle(id = 3, designation = "Beta", quantiteReelle = 1.0)
        )
        assertEquals(listOf(1L), ids(ArticleFilters.applyFilters(list, ArticleFilter.SCANNES, "alp", FieldFilters())))
    }

    private val toSort = listOf(
        testArticle(id = 1, designation = "banane", prixUnitaire = 5.0, quantiteTheorique = 3.0),
        testArticle(id = 2, designation = "", codeArticle = "Cerise", prixUnitaire = 1.0, quantiteTheorique = 9.0),
        testArticle(id = 3, designation = "Abricot", prixUnitaire = 9.0, quantiteTheorique = -1.0)
    )

    private fun sorted(s: ArticleSort) = ids(ArticleFilters.sort(toSort, s))

    @Test
    fun `DEFAUT conserve l'ordre`() {
        assertEquals(listOf(1L, 2L, 3L), sorted(ArticleSort.DEFAUT))
    }

    @Test
    fun `TITRE trie sans casse avec repli sur code article`() {
        assertEquals(listOf(3L, 1L, 2L), sorted(ArticleSort.TITRE_AZ))
        assertEquals(listOf(2L, 1L, 3L), sorted(ArticleSort.TITRE_ZA))
    }

    @Test
    fun `PRIX trie par PPV`() {
        assertEquals(listOf(2L, 1L, 3L), sorted(ArticleSort.PRIX_ASC))
        assertEquals(listOf(3L, 1L, 2L), sorted(ArticleSort.PRIX_DESC))
    }

    @Test
    fun `QUANTITE trie par stock systeme`() {
        assertEquals(listOf(3L, 1L, 2L), sorted(ArticleSort.QUANTITE_ASC))
        assertEquals(listOf(2L, 1L, 3L), sorted(ArticleSort.QUANTITE_DESC))
    }
}
