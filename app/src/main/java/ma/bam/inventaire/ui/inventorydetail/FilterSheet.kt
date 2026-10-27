package ma.bam.inventaire.ui.inventorydetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt
import ma.bam.inventaire.R
import ma.bam.inventaire.domain.ArticleFilter
import ma.bam.inventaire.ui.components.DialogButtons
import ma.bam.inventaire.ui.components.pluralLabel
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.LightDarkPreviews
import ma.bam.inventaire.ui.theme.PreviewSurface
import ma.bam.inventaire.ui.theme.TextPrimary
import ma.bam.inventaire.ui.theme.TextSecondary

/** Pas d'arrondi des bornes du slider de prix, à la fin du glissement. */
private const val PRICE_STEP = 10f

/** Statuts de la liste « Statut », dans l'ordre d'affichage. */
private val statusOptions: List<Pair<ArticleFilter, Int>> = listOf(
    ArticleFilter.TOUS to R.string.filter_all,
    ArticleFilter.SCANNES to R.string.filter_counted,
    ArticleFilter.ECARTS to R.string.filter_ecarts,
    ArticleFilter.CORRIGES to R.string.filter_corrected,
    ArticleFilter.NON_SCANNES to R.string.filter_not_counted,
    ArticleFilter.NEGATIFS to R.string.filter_negative,
    ArticleFilter.VIDE to R.string.filter_empty
)

/** Brouillon des choix de la feuille « Filtrer », appliqué d'un coup par le bouton « N produits ». */
@Stable
private class FilterDraft(
    filter: ArticleFilter,
    position: String?,
    category: String?,
    price: ClosedFloatingPointRange<Float>
) {
    var filter by mutableStateOf(filter)
    var position by mutableStateOf(position)
    var category by mutableStateOf(category)
    var price by mutableStateOf(price)
}

/**
 * Feuille « Filtrer » : statut, zone, catégorie (listes déroulantes) et plage de prix. Les choix
 * restent en brouillon jusqu'au bouton « N produits », qui les applique tous d'un coup.
 * « Réinitialiser » est appliqué immédiatement (via [onReset]), même si la feuille est ensuite fermée.
 * Le nombre affiché part de [appliedCount] (produits affichés avec les filtres appliqués) puis suit
 * [previewCount] calculé sur le brouillon.
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
    appliedCount: Int,
    previewCount: suspend (ArticleFilter, String?, String?, ClosedFloatingPointRange<Float>?) -> Int,
    onApply: (ArticleFilter, String?, String?, ClosedFloatingPointRange<Float>?) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val draft = remember { FilterDraft(filter, position, category, priceRange ?: priceBounds) }
    val hasPriceRange = priceBounds.start < priceBounds.endInclusive
    // Plage égale aux bornes = pas de filtre de prix.
    val effectivePrice = draft.price.takeIf { hasPriceRange && it != priceBounds }
    // Recalculé hors du thread principal à chaque changement du brouillon (le calcul précédent
    // est annulé, par ex. pendant le glissement du curseur de prix).
    var count by remember { mutableIntStateOf(appliedCount) }
    LaunchedEffect(draft.filter, draft.position, draft.category, effectivePrice) {
        count = previewCount(draft.filter, draft.position, draft.category, effectivePrice)
    }

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
            FilterSheetHeader(onReset = {
                draft.filter = ArticleFilter.TOUS
                draft.position = null
                draft.category = null
                draft.price = priceBounds
                onReset()
            })
            Spacer(modifier = Modifier.height(8.dp))
            FilterDropdowns(
                draft = draft,
                filterCounts = filterCounts,
                positions = positions,
                positionCounts = positionCounts,
                categories = categories,
                categoryCounts = categoryCounts
            )
            Spacer(modifier = Modifier.height(20.dp))
            PriceRangeSection(draft = draft, priceBounds = priceBounds, hasPriceRange = hasPriceRange)
            Spacer(modifier = Modifier.height(24.dp))
            DialogButtons(
                secondaryLabel = stringResource(R.string.common_cancel),
                onSecondary = onDismiss,
                primaryLabel = pluralLabel(R.plurals.products_count, count),
                onPrimary = { onApply(draft.filter, draft.position, draft.category, effectivePrice) },
                modifier = Modifier
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FilterSheetHeader(onReset: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.action_filter),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onReset) {
            Text(stringResource(R.string.filter_reset), color = BamPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Listes déroulantes Statut, Zone et Catégorie, avec le nombre de produits de chaque choix. */
@Composable
private fun FilterDropdowns(
    draft: FilterDraft,
    filterCounts: Map<ArticleFilter, Int>,
    positions: List<String>,
    positionCounts: Map<String, Int>,
    categories: List<String>,
    categoryCounts: Map<String, Int>
) {
    val statusLabels = statusOptions.map { (value, labelRes) ->
        value to withCount(stringResource(labelRes), filterCounts[value])
    }
    SheetDropdown(
        label = stringResource(R.string.filter_status),
        active = draft.filter != ArticleFilter.TOUS,
        selectedText = statusLabels.first { it.first == draft.filter }.second,
        options = statusLabels.map { (value, text) ->
            DropdownOption(text, draft.filter == value) { draft.filter = value }
        }
    )
    Spacer(modifier = Modifier.height(12.dp))
    val allZones = stringResource(R.string.filter_all_zones)
    SheetDropdown(
        label = stringResource(R.string.filter_zone),
        active = draft.position != null,
        selectedText = draft.position?.let { withCount(it, positionCounts[it]) } ?: allZones,
        options = listOf(DropdownOption(allZones, draft.position == null) { draft.position = null }) +
            positions.map { zone ->
                DropdownOption(withCount(zone, positionCounts[zone]), draft.position == zone) { draft.position = zone }
            }
    )
    Spacer(modifier = Modifier.height(12.dp))
    val allCategories = stringResource(R.string.filter_all_categories)
    SheetDropdown(
        label = stringResource(R.string.filter_category),
        active = draft.category != null,
        selectedText = draft.category?.let { withCount(it, categoryCounts[it]) } ?: allCategories,
        options = listOf(DropdownOption(allCategories, draft.category == null) { draft.category = null }) +
            categories.map { cat ->
                DropdownOption(withCount(cat, categoryCounts[cat]), draft.category == cat) { draft.category = cat }
            }
    )
}

/** « PRIX UNITAIRE » : slider à deux pouces (arrondi à 10 DH au lâcher) et cases Min / Max. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriceRangeSection(draft: FilterDraft, priceBounds: ClosedFloatingPointRange<Float>, hasPriceRange: Boolean) {
    Text(
        stringResource(R.string.filter_unit_price),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp,
        color = TextSecondary
    )
    if (hasPriceRange) {
        RangeSlider(
            value = draft.price,
            onValueChange = { draft.price = it },
            onValueChangeFinished = {
                val roundedStart = (draft.price.start / PRICE_STEP).roundToInt() * PRICE_STEP
                val roundedEnd = (draft.price.endInclusive / PRICE_STEP).roundToInt() * PRICE_STEP
                val clampedStart = roundedStart.coerceIn(priceBounds.start, priceBounds.endInclusive)
                val clampedEnd = roundedEnd.coerceIn(priceBounds.start, priceBounds.endInclusive)
                draft.price = minOf(clampedStart, clampedEnd)..maxOf(clampedStart, clampedEnd)
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
        Text(stringResource(R.string.filter_single_price), fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(8.dp))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PriceBox(stringResource(R.string.filter_price_min), floor(draft.price.start).toInt(), Modifier.weight(1f))
        PriceBox(stringResource(R.string.filter_price_max), ceil(draft.price.endInclusive).toInt(), Modifier.weight(1f))
    }
}

@LightDarkPreviews
@Composable
private fun PriceRangeSectionPreview() {
    PreviewSurface(modifier = Modifier.padding(20.dp)) {
        Column {
            PriceRangeSection(
                draft = remember { FilterDraft(filter = ArticleFilter.TOUS, position = null, category = null, price = 20f..140f) },
                priceBounds = 0f..200f,
                hasPriceRange = true
            )
        }
    }
}
