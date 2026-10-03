package ma.bam.inventaire.util

/**
 * Normalise les en-têtes de colonnes du fichier Excel importé (insensible à la casse,
 * aux accents et aux espaces) vers les clés métier attendues.
 */
object ExcelColumnMapper {

    const val CODE_ARTICLE = "code_article"
    const val CODE_BARRE = "code_barre"
    const val REFERENCE = "reference"
    const val DESIGNATION = "designation"
    const val DESCRIPTION = "description"
    const val CATEGORIE = "categorie"
    const val EMPLACEMENT = "emplacement"
    const val QUANTITE_THEORIQUE = "quantite_theorique"
    const val UNITE = "unite"
    const val PRIX_UNITAIRE = "prix_unitaire"
    const val DATE_IMPORT = "date_import"

    val EXPECTED_COLUMNS = listOf(
        CODE_ARTICLE, CODE_BARRE, REFERENCE, DESIGNATION, DESCRIPTION, CATEGORIE,
        EMPLACEMENT, QUANTITE_THEORIQUE, UNITE, PRIX_UNITAIRE, DATE_IMPORT
    )

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
