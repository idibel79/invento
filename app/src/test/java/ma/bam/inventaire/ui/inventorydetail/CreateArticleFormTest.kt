package ma.bam.inventaire.ui.inventorydetail

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateArticleFormTest {

    private fun completeForm() = CreateArticleForm().apply {
        designation = "  Doliprane 1000  "
        codeArticle = " PID-1 "
        codeBarre1 = " 6111 "
        categorie = "Médicament"
        tva = "Exonéré"
        zone = "Rayon A"
        prixText = "12,50"
        pphText = "10.2"
    }

    @Test
    fun `formulaire vide incomplet, submit affiche les erreurs`() {
        val form = CreateArticleForm()
        assertFalse(form.isComplete)
        assertNull(form.submit(now = 1L))
        assertTrue(form.showErrors)
    }

    @Test
    fun `champs facultatifs vides donnent stock 0 et article non compte`() {
        val article = completeForm().submit(now = 42L)!!
        assertEquals("Doliprane 1000", article.designation)
        assertEquals("PID-1", article.codeArticle)
        assertEquals("6111", article.codeBarre1)
        assertEquals("", article.codeBarre2)
        assertEquals(0.0, article.quantiteTheorique, 0.0)
        assertNull(article.quantiteReelle)
        assertEquals(12.5, article.prixUnitaire, 0.0)
        assertEquals(10.2, article.pph, 0.0)
        assertEquals(42L, article.dateImport)
    }

    @Test
    fun `quantites saisies avec virgule`() {
        val article = completeForm().apply {
            quantiteText = "3,5"
            quantiteInventaireText = "2"
        }.submit(now = 0L)!!
        assertEquals(3.5, article.quantiteTheorique, 0.0)
        assertEquals(2.0, article.quantiteReelle!!, 0.0)
    }

    @Test
    fun `prix invalide ou zone manquante rendent le formulaire incomplet`() {
        assertFalse(completeForm().apply { prixText = "abc" }.isComplete)
        assertFalse(completeForm().apply { zone = null }.isComplete)
        assertFalse(completeForm().apply { codeBarre1 = " " }.isComplete)
        assertFalse(completeForm().apply { quantiteText = "x" }.isComplete)
        assertTrue(completeForm().isComplete)
    }
}
