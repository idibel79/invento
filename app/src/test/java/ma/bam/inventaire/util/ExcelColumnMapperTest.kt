package ma.bam.inventaire.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ExcelColumnMapperTest {

    @Test
    fun `mapHeaders normalise accents espaces et casse`() {
        // Le texte est supposé déjà correctement décodé ici (la réparation du mojibake se fait
        // en amont, dans XlsxReader) : on teste uniquement la normalisation accents/espaces/casse.
        val headers = listOf("PID", "Produit", "Catégorie", "Zone", "Date de péremption")
        val map = ExcelColumnMapper.mapHeaders(headers)

        assertEquals(0, map[ExcelColumnMapper.PID])
        assertEquals(1, map[ExcelColumnMapper.PRODUIT])
        assertEquals(2, map[ExcelColumnMapper.CATEGORIE])
        assertEquals(3, map[ExcelColumnMapper.ZONE])
        assertEquals(4, map[ExcelColumnMapper.DATE_PEREMPTION])
    }

    @Test
    fun `mapHeaders gere les en-tetes de l export Sobrus`() {
        val headers = listOf(
            "PID", "Produit", "Catégorie", "TVA", "PPV", "PPH", "Zone", "Stock",
            "Date de péremption", "Stock min", "Stock max", "Code barre 1", "Code barre 2"
        )
        val map = ExcelColumnMapper.mapHeaders(headers)

        assertEquals(0, map[ExcelColumnMapper.PID])
        assertEquals(1, map[ExcelColumnMapper.PRODUIT])
        assertEquals(2, map[ExcelColumnMapper.CATEGORIE])
        assertEquals(3, map[ExcelColumnMapper.TVA])
        assertEquals(4, map[ExcelColumnMapper.PPV])
        assertEquals(5, map[ExcelColumnMapper.PPH])
        assertEquals(6, map[ExcelColumnMapper.ZONE])
        assertEquals(7, map[ExcelColumnMapper.STOCK])
        assertEquals(8, map[ExcelColumnMapper.DATE_PEREMPTION])
        assertEquals(9, map[ExcelColumnMapper.STOCK_MIN])
        assertEquals(10, map[ExcelColumnMapper.STOCK_MAX])
        assertEquals(11, map[ExcelColumnMapper.CODE_BARRE_1])
        assertEquals(12, map[ExcelColumnMapper.CODE_BARRE_2])
    }
}
