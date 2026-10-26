package ma.bam.inventaire.domain

import ma.bam.inventaire.testutil.testArticle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleSearchTest {

    private val article = testArticle(
        id = 1,
        codeArticle = "PID-42",
        designation = "Doliprane 500 mg",
        codeBarre1 = "6111000000042",
        codeBarre2 = "XY-990"
    )

    @Test
    fun `recherche dans la designation sans casse`() {
        assertTrue(article.matchesQuery("doli"))
        assertTrue(article.matchesQuery("500 MG"))
    }

    @Test
    fun `recherche dans le PID`() {
        assertTrue(article.matchesQuery("pid-4"))
    }

    @Test
    fun `recherche dans les deux codes-barres`() {
        assertTrue(article.matchesQuery("0000042"))
        assertTrue(article.matchesQuery("xy-99"))
    }

    @Test
    fun `recherche vide ou blanche correspond a tout`() {
        assertTrue(article.matchesQuery(""))
        assertTrue(article.matchesQuery("   "))
    }

    @Test
    fun `recherche sans correspondance`() {
        assertFalse(article.matchesQuery("aspirine"))
    }
}
