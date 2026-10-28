package ma.bam.inventaire.ui.inventorydetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ma.bam.inventaire.data.repository.ImportedArticle

private fun String.toDoubleOrNullFr(): Double? = replace(",", ".").toDoubleOrNull()

/**
 * Champs saisis dans la popup de création d'un produit. Tous sont obligatoires sauf le code-barres 2,
 * la quantité en stock (0 si non saisie) et la quantité inventoriée (non comptée si non saisie).
 */
internal class CreateArticleForm {
    var designation by mutableStateOf("")
    var codeArticle by mutableStateOf("")
    var codeBarre1 by mutableStateOf("")
    var codeBarre2 by mutableStateOf("")
    var categorie by mutableStateOf<String?>(null)
    var tva by mutableStateOf<String?>(null)
    var zone by mutableStateOf<String?>(null)
    var quantiteText by mutableStateOf("")
    var quantiteInventaireText by mutableStateOf("")
    var prixText by mutableStateOf("")
    var pphText by mutableStateOf("")

    /** Passe à vrai au premier « Créer » incomplet : les champs manquants s'affichent alors en rouge. */
    var showErrors by mutableStateOf(false)

    val quantite: Double? get() = if (quantiteText.isBlank()) 0.0 else quantiteText.toDoubleOrNullFr()
    val quantiteInventaire: Double? get() = if (quantiteInventaireText.isBlank()) null else quantiteInventaireText.toDoubleOrNullFr()
    val prix: Double? get() = prixText.toDoubleOrNullFr()
    val pph: Double? get() = pphText.toDoubleOrNullFr()

    val isComplete: Boolean
        get() = listOf(designation, codeArticle, codeBarre1).all { it.isNotBlank() } &&
            listOf(categorie, tva, zone, quantite, prix, pph).all { it != null }

    /** Le produit à créer, ou null (et les erreurs affichées) s'il manque un champ obligatoire. */
    fun submit(now: Long): ImportedArticle? {
        if (!isComplete) {
            showErrors = true
            return null
        }
        return ImportedArticle(
            codeArticle = codeArticle.trim(),
            designation = designation.trim(),
            categorie = categorie.orEmpty(),
            tva = tva.orEmpty(),
            emplacement = zone.orEmpty(),
            quantiteTheorique = quantite ?: 0.0,
            datePeremption = "",
            stockMin = 0.0,
            stockMax = 0.0,
            codeBarre1 = codeBarre1.trim(),
            codeBarre2 = codeBarre2.trim(),
            prixUnitaire = prix ?: 0.0,
            pph = pph ?: 0.0,
            dateImport = now,
            quantiteReelle = quantiteInventaire
        )
    }
}
