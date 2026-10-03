package ma.bam.inventaire.domain

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
}
