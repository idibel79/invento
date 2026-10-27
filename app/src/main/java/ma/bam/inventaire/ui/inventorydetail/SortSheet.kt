package ma.bam.inventaire.ui.inventorydetail

import androidx.annotation.StringRes
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.R
import ma.bam.inventaire.domain.ArticleSort
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamNavyFill
import ma.bam.inventaire.ui.theme.BamPrimaryContainer
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.ControlBorder
import ma.bam.inventaire.ui.theme.LightDarkPreviews
import ma.bam.inventaire.ui.theme.OnBrand
import ma.bam.inventaire.ui.theme.PreviewSurface
import ma.bam.inventaire.ui.theme.TextPrimary

private val SegmentBorder: Color @Composable get() = ControlBorder

private class SortRow(
    @StringRes val label: Int,
    val icon: ImageVector,
    /** Sens croissant puis décroissant ; vide pour « Ordre d'origine ». */
    val options: List<Pair<Int, ArticleSort>>
)

private val sortRows = listOf(
    SortRow(R.string.sort_original, Icons.AutoMirrored.Outlined.Notes, emptyList()),
    SortRow(
        R.string.sort_title,
        Icons.Filled.SortByAlpha,
        listOf(R.string.sort_az to ArticleSort.TITRE_AZ, R.string.sort_za to ArticleSort.TITRE_ZA)
    ),
    SortRow(
        R.string.sort_price,
        Icons.Outlined.Payments,
        listOf(R.string.sort_min to ArticleSort.PRIX_ASC, R.string.sort_max to ArticleSort.PRIX_DESC)
    ),
    SortRow(
        R.string.sort_quantity,
        Icons.Outlined.Inventory2,
        listOf(R.string.sort_min to ArticleSort.QUANTITE_ASC, R.string.sort_max to ArticleSort.QUANTITE_DESC)
    )
)

/** Feuille « Trier par » : une ligne par critère, avec un sélecteur de sens à deux segments. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortSheet(sort: ArticleSort, onSelect: (ArticleSort) -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = SheetShape,
        containerColor = CardSurface,
        dragHandle = { SheetHandle() }
    ) {
        SortSheetContent(sort = sort, onSelect = onSelect)
    }
}

@Composable
private fun SortSheetContent(sort: ArticleSort, onSelect: (ArticleSort) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            stringResource(R.string.sort_title_sheet),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )
        sortRows.forEach { row -> SortRowItem(row = row, sort = sort, onSelect = onSelect) }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SortRowItem(row: SortRow, sort: ArticleSort, onSelect: (ArticleSort) -> Unit) {
    val active = if (row.options.isEmpty()) {
        sort == ArticleSort.DEFAUT
    } else {
        row.options.any { it.second == sort }
    }
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
            stringResource(row.label),
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
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = BamNavy,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SegmentedDirection(
    options: List<Pair<Int, ArticleSort>>,
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
        options.forEachIndexed { index, (labelRes, value) ->
            // Comme la maquette : pas de trait contre le 2e segment quand c'est lui qui est actif.
            if (index > 0 && value != selected) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(SegmentBorder)
                )
            }
            DirectionSegment(
                label = stringResource(labelRes),
                ascending = index == 0,
                isSelected = value == selected,
                onClick = { onSelect(value) }
            )
        }
    }
}

@Composable
private fun DirectionSegment(label: String, ascending: Boolean, isSelected: Boolean, onClick: () -> Unit) {
    val contentColor = if (isSelected) OnBrand else BamNavy
    Row(
        modifier = Modifier
            .size(width = 64.dp, height = 40.dp)
            .background(if (isSelected) BamNavyFill else CardSurface)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (ascending) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
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

@LightDarkPreviews
@Composable
private fun SortSheetContentPreview() {
    PreviewSurface {
        Box(modifier = Modifier.background(CardSurface)) {
            SortSheetContent(sort = ArticleSort.PRIX_DESC, onSelect = {})
        }
    }
}
