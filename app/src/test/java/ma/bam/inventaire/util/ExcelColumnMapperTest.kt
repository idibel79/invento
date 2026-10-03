package ma.bam.inventaire.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ExcelColumnMapperTest {

    @Test
    fun `mapHeaders normalise accents espaces et casse`() {
        val headers = listOf("Code Article", "Code-Barre", "Désignation", "Quantité Théorique")
        val map = ExcelColumnMapper.mapHeaders(headers)

        assertEquals(0, map[ExcelColumnMapper.CODE_ARTICLE])
        assertEquals(1, map[ExcelColumnMapper.CODE_BARRE])
        assertEquals(2, map[ExcelColumnMapper.DESIGNATION])
        assertEquals(3, map[ExcelColumnMapper.QUANTITE_THEORIQUE])
    }

    @Test
    fun `splitBarcodes gere plusieurs separateurs`() {
        val result = ExcelColumnMapper.splitBarcodes("123456 , 789012;345678")
        assertEquals(listOf("123456", "789012", "345678"), result)
    }

    @Test
    fun `splitBarcodes valeur unique`() {
        val result = ExcelColumnMapper.splitBarcodes("123456")
        assertEquals(listOf("123456"), result)
    }
}
