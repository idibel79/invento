package ma.bam.inventaire.ui.inventorydetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import ma.bam.inventaire.ui.components.DialogButtons
import ma.bam.inventaire.R
import ma.bam.inventaire.data.repository.ImportedArticle
import ma.bam.inventaire.ui.components.StaticLabelTextField
import ma.bam.inventaire.ui.components.fadingEdge
import ma.bam.inventaire.ui.theme.BamPrimary

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
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_add_product),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = androidx.compose.ui.graphics.Color.Unspecified
            )
        },
        title = { Text(stringResource(R.string.create_title), fontWeight = FontWeight.SemiBold) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                StaticLabelTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = stringResource(R.string.create_field_title),
                    containerColor = AlertDialogDefaults.containerColor,
                    singleLine = true,
                    isError = showErrors && designation.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                StaticLabelTextField(
                    value = codeArticle,
                    onValueChange = { codeArticle = it },
                    label = stringResource(R.string.create_field_pid),
                    containerColor = AlertDialogDefaults.containerColor,
                    singleLine = true,
                    isError = showErrors && codeArticle.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                StaticLabelTextField(
                    value = codeBarre1,
                    onValueChange = { codeBarre1 = it },
                    label = stringResource(R.string.create_field_barcode1),
                    containerColor = AlertDialogDefaults.containerColor,
                    singleLine = true,
                    isError = showErrors && codeBarre1.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                StaticLabelTextField(
                    value = codeBarre2,
                    onValueChange = { codeBarre2 = it },
                    label = stringResource(R.string.create_field_barcode2),
                    containerColor = AlertDialogDefaults.containerColor,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                DropdownField(
                    label = stringResource(R.string.field_category),
                    options = availableCategories,
                    selected = categorie,
                    onSelect = { categorie = it },
                    isError = showErrors && categorie == null
                )
                FieldSpacer()
                DropdownField(
                    label = stringResource(R.string.field_tva),
                    options = availableTvas,
                    selected = tva,
                    onSelect = { tva = it },
                    isError = showErrors && tva == null
                )
                FieldSpacer()
                DropdownField(
                    label = stringResource(R.string.field_zone),
                    options = availableZones,
                    selected = zone,
                    onSelect = { zone = it },
                    isError = showErrors && zone == null
                )
                FieldSpacer()
                StaticLabelTextField(
                    value = quantiteText,
                    onValueChange = { quantiteText = it },
                    label = stringResource(R.string.create_field_system_qty),
                    containerColor = AlertDialogDefaults.containerColor,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                StaticLabelTextField(
                    value = quantiteInventaireText,
                    onValueChange = { quantiteInventaireText = it },
                    label = stringResource(R.string.create_field_counted_qty),
                    containerColor = AlertDialogDefaults.containerColor,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                StaticLabelTextField(
                    value = prixText,
                    onValueChange = { prixText = it },
                    label = stringResource(R.string.field_ppv),
                    containerColor = AlertDialogDefaults.containerColor,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = showErrors && prix == null,
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                StaticLabelTextField(
                    value = pphText,
                    onValueChange = { pphText = it },
                    label = stringResource(R.string.field_pph),
                    containerColor = AlertDialogDefaults.containerColor,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = showErrors && pph == null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            DialogButtons(
                secondaryLabel = stringResource(R.string.common_cancel),
                onSecondary = onDismiss,
                primaryLabel = stringResource(R.string.create_confirm),
                modifier = Modifier,
                onPrimary = {
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
                }
            )
        }
    )
}

@Composable
private fun FieldSpacer() {
    Spacer(modifier = Modifier.height(20.dp))
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
    val valueScrollState = rememberScrollState()
    Box(modifier = Modifier.fillMaxWidth()) {
        StaticLabelTextField(
            // La valeur réelle est affichée par le Text défilant ci-dessous (pour pouvoir
            // glisser et lire le texte en entier) ; le champ natif reste donc vide.
            value = "",
            onValueChange = {},
            label = label,
            containerColor = AlertDialogDefaults.containerColor,
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = stringResource(R.string.create_open_list))
                }
            },
            singleLine = true,
            isError = isError,
            modifier = Modifier.fillMaxWidth()
        )
        // Capte le clic sur tout le champ (pas seulement la flèche) pour ouvrir la liste, et
        // affiche la valeur sélectionnée en glissable pour pouvoir lire le texte en entier.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = { expanded = true })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 48.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    selected.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .fadingEdge(valueScrollState)
                        .horizontalScroll(valueScrollState)
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 280.dp)
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.create_no_value), color = MaterialTheme.colorScheme.onSurfaceVariant) },
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
