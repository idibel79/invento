package ma.bam.inventaire.ui.inventorydetail

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ma.bam.inventaire.R
import ma.bam.inventaire.domain.ArticleFilter
import ma.bam.inventaire.ui.components.PillShape
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamNavyFill
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.LightDarkPreviews
import ma.bam.inventaire.ui.theme.OnBrand
import ma.bam.inventaire.ui.theme.PreviewSurface
import ma.bam.inventaire.ui.theme.TabularNums

/** Filtres rapides de la page produits, dans l'ordre d'affichage des puces. */
private val quickFilters: List<Pair<ArticleFilter, Int>> = listOf(
    ArticleFilter.TOUS to R.string.filter_all,
    ArticleFilter.SCANNES to R.string.filter_counted,
    ArticleFilter.ECARTS to R.string.filter_ecarts,
    ArticleFilter.CORRIGES to R.string.filter_corrected,
    ArticleFilter.NON_SCANNES to R.string.filter_not_counted,
    ArticleFilter.NEGATIFS to R.string.filter_negative,
    ArticleFilter.VIDE to R.string.filter_empty_chip
)

/** Rangée de puces défilante horizontalement, chacune avec son compteur. */
@Composable
internal fun QuickFilterRow(selected: ArticleFilter, filterCounts: Map<ArticleFilter, Int>, onSelect: (ArticleFilter) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        quickFilters.forEach { (value, labelRes) ->
            QuickFilterChip(
                labelRes = labelRes,
                count = filterCounts[value],
                selected = selected == value,
                onClick = { onSelect(value) }
            )
        }
    }
}

/** Filtre rapide en pilule ; sélectionné = fond foncé, texte blanc. Affiche le compteur s'il est connu. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickFilterChip(@StringRes labelRes: Int, count: Int?, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(stringResource(labelRes), fontWeight = FontWeight.SemiBold)
            if (count != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    count.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = TabularNums),
                    fontWeight = FontWeight.Bold,
                    color = if (selected) OnBrand.copy(alpha = 0.7f) else BamNavy.copy(alpha = 0.7f)
                )
            }
        },
        shape = PillShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = CardSurface,
            labelColor = BamNavy,
            selectedContainerColor = BamNavyFill,
            selectedLabelColor = OnBrand
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = BamNavyFill
        )
    )
}

@LightDarkPreviews
@Composable
private fun QuickFilterRowPreview() {
    PreviewSurface {
        QuickFilterRow(
            selected = ArticleFilter.ECARTS,
            filterCounts = PreviewArticles.uiState.stats.filterCounts,
            onSelect = {}
        )
    }
}
