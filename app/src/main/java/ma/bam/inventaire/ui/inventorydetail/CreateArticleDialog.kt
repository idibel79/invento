package ma.bam.inventaire.ui.inventorydetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ma.bam.inventaire.R
import ma.bam.inventaire.data.repository.ImportedArticle
import ma.bam.inventaire.ui.components.DialogButtons
import ma.bam.inventaire.ui.components.StaticLabelTextField
import ma.bam.inventaire.ui.components.fadingEdge

/**
 * Popup de création manuelle d'un produit (page des produits). Tous les champs sont
 * obligatoires sauf le code-barres 2 et la quantité en stock (par défaut 0 si non saisie).
 * Le bouton "Créer" reste toujours actif ; s'il manque un champ obligatoire, celui-ci est
 * mis en évidence en rouge au lieu de bloquer le bouton.
 */
@Composable
fun CreateArticleDialog(
    availableCategories: List<String>,
    availableTvas: List<String>,
    availableZones: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (ImportedArticle) -> Unit
) {
    val form = remember { CreateArticleForm() }

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
                IdentityFields(form)
                FieldSpacer()
                ClassificationFields(form, availableCategories, availableTvas, availableZones)
                FieldSpacer()
                AmountFields(form)
            }
        },
        confirmButton = {
            DialogButtons(
                secondaryLabel = stringResource(R.string.common_cancel),
                onSecondary = onDismiss,
                primaryLabel = stringResource(R.string.create_confirm),
                modifier = Modifier,
                onPrimary = { form.submit(System.currentTimeMillis())?.let(onConfirm) }
            )
        }
    )
}

/** Titre, PID et codes-barres. */
@Composable
private fun IdentityFields(form: CreateArticleForm) {
    FormTextField(
        value = form.designation,
        onValueChange = { form.designation = it },
        label = stringResource(R.string.create_field_title),
        isError = form.showErrors && form.designation.isBlank()
    )
    FieldSpacer()
    FormTextField(
        value = form.codeArticle,
        onValueChange = { form.codeArticle = it },
        label = stringResource(R.string.create_field_pid),
        isError = form.showErrors && form.codeArticle.isBlank()
    )
    FieldSpacer()
    FormTextField(
        value = form.codeBarre1,
        onValueChange = { form.codeBarre1 = it },
        label = stringResource(R.string.create_field_barcode1),
        isError = form.showErrors && form.codeBarre1.isBlank()
    )
    FieldSpacer()
    FormTextField(
        value = form.codeBarre2,
        onValueChange = { form.codeBarre2 = it },
        label = stringResource(R.string.create_field_barcode2)
    )
}

/** Catégorie, TVA et zone, choisies parmi les valeurs déjà présentes dans l'inventaire. */
@Composable
private fun ClassificationFields(
    form: CreateArticleForm,
    availableCategories: List<String>,
    availableTvas: List<String>,
    availableZones: List<String>
) {
    DropdownField(
        label = stringResource(R.string.field_category),
        options = availableCategories,
        selected = form.categorie,
        onSelect = { form.categorie = it },
        isError = form.showErrors && form.categorie == null
    )
    FieldSpacer()
    DropdownField(
        label = stringResource(R.string.field_tva),
        options = availableTvas,
        selected = form.tva,
        onSelect = { form.tva = it },
        isError = form.showErrors && form.tva == null
    )
    FieldSpacer()
    DropdownField(
        label = stringResource(R.string.field_zone),
        options = availableZones,
        selected = form.zone,
        onSelect = { form.zone = it },
        isError = form.showErrors && form.zone == null
    )
}

/** Quantités et prix, au clavier numérique. */
@Composable
private fun AmountFields(form: CreateArticleForm) {
    FormTextField(
        value = form.quantiteText,
        onValueChange = { form.quantiteText = it },
        label = stringResource(R.string.create_field_system_qty),
        decimal = true
    )
    FieldSpacer()
    FormTextField(
        value = form.quantiteInventaireText,
        onValueChange = { form.quantiteInventaireText = it },
        label = stringResource(R.string.create_field_counted_qty),
        decimal = true
    )
    FieldSpacer()
    FormTextField(
        value = form.prixText,
        onValueChange = { form.prixText = it },
        label = stringResource(R.string.field_ppv),
        decimal = true,
        isError = form.showErrors && form.prix == null
    )
    FieldSpacer()
    FormTextField(
        value = form.pphText,
        onValueChange = { form.pphText = it },
        label = stringResource(R.string.field_pph),
        decimal = true,
        isError = form.showErrors && form.pph == null
    )
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    decimal: Boolean = false,
    isError: Boolean = false
) {
    StaticLabelTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        containerColor = AlertDialogDefaults.containerColor,
        singleLine = true,
        keyboardOptions = if (decimal) KeyboardOptions(keyboardType = KeyboardType.Decimal) else KeyboardOptions.Default,
        isError = isError,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FieldSpacer() {
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
private fun DropdownField(label: String, options: List<String>, selected: String?, onSelect: (String) -> Unit, isError: Boolean = false) {
    var expanded by remember { mutableStateOf(false) }
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
        DropdownValue(selected = selected, isError = isError, onClick = { expanded = true })
        DropdownOptions(
            expanded = expanded,
            options = options,
            onSelect = {
                onSelect(it)
                expanded = false
            },
            onDismiss = { expanded = false }
        )
    }
}

/**
 * Capte le clic sur tout le champ (pas seulement la flèche) pour ouvrir la liste, et affiche la
 * valeur sélectionnée en glissable pour pouvoir lire le texte en entier.
 */
@Composable
private fun BoxScope.DropdownValue(selected: String?, isError: Boolean, onClick: () -> Unit) {
    val valueScrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .matchParentSize()
            .clickable(onClick = onClick)
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
}

@Composable
private fun DropdownOptions(expanded: Boolean, options: List<String>, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.heightIn(max = 280.dp)
    ) {
        if (options.isEmpty()) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.create_no_value), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                onClick = onDismiss,
                enabled = false
            )
        }
        options.forEach { option ->
            DropdownMenuItem(
                text = { Text(option) },
                onClick = { onSelect(option) }
            )
        }
    }
}
