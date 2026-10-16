package ma.bam.inventaire.util

/**
 * Normalise les en-têtes de colonnes du fichier Excel importé (insensible à la casse,
 * aux accents et aux espaces) vers les clés métier attendues.
 *
 * Colonnes attendues (export Sobrus - Stocks) : PID, Produit, Catégorie, TVA, PPV, PPH, Zone,
 * Stock, Date de péremption, Stock min, Stock max, Code barre 1, Code barre 2. PPV sert de prix
 * unitaire ; PPH est affiché à titre informatif dans l'aperçu détaillé de l'article.
 */
object ExcelColumnMapper {

    const val PID = "pid"
    const val PRODUIT = "produit"
    const val CATEGORIE = "categorie"
    const val TVA = "tva"
    const val PPV = "ppv"
    const val PPH = "pph"
    const val ZONE = "zone"
    const val STOCK = "stock"
    const val DATE_PEREMPTION = "date_de_peremption"
    const val STOCK_MIN = "stock_min"
    const val STOCK_MAX = "stock_max"
    const val CODE_BARRE_1 = "code_barre_1"
    const val CODE_BARRE_2 = "code_barre_2"

    /** Construit la map {clé normalisée -> index de colonne} à partir de la ligne d'en-tête. */
    fun mapHeaders(headerRow: List<String?>): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        headerRow.forEachIndexed { index, raw ->
            val normalized = normalize(raw ?: return@forEachIndexed)
            if (normalized.isNotBlank()) {
                result[normalized] = index
            }
        }
        return result
    }

    private fun normalize(value: String): String =
        value.trim()
            .lowercase()
            .replace(Regex("[éèêë]"), "e")
            .replace(Regex("[àâ]"), "a")
            .replace(Regex("[îï]"), "i")
            .replace(Regex("[ûü]"), "u")
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
}
