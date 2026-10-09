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

private fun String.toDoubleOrNullFr(): Double? = replace(",", ".").toDoubleOrNull()

/**
 * Popup de création manuelle d'un produit (page des produits). Tous les champs sont
 * obligatoires, sauf la quantité en stock (par défaut 0 si non saisie).
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
    var stockMinText by remember { mutableStateOf("") }
    var stockMaxText by remember { mutableStateOf("") }
    var prixText by remember { mutableStateOf("") }
    var pphText by remember { mutableStateOf("") }

    val quantite = if (quantiteText.isBlank()) 0.0 else quantiteText.toDoubleOrNullFr()
    val stockMin = stockMinText.toDoubleOrNullFr()
    val stockMax = stockMaxText.toDoubleOrNullFr()
    val prix = prixText.toDoubleOrNullFr()
    val pph = pphText.toDoubleOrNullFr()

    val isValid = designation.isNotBlank() && codeArticle.isNotBlank() &&
        codeBarre1.isNotBlank() && codeBarre2.isNotBlank() &&
        categorie != null && tva != null && zone != null &&
        quantite != null && stockMin != null && stockMax != null && prix != null && pph != null

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
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                OutlinedTextField(
                    value = codeArticle,
                    onValueChange = { codeArticle = it },
                    label = { Text("Code article (PID)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                OutlinedTextField(
                    value = codeBarre1,
                    onValueChange = { codeBarre1 = it },
                    label = { Text("Code-barres 1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                OutlinedTextField(
                    value = codeBarre2,
                    onValueChange = { codeBarre2 = it },
                    label = { Text("Code-barres 2") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                DropdownField(
                    label = "Catégorie",
                    options = availableCategories,
                    selected = categorie,
                    onSelect = { categorie = it }
                )
                FieldSpacer()
                DropdownField(
                    label = "TVA",
                    options = availableTvas,
                    selected = tva,
                    onSelect = { tva = it }
                )
                FieldSpacer()
                DropdownField(
                    label = "Zone",
                    options = availableZones,
                    selected = zone,
                    onSelect = { zone = it }
                )
                FieldSpacer()
                OutlinedTextField(
                    value = quantiteText,
                    onValueChange = { quantiteText = it },
                    label = { Text("Quantité en stock (optionnel)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                OutlinedTextField(
                    value = stockMinText,
                    onValueChange = { stockMinText = it },
                    label = { Text("Stock min") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                OutlinedTextField(
                    value = stockMaxText,
                    onValueChange = { stockMaxText = it },
                    label = { Text("Stock max") },
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
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                OutlinedTextField(
                    value = pphText,
                    onValueChange = { pphText = it },
                    label = { Text("PPH") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = {
                    onConfirm(
                        ImportedArticle(
                            codeArticle = codeArticle.trim(),
                            designation = designation.trim(),
                            categorie = categorie.orEmpty(),
                            tva = tva.orEmpty(),
                            emplacement = zone.orEmpty(),
                            quantiteTheorique = quantite ?: 0.0,
                            datePeremption = "",
                            stockMin = stockMin ?: 0.0,
                            stockMax = stockMax ?: 0.0,
                            codeBarre1 = codeBarre1.trim(),
                            codeBarre2 = codeBarre2.trim(),
                            prixUnitaire = prix ?: 0.0,
                            pph = pph ?: 0.0,
                            dateImport = System.currentTimeMillis()
                        )
                    )
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
    onSelect: (String) -> Unit
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
