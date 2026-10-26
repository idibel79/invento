package ma.bam.inventaire.domain

import ma.bam.inventaire.testutil.testArticle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EcartCalculatorTest {

    @Test
    fun `ecart nul quand quantites identiques`() {
        val ecart = EcartCalculator.compute(quantiteTheorique = 10.0, quantiteReelle = 10.0)
        assertEquals(0.0, ecart, 0.0001)
        assertFalse(EcartCalculator.hasEcart(ecart))
    }

    @Test
    fun `ecart positif quand surplus`() {
        val ecart = EcartCalculator.compute(quantiteTheorique = 10.0, quantiteReelle = 12.0)
        assertEquals(2.0, ecart, 0.0001)
        assertTrue(EcartCalculator.hasEcart(ecart))
    }

    @Test
    fun `ecart negatif quand manquant`() {
        val ecart = EcartCalculator.compute(quantiteTheorique = 10.0, quantiteReelle = 7.0)
        assertEquals(-3.0, ecart, 0.0001)
        assertTrue(EcartCalculator.hasEcart(ecart))
    }

    @Test
    fun `hasEcart faux pour null`() {
        assertFalse(EcartCalculator.hasEcart(null))
    }

    @Test
    fun `ecart sous le seuil d'arrondi considere nul`() {
        assertFalse(EcartCalculator.hasEcart(EcartCalculator.SEUIL_ECART_NUL))
        assertFalse(EcartCalculator.hasEcart(-EcartCalculator.SEUIL_ECART_NUL / 2))
        assertTrue(EcartCalculator.hasEcart(EcartCalculator.SEUIL_ECART_NUL * 10))
        // 0.1 + 0.2 - 0.3 ≠ 0 en Double, mais reste sous le seuil.
        assertFalse(EcartCalculator.hasEcart(EcartCalculator.compute(0.3, 0.1 + 0.2)))
    }

    @Test
    fun `ecart non corrige exclut les corriges, les conformes et les non comptes`() {
        assertTrue(EcartCalculator.isEcartNonCorrige(testArticle(ecart = 2.0)))
        assertFalse(EcartCalculator.isEcartNonCorrige(testArticle(ecart = 2.0, corrige = true)))
        assertFalse(EcartCalculator.isEcartNonCorrige(testArticle(ecart = 0.0)))
        assertFalse(EcartCalculator.isEcartNonCorrige(testArticle(ecart = null)))
    }

    @Test
    fun `ecart fort a partir de 5 en valeur absolue`() {
        assertTrue(EcartCalculator.isEcartFort(5.0))
        assertTrue(EcartCalculator.isEcartFort(-5.0))
        assertFalse(EcartCalculator.isEcartFort(4.99))
        assertFalse(EcartCalculator.isEcartFort(null))
    }
}
