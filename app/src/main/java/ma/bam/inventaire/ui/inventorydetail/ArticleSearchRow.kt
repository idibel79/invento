package ma.bam.inventaire.ui.inventorydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.R
import ma.bam.inventaire.ui.components.rememberTapHaptic
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamPrimaryFill
import ma.bam.inventaire.ui.theme.LightDarkPreviews
import ma.bam.inventaire.ui.theme.OnBrand
import ma.bam.inventaire.ui.theme.PreviewSurface

/** Recherche « Code ou titre » suivie des boutons Filtrer (avec pastille) et Trier. */
@Composable
internal fun ArticleSearchRow(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    activeFieldFilters: Int,
    sortActive: Boolean,
    onOpenFilters: () -> Unit,
    onOpenSort: () -> Unit
) {
    val tapHaptic = rememberTapHaptic()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ArticleSearchField(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            modifier = Modifier.weight(1f)
        )
        SquareToolButton(
            active = activeFieldFilters > 0,
            badgeCount = activeFieldFilters,
            onClick = {
                tapHaptic()
                onOpenFilters()
            }
        ) { tint ->
            Icon(
                imageVector = Icons.Filled.FilterAlt,
                contentDescription = stringResource(R.string.action_filter),
                tint = tint
            )
        }
        SquareToolButton(
            active = sortActive,
            onClick = {
                tapHaptic()
                onOpenSort()
            }
        ) { tint ->
            Icon(
                painter = painterResource(id = R.drawable.ic_sort),
                contentDescription = stringResource(R.string.action_sort),
                modifier = Modifier.size(24.dp),
                tint = tint
            )
        }
    }
}

@Composable
private fun ArticleSearchField(searchQuery: String, onSearchQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text(stringResource(R.string.detail_search_placeholder), color = BamNavy.copy(alpha = 0.6f)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BamNavy) },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.common_clear))
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedTextColor = BamNavy,
            unfocusedTextColor = BamNavy,
            cursorColor = BamNavy
        ),
        modifier = modifier
    )
}

/**
 * Bouton carré (filtre, tri) posé à côté de la recherche. Actif = fond primaire clair et
 * petit point en haut à droite (ou pastille avec le nombre de filtres actifs).
 */
@Composable
private fun SquareToolButton(active: Boolean, onClick: () -> Unit, badgeCount: Int = 0, icon: @Composable (tint: Color) -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    val borderColor = if (active) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.outline
    }
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(shape)
            .background(if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        icon(BamNavy)
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-5).dp, y = 5.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(BamPrimaryFill),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    badgeCount.toString(),
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBrand
                )
            }
        } else if (active) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-9).dp, y = 9.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@LightDarkPreviews
@Composable
private fun ArticleSearchRowPreview() {
    PreviewSurface {
        ArticleSearchRow(
            searchQuery = "",
            onSearchQueryChange = {},
            activeFieldFilters = 2,
            sortActive = true,
            onOpenFilters = {},
            onOpenSort = {}
        )
    }
}
