package ma.bam.inventaire.ui.inventorydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.R
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamNavyFill
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.BamPrimaryContainer
import ma.bam.inventaire.ui.theme.BorderColor
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.LightDarkPreviews
import ma.bam.inventaire.ui.theme.OnBrand
import ma.bam.inventaire.ui.theme.PreviewSurface
import ma.bam.inventaire.ui.theme.TextPrimary
import ma.bam.inventaire.ui.theme.TextSecondary
import ma.bam.inventaire.ui.theme.TrackColor

/** Bordure des champs et des lignes des feuilles du bas. */
internal val SheetBorder: Color @Composable get() = TrackColor
internal val FieldShape = RoundedCornerShape(10.dp)
val SheetShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)

/** Poignée grise en haut des feuilles du bas. */
@Composable
fun SheetHandle() {
    Box(
        modifier = Modifier
            .padding(top = 10.dp, bottom = 6.dp)
            .size(width = 40.dp, height = 4.dp)
            .clip(CircleShape)
            .background(BorderColor)
    )
}

internal fun withCount(label: String, count: Int?): String = if (count != null) "$label ($count)" else label

internal class DropdownOption(val text: String, val selected: Boolean, val onSelect: () -> Unit)

/** Liste déroulante en lecture seule (coins 10dp) ; bordure et libellé marine quand un choix est actif. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SheetDropdown(label: String, active: Boolean, selectedText: String, options: List<DropdownOption>) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                color = TextPrimary
            ),
            shape = FieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BamNavy,
                unfocusedBorderColor = if (active) BamNavy else SheetBorder,
                focusedLabelColor = BamNavy,
                unfocusedLabelColor = if (active) BamNavy else TextSecondary,
                focusedTrailingIconColor = BamNavy,
                unfocusedTrailingIconColor = if (active) BamNavy else TextSecondary,
                focusedContainerColor = CardSurface,
                unfocusedContainerColor = CardSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        SheetDropdownMenu(expanded = expanded, options = options, onClose = { expanded = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenuBoxScope.SheetDropdownMenu(expanded: Boolean, options: List<DropdownOption>, onClose: () -> Unit) {
    ExposedDropdownMenu(
        expanded = expanded,
        onDismissRequest = onClose,
        containerColor = CardSurface,
        modifier = Modifier.heightIn(max = 320.dp)
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = {
                    Text(
                        option.text,
                        fontSize = 14.sp,
                        fontWeight = if (option.selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (option.selected) BamNavy else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                trailingIcon = if (option.selected) {
                    { Icon(Icons.Filled.Check, contentDescription = null, tint = BamPrimary) }
                } else {
                    null
                },
                onClick = {
                    option.onSelect()
                    onClose()
                },
                modifier = if (option.selected) Modifier.background(BamPrimaryContainer) else Modifier,
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
        }
    }
}

/** Case « Min » / « Max » sous le slider de prix. */
@Composable
internal fun PriceBox(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(FieldShape)
            .border(1.dp, SheetBorder, FieldShape)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(label, fontSize = 11.sp, color = TextSecondary)
        Text(
            stringResource(R.string.price_dh, value.toString()),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = BamNavy,
            maxLines = 1
        )
    }
}

/** Pouce rond marine du slider de prix, avec une zone tactile de 48dp. */
@Composable
internal fun NavyThumb() {
    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(BamNavyFill),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(OnBrand)
            )
        }
    }
}

@LightDarkPreviews
@Composable
private fun SheetDropdownPreview() {
    PreviewSurface(modifier = Modifier.padding(16.dp)) {
        Column {
            SheetDropdown(
                label = stringResource(R.string.filter_zone),
                active = true,
                selectedText = withCount(label = "Rayon A - Étagère 2", count = 5),
                options = emptyList()
            )
            PriceBox(
                label = stringResource(R.string.filter_price_min),
                value = 10,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}
