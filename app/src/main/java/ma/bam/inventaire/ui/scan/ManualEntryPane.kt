package ma.bam.inventaire.ui.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.R
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.ui.components.PrimaryActionButton
import ma.bam.inventaire.ui.components.pluralLabel
import ma.bam.inventaire.ui.inventorydetail.ArticleRow
import ma.bam.inventaire.ui.inventorydetail.PreviewArticles
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.LightDarkPreviews
import ma.bam.inventaire.ui.theme.PreviewSurface
import ma.bam.inventaire.ui.theme.TabularNums
import ma.bam.inventaire.ui.theme.TextMuted
import ma.bam.inventaire.ui.theme.TextPrimary
import ma.bam.inventaire.ui.theme.TextSecondary
import ma.bam.inventaire.ui.theme.TrackColor

/** Message et bouton affichés quand la caméra n'est pas autorisée. */
@Composable
internal fun CameraPermissionPane(bottomPadding: Dp, onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 64.dp, bottom = bottomPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.scan_camera_required))
        Spacer(modifier = Modifier.height(8.dp))
        PrimaryActionButton(
            stringResource(R.string.scan_allow_camera),
            onRequestPermission,
            Modifier.fillMaxWidth()
        )
    }
}

/** Saisie manuelle : champ toujours en haut, résultats en cartes produit de la liste. */
@Composable
internal fun ManualEntryPane(
    query: String,
    results: List<StockArticleEntity>,
    bottomPadding: Dp,
    onQueryChange: (String) -> Unit,
    onSelect: (StockArticleEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 64.dp)
    ) {
        ManualSearchField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 4.dp)
        )
        val hintModifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp)
        when {
            query.isBlank() -> ManualHint(stringResource(R.string.scan_manual_hint), hintModifier)
            results.isEmpty() -> ManualHint(stringResource(R.string.scan_no_result), hintModifier)
            else -> {
                ResultsCount(results.size, hintModifier)
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = 14.dp,
                        end = 14.dp,
                        top = 8.dp,
                        bottom = bottomPadding + 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results, key = { it.id }) { article ->
                        ArticleRow(
                            item = article,
                            locked = false,
                            onEdit = {},
                            onShowDetail = {},
                            onSelect = { onSelect(article) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualHint(text: String, modifier: Modifier) {
    Text(text, fontSize = 12.sp, color = TextSecondary, modifier = modifier)
}

/** « N produits trouvés », nombre en gras. */
@Composable
private fun ResultsCount(count: Int, modifier: Modifier) {
    val label = pluralLabel(R.plurals.scan_products_found, count)
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = BamNavy)) {
                append("$count")
            }
            append(" ")
            append(label)
        },
        fontSize = 12.sp,
        color = TextSecondary,
        style = LocalTextStyle.current.copy(fontFeatureSettings = TabularNums),
        modifier = modifier
    )
}

/**
 * Champ de recherche de la saisie manuelle : 46dp, coins 12dp, bordure marine 2dp au focus.
 * Le clavier s'ouvre dès l'entrée en saisie manuelle.
 */
@Composable
private fun ManualSearchField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
        cursorBrush = SolidColor(BamPrimary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .focusRequester(focusRequester),
        decorationBox = { innerTextField ->
            SearchFieldDecoration(
                value = value,
                focused = focused,
                onClear = { onValueChange("") },
                innerTextField = innerTextField
            )
        }
    )
}

@Composable
private fun SearchFieldDecoration(value: String, focused: Boolean, onClear: () -> Unit, innerTextField: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(CardSurface)
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = if (focused) BamNavy else TrackColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp)
    ) {
        Icon(
            Icons.Filled.Search,
            contentDescription = null,
            tint = if (focused) BamNavy else TextMuted,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text(
                    stringResource(R.string.scan_search_placeholder),
                    fontSize = 14.sp,
                    color = TextMuted,
                    maxLines = 1
                )
            }
            innerTextField()
        }
        if (value.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onClear),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.common_clear),
                    tint = TextSecondary,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@LightDarkPreviews
@Composable
private fun ManualEntryPanePreview() {
    PreviewSurface {
        ManualEntryPane(
            query = "vit",
            results = listOf(PreviewArticles.corrige, PreviewArticles.nonCompte),
            bottomPadding = 0.dp,
            onQueryChange = {},
            onSelect = {}
        )
    }
}
