package ma.bam.inventaire.ui.inventorydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ma.bam.inventaire.R
import ma.bam.inventaire.data.repository.ImportedArticle
import ma.bam.inventaire.ui.components.StaticLabelTextField
import ma.bam.inventaire.ui.theme.InProgressBlue
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
                Spacer(modifier = Modifier.height(12.dp))
                StaticLabelTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = "Titre du produit",
                    containerColor = AlertDialogDefaults.containerColor,
                    singleLine = true,
                    isError = showErrors && designation.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                StaticLabelTextField(
                    value = codeArticle,
                    onValueChange = { codeArticle = it },
                    label = "Code article (PID)",
                    containerColor = AlertDialogDefaults.containerColor,
                    singleLine = true,
                    isError = showErrors && codeArticle.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                StaticLabelTextField(
                    value = codeBarre1,
                    onValueChange = { codeBarre1 = it },
                    label = "Code-barres 1",
                    containerColor = AlertDialogDefaults.containerColor,
                    singleLine = true,
                    isError = showErrors && codeBarre1.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                StaticLabelTextField(
                    value = codeBarre2,
                    onValueChange = { codeBarre2 = it },
                    label = "Code-barres 2 (optionnel)",
                    containerColor = AlertDialogDefaults.containerColor,
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
                StaticLabelTextField(
                    value = quantiteText,
                    onValueChange = { quantiteText = it },
                    label = "Quantité système (optionnel)",
                    containerColor = AlertDialogDefaults.containerColor,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                StaticLabelTextField(
                    value = quantiteInventaireText,
                    onValueChange = { quantiteInventaireText = it },
                    label = "Quantité inventaire (optionnel)",
                    containerColor = AlertDialogDefaults.containerColor,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                FieldSpacer()
                StaticLabelTextField(
                    value = prixText,
                    onValueChange = { prixText = it },
                    label = "PPV",
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
                    label = "PPH",
                    containerColor = AlertDialogDefaults.containerColor,
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
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                "Votre produit a bien été créé",
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    article.designation.ifBlank { article.codeArticle },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = InProgressBlue,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(painter = painterResource(id = R.drawable.ic_pid_cross), text = article.codeArticle)
                val codesBarres = listOf(article.codeBarre1, article.codeBarre2).filter { it.isNotBlank() }
                if (codesBarres.isNotEmpty()) {
                    DetailRow(
                        painter = painterResource(id = R.drawable.ic_barcode_scan),
                        text = codesBarres.joinToString(" / ")
                    )
                }
                DetailRow(icon = Icons.Filled.Category, text = article.categorie)
                DetailRow(icon = Icons.Filled.Place, text = article.emplacement)
                DetailRow(
                    icon = Icons.Filled.Inventory2,
                    text = "Quantité système : ${formatQuantity(article.quantiteTheorique)}"
                )
                DetailRow(
                    icon = Icons.AutoMirrored.Filled.FactCheck,
                    text = "Quantité inventaire : " +
                        (article.quantiteReelle?.let { formatQuantity(it) } ?: "-")
                )
                DetailRow(icon = Icons.Filled.Sell, text = "PPV : ${formatQuantity(article.prixUnitaire)} DH")
                DetailRow(
                    icon = Icons.Filled.LocalHospital,
                    text = "PPH : ${formatQuantity(article.pph)} DH"
                )
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
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    DetailRow(text = text) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailRow(painter: androidx.compose.ui.graphics.painter.Painter, text: String) {
    DetailRow(text = text) {
        Icon(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailRow(text: String, icon: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        icon()
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
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
    Box(modifier = Modifier.fillMaxWidth()) {
        StaticLabelTextField(
            value = selected.orEmpty(),
            onValueChange = {},
            label = label,
            containerColor = AlertDialogDefaults.containerColor,
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Ouvrir la liste")
                }
            },
            singleLine = true,
            isError = isError,
            modifier = Modifier.fillMaxWidth()
        )
        // Capte le clic sur tout le champ (pas seulement la flèche) pour ouvrir la liste.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = { expanded = true })
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
