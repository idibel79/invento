package ma.bam.inventaire.ui.inventorydetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ma.bam.inventaire.data.repository.ImportedArticle
import ma.bam.inventaire.ui.theme.SuccessGreen
import ma.bam.inventaire.util.formatQuantity

private fun String.toDoubleOrNullFr(): Double? = replace(",", ".").toDoubleOrNull()

/**
 * Popup de création manuelle d'un produit (page des produits). Tous les champs sont
 * obligatoires sauf le code-barres 2 et la quantité en stock (par défaut 0 si non saisie).
 * Le bouton "Créer" reste toujours actif ; s'il manque un champ obligatoire, celui-ci est
 * mis en évidence en rouge au lieu de bloquer le bouton.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateArticleDialog(
    availableCategories: List<String>,
    availableTvas: List<String>,
    availableZones: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (ImportedArticle) -> Unit
) {
    var designation by remember { mutableStateOf("") }
    var codeArticle by remember { mutableStateOf("") }
    var codeBarre1 by remember { mutableStateOf("") }
    var codeBarre2 by remember { mutableStateOf("") }
    var categorie by remember { mutableStateOf<String?>(null) }
    var tva by remember { mutableStateOf<String?>(null) }
    var zone by remember { mutableStateOf<String?>(null) }
    var quantiteText by remember { mutableStateOf("") }
    var quantiteInventaireText by remember { mutableStateOf("") }
    var prixText by remember { mutableStateOf("") }
    var pphText by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }

    val quantite = if (quantiteText.isBlank()) 0.0 else quantiteText.toDoubleOrNullFr()
    val quantiteInventaire = if (quantiteInventaireText.isBlank()) null else quantiteInventaireText.toDoubleOrNullFr()
    val prix = prixText.toDoubleOrNullFr()
    val pph = pphText.toDoubleOrNullFr()

    val isComplete = designation.isNotBlank() && codeArticle.isNotBlank() &&
        codeBarre1.isNotBlank() &&
        categorie != null && tva != null && zone != null &&
        quantite != null && prix != null && pph != null

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.AddBox, contentDescription = null) },
        title = { Text("Nouveau produit", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("Titre du produit") },
                    singleLine = true,
                    isError = showErrors && designation.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                OutlinedTextField(
                    value = codeArticle,
                    onValueChange = { codeArticle = it },
                    label = { Text("Code article (PID)") },
                    singleLine = true,
                    isError = showErrors && codeArticle.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                OutlinedTextField(
                    value = codeBarre1,
                    onValueChange = { codeBarre1 = it },
                    label = { Text("Code-barres 1") },
                    singleLine = true,
                    isError = showErrors && codeBarre1.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                OutlinedTextField(
                    value = codeBarre2,
                    onValueChange = { codeBarre2 = it },
                    label = { Text("Code-barres 2 (optionnel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                DropdownField(
                    label = "Catégorie",
                    options = availableCategories,
                    selected = categorie,
                    onSelect = { categorie = it },
                    isError = showErrors && categorie == null
                )
                FieldSpacer()
                DropdownField(
                    label = "TVA",
                    options = availableTvas,
                    selected = tva,
                    onSelect = { tva = it },
                    isError = showErrors && tva == null
                )
                FieldSpacer()
                DropdownField(
                    label = "Zone",
                    options = availableZones,
                    selected = zone,
                    onSelect = { zone = it },
                    isError = showErrors && zone == null
                )
                FieldSpacer()
                OutlinedTextField(
                    value = quantiteText,
                    onValueChange = { quantiteText = it },
                    label = { Text("Quantité système (optionnel)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                OutlinedTextField(
                    value = quantiteInventaireText,
                    onValueChange = { quantiteInventaireText = it },
                    label = { Text("Quantité inventaire (optionnel)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                OutlinedTextField(
                    value = prixText,
                    onValueChange = { prixText = it },
                    label = { Text("PPV") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = showErrors && prix == null,
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                OutlinedTextField(
                    value = pphText,
                    onValueChange = { pphText = it },
                    label = { Text("PPH") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = showErrors && pph == null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isComplete) {
                        onConfirm(
                            ImportedArticle(
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
                                dateImport = System.currentTimeMillis(),
                                quantiteReelle = quantiteInventaire
                            )
                        )
                    } else {
                        showErrors = true
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = SuccessGreen)
            ) {
                Text("Créer", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

/** Popup de confirmation affiché juste après la création manuelle d'un produit. */
@Composable
fun ArticleCreatedDialog(
    article: ImportedArticle,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen) },
        title = { Text("Votre produit a bien été créé", fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                Text(
                    article.designation.ifBlank { article.codeArticle },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Code article (PID) : ${article.codeArticle}")
                Text("Code-barres 1 : ${article.codeBarre1}")
                if (article.codeBarre2.isNotBlank()) {
                    Text("Code-barres 2 : ${article.codeBarre2}")
                }
                Text("Catégorie : ${article.categorie}")
                Text("Zone : ${article.emplacement}")
                Text("Quantité système : ${formatQuantity(article.quantiteTheorique)}")
                Text(
                    "Quantité inventaire : " +
                        (article.quantiteReelle?.let { formatQuantity(it) } ?: "-")
                )
                Text("PPV : ${formatQuantity(article.prixUnitaire)} DH")
                Text("PPH : ${formatQuantity(article.pph)} DH")
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = SuccessGreen)
            ) {
                Text("OK", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
private fun FieldSpacer() {
    Spacer(modifier = Modifier.height(10.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    isError: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Ouvrir la liste")
                }
            },
            singleLine = true,
            isError = isError,
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 280.dp)
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Aucune valeur disponible", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = { expanded = false },
                    enabled = false
                )
            }
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
