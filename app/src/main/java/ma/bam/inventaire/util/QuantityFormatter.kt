package ma.bam.inventaire.util

import java.util.Locale

/**
 * Arrondit à 2 décimales et affiche un entier sans décimales inutiles (évite les artefacts
 * flottants type -2.9999999999999996). Utilisé partout où une quantité ou un écart est affiché.
 */
fun formatQuantity(value: Double, showSign: Boolean = false): String {
    val rounded = kotlin.math.round(value * 100) / 100.0
    val text = if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        String.format(Locale.FRANCE, "%.2f", rounded)
    }
    return if (showSign && rounded > 0) "+$text" else text
}
