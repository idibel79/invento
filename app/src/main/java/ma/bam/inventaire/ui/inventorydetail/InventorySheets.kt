package ma.bam.inventaire.ui.inventorydetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamNavyFill
import ma.bam.inventaire.ui.theme.BorderColor
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.ControlBorder
import ma.bam.inventaire.ui.theme.OnBrand
import ma.bam.inventaire.ui.theme.TrackColor
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.BamPrimaryContainer
import ma.bam.inventaire.ui.theme.TextPrimary
import ma.bam.inventaire.ui.theme.TextSecondary
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

private val SheetBorder: Color @Composable get() = TrackColor
private val SegmentBorder: Color @Composable get() = ControlBorder
private val FieldShape = RoundedCornerShape(10.dp)
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

private val statusOptions = listOf(
    ArticleFilter.TOUS to "Tous",
    ArticleFilter.SCANNES to "Inventoriés",
    ArticleFilter.ECARTS to "Écarts",
    ArticleFilter.CORRIGES to "Corrigés",
    ArticleFilter.NON_SCANNES to "Non inventoriés",
    ArticleFilter.NEGATIFS to "Négatifs",
    ArticleFilter.VIDE to "Vide"
)

/**
 * Feuille « Filtrer » : statut, zone, catégorie (listes déroulantes) et plage de prix. Les choix
 * restent en brouillon jusqu'au bouton « N produits », qui les applique tous d'un coup.
 * « Réinitialiser » est appliqué immédiatement (via [onReset]), même si la feuille est ensuite fermée.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    filter: ArticleFilter,
    filterCounts: Map<ArticleFilter, Int>,
    position: String?,
    positions: List<String>,
    positionCounts: Map<String, Int>,
    category: String?,
    categories: List<String>,
    categoryCounts: Map<String, Int>,
    priceRange: ClosedFloatingPointRange<Float>?,
    priceBounds: ClosedFloatingPointRange<Float>,
    previewCount: (ArticleFilter, String?, String?, ClosedFloatingPointRange<Float>?) -> Int,
    onApply: (ArticleFilter, String?, String?, ClosedFloatingPointRange<Float>?) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draftFilter by remember { mutableStateOf(filter) }
    var draftPosition by remember { mutableStateOf(position) }
    var draftCategory by remember { mutableStateOf(category) }
    var draftPrice by remember { mutableStateOf(priceRange ?: priceBounds) }
    val hasPriceRange = priceBounds.start < priceBounds.endInclusive
    // Plage égale aux bornes = pas de filtre de prix.
    val effectivePrice = draftPrice.takeIf { hasPriceRange && it != priceBounds }
    val count = previewCount(draftFilter, draftPosition, draftCategory, effectivePrice)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = SheetShape,
        containerColor = CardSurface,
        dragHandle = { SheetHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Filtrer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = {
                    draftFilter = ArticleFilter.TOUS
                    draftPosition = null
                    draftCategory = null
                    draftPrice = priceBounds
                    onReset()
                }) {
                    Text("Réinitialiser", color = BamPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            SheetDropdown(
                label = "Statut",
                active = draftFilter != ArticleFilter.TOUS,
                selectedText = statusOptions.first { it.first == draftFilter }.let { (value, label) ->
                    withCount(label, filterCounts[value])
                },
                options = statusOptions.map { (value, label) ->
                    DropdownOption(withCount(label, filterCounts[value]), draftFilter == value) { draftFilter = value }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SheetDropdown(
                label = "Zone",
                active = draftPosition != null,
                selectedText = draftPosition?.let { withCount(it, positionCounts[it]) } ?: "Toutes les zones",
                options = listOf(DropdownOption("Toutes les zones", draftPosition == null) { draftPosition = null }) +
                    positions.map { zone ->
                        DropdownOption(withCount(zone, positionCounts[zone]), draftPosition == zone) { draftPosition = zone }
                    }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SheetDropdown(
                label = "Catégorie",
                active = draftCategory != null,
                selectedText = draftCategory?.let { withCount(it, categoryCounts[it]) } ?: "Toutes les catégories",
                options = listOf(DropdownOption("Toutes les catégories", draftCategory == null) { draftCategory = null }) +
                    categories.map { cat ->
                        DropdownOption(withCount(cat, categoryCounts[cat]), draftCategory == cat) { draftCategory = cat }
                    }
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "PRIX UNITAIRE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = TextSecondary
            )
            if (hasPriceRange) {
                val priceStepSize = 10f
                RangeSlider(
                    value = draftPrice,
                    onValueChange = { draftPrice = it },
                    onValueChangeFinished = {
                        val roundedStart = (draftPrice.start / priceStepSize).roundToInt() * priceStepSize
                        val roundedEnd = (draftPrice.endInclusive / priceStepSize).roundToInt() * priceStepSize
                        val clampedStart = roundedStart.coerceIn(priceBounds.start, priceBounds.endInclusive)
                        val clampedEnd = roundedEnd.coerceIn(priceBounds.start, priceBounds.endInclusive)
                        draftPrice = minOf(clampedStart, clampedEnd)..maxOf(clampedStart, clampedEnd)
                    },
                    valueRange = priceBounds,
                    startThumb = { NavyThumb() },
                    endThumb = { NavyThumb() },
                    track = { state ->
                        SliderDefaults.Track(
                            rangeSliderState = state,
                            modifier = Modifier.height(4.dp),
                            colors = SliderDefaults.colors(
                                activeTrackColor = BamNavy,
                                inactiveTrackColor = BamNavy.copy(alpha = 0.15f)
                            ),
                            thumbTrackGapSize = 0.dp
                        )
                    }
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Un seul prix disponible", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PriceBox("Min", floor(draftPrice.start).toInt(), Modifier.weight(1f))
                PriceBox("Max", ceil(draftPrice.endInclusive).toInt(), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SheetBorder),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Annuler", color = BamNavy, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = { onApply(draftFilter, draftPosition, draftCategory, effectivePrice) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BamNavyFill, contentColor = OnBrand),
                    modifier = Modifier.weight(1.6f).height(48.dp)
                ) {
                    Text(
                        "$count produit${if (count > 1) "s" else ""}",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun withCount(label: String, count: Int?): String = if (count != null) "$label ($count)" else label

private class DropdownOption(val text: String, val selected: Boolean, val onSelect: () -> Unit)

/** Liste déroulante en lecture seule (coins 10dp) ; bordure et libellé marine quand un choix est actif. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetDropdown(
    label: String,
    active: Boolean,
    selectedText: String,
    options: List<DropdownOption>
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            textStyle = androidx.compose.ui.text.TextStyle(
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
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
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
                    } else null,
                    onClick = {
                        option.onSelect()
                        expanded = false
                    },
                    modifier = if (option.selected) Modifier.background(BamPrimaryContainer) else Modifier,
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
private fun PriceBox(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(FieldShape)
            .border(1.dp, SheetBorder, FieldShape)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(label, fontSize = 11.sp, color = TextSecondary)
        Text("$value DH", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BamNavy, maxLines = 1)
    }
}

/** Pouce rond marine du slider de prix, avec une zone tactile de 48dp. */
@Composable
private fun NavyThumb() {
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

private class SortRow(
    val label: String,
    val icon: ImageVector,
    val options: List<Pair<String, ArticleSort>>
)

private val sortRows = listOf(
    SortRow("Ordre d'origine", Icons.AutoMirrored.Outlined.Notes, emptyList()),
    SortRow("Titre", Icons.Filled.SortByAlpha, listOf("A-Z" to ArticleSort.TITRE_AZ, "Z-A" to ArticleSort.TITRE_ZA)),
    SortRow("Prix", Icons.Outlined.Payments, listOf("Min" to ArticleSort.PRIX_ASC, "Max" to ArticleSort.PRIX_DESC)),
    SortRow("Quantité", Icons.Outlined.Inventory2, listOf("Min" to ArticleSort.QUANTITE_ASC, "Max" to ArticleSort.QUANTITE_DESC))
)

/** Feuille « Trier par » : une ligne par critère, avec un sélecteur de sens à deux segments. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortSheet(
    sort: ArticleSort,
    onSelect: (ArticleSort) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = SheetShape,
        containerColor = CardSurface,
        dragHandle = { SheetHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Trier par",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            sortRows.forEach { row ->
                val active = if (row.options.isEmpty()) sort == ArticleSort.DEFAUT else row.options.any { it.second == sort }
                val shape = RoundedCornerShape(12.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clip(shape)
                        .background(if (active) BamPrimaryContainer else CardSurface)
                        .border(1.dp, if (active) BamNavy else SheetBorder, shape)
                        .then(
                            if (row.options.isEmpty()) Modifier.clickable { onSelect(ArticleSort.DEFAUT) } else Modifier
                        )
                        .padding(start = 12.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(row.icon, contentDescription = null, tint = BamNavy, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        row.label,
                        fontSize = 13.5.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    if (row.options.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(10.dp))
                        SegmentedDirection(
                            options = row.options,
                            selected = sort,
                            rowActive = active,
                            onSelect = onSelect
                        )
                    } else if (active) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = BamNavy, modifier = Modifier.size(20.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SegmentedDirection(
    options: List<Pair<String, ArticleSort>>,
    selected: ArticleSort,
    rowActive: Boolean,
    onSelect: (ArticleSort) -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .border(1.5.dp, if (rowActive) BamNavy else SegmentBorder, shape)
            .background(CardSurface)
            .padding(1.5.dp)
            .clip(RoundedCornerShape(8.5.dp))
            .height(IntrinsicSize.Min)
    ) {
        // Le 1er segment est le sens croissant (flèche haut), le 2e le sens décroissant (flèche bas).
        options.forEachIndexed { index, (label, value) ->
            // Comme la maquette : pas de trait contre le 2e segment quand c'est lui qui est actif.
            if (index > 0 && value != selected) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(SegmentBorder)
                )
            }
            val isSelected = value == selected
            val contentColor = if (isSelected) OnBrand else BamNavy
            Row(
                modifier = Modifier
                    .size(width = 64.dp, height = 40.dp)
                    .background(if (isSelected) BamNavyFill else CardSurface)
                    .clickable { onSelect(value) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (index == 0) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
