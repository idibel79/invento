package ma.bam.inventaire.ui.inventorydetail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.R
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.domain.EcartCalculator
import ma.bam.inventaire.ui.components.EllipsisScrollBox
import ma.bam.inventaire.ui.components.rememberLongPressHaptic
import ma.bam.inventaire.ui.components.rememberTapHaptic
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.LightDarkPreviews
import ma.bam.inventaire.ui.theme.PreviewSurface
import ma.bam.inventaire.ui.theme.StatusEcart
import ma.bam.inventaire.ui.theme.StatusEcartBg
import ma.bam.inventaire.ui.theme.StatusNegative
import ma.bam.inventaire.ui.theme.StatusNegativeBg
import ma.bam.inventaire.ui.theme.StatusPendingBg
import ma.bam.inventaire.ui.theme.StatusSuccess
import ma.bam.inventaire.ui.theme.StatusSuccessBg
import ma.bam.inventaire.ui.theme.TabularNums
import ma.bam.inventaire.ui.theme.TextMuted
import ma.bam.inventaire.ui.theme.TextSecondary
import ma.bam.inventaire.util.formatQuantity

/**
 * Carte produit de la liste d'inventaire. Avec [onSelect] (saisie manuelle du scan), un appui
 * sélectionne le produit : pas d'appui long ni de crayon.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ArticleRow(
    item: StockArticleEntity,
    locked: Boolean,
    onEdit: () -> Unit,
    onShowDetail: () -> Unit,
    onSelect: (() -> Unit)? = null,
    /** Article corrigé : affiche la remarque (ou la mention de correction) sous les quantités. */
    showRemarque: Boolean = false
) {
    val tapHaptic = rememberTapHaptic()
    val longPressHaptic = rememberLongPressHaptic()
    val style = articleRowStyle(item)
    val clickModifier = if (onSelect != null) {
        Modifier.clickable(onClick = onSelect)
    } else {
        Modifier.combinedClickable(
            onClick = {
                if (style.notScanned && !locked) {
                    tapHaptic()
                    onEdit()
                }
            },
            onLongClick = {
                longPressHaptic()
                onShowDetail()
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().then(clickModifier),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = style.cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            ArticleTitleLine(
                title = item.designation.ifBlank { item.codeArticle },
                style = style,
                showEdit = !style.notScanned && !locked && onSelect == null,
                onEdit = {
                    tapHaptic()
                    onEdit()
                }
            )
            ArticlePriceLine(item, style)
            ArticleZoneLine(item.emplacement, style)
            QuantityTable(item, style)
            if (showRemarque) {
                RemarqueNote(item.remarque, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

/**
 * Couleurs de la carte selon l'état : rouge si |écart| >= 5, orange si écart plus petit (entre -5
 * et +5 exclus), vert si conforme, gris si non inventorié.
 */
internal data class ArticleRowStyle(
    val notScanned: Boolean,
    val hasEcart: Boolean,
    val ecartColor: Color,
    val cardColor: Color,
    val titleColor: Color,
    val infoTextColor: Color,
    val infoWeight: FontWeight,
    val infoIconColor: Color
)

@Composable
private fun articleRowStyle(article: StockArticleEntity): ArticleRowStyle {
    val hasEcart = EcartCalculator.hasEcart(article.ecart)
    val notScanned = article.quantiteReelle == null
    val ecartFaible = hasEcart && !EcartCalculator.isEcartFort(article.ecart)
    val ecartColor = if (ecartFaible) StatusNegative else StatusEcart
    return ArticleRowStyle(
        notScanned = notScanned,
        hasEcart = hasEcart,
        ecartColor = ecartColor,
        cardColor = when {
            notScanned -> StatusPendingBg
            ecartFaible -> StatusNegativeBg
            hasEcart -> StatusEcartBg
            else -> StatusSuccessBg
        },
        titleColor = when {
            notScanned -> TextSecondary
            hasEcart -> ecartColor
            else -> StatusSuccess
        },
        infoTextColor = if (notScanned) TextSecondary else BamNavy,
        infoWeight = if (notScanned) FontWeight.Normal else FontWeight.SemiBold,
        infoIconColor = if (notScanned) TextMuted else TextSecondary
    )
}

/**
 * Ligne 1 : titre qui défile + stylo (absent si non inventorié). Hauteur fixe pour que toutes
 * les cartes aient la même hauteur, avec ou sans stylo.
 */
@Composable
private fun ArticleTitleLine(title: String, style: ArticleRowStyle, showEdit: Boolean, onEdit: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.heightIn(min = 24.dp)
    ) {
        EllipsisScrollBox(
            scrollState = rememberScrollState(),
            background = style.cardColor,
            ellipsisColor = style.titleColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = style.titleColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip
            )
        }
        if (showEdit) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onEdit),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.article_edit_quantity),
                    tint = style.titleColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/** Ligne 2 : prix · code-barres. */
@Composable
private fun ArticlePriceLine(article: StockArticleEntity, style: ArticleRowStyle) {
    val codesBarres = listOf(article.codeBarre1, article.codeBarre2).filter { it.isNotBlank() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 2.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Sell,
            contentDescription = null,
            tint = style.infoIconColor,
            modifier = Modifier.size(14.dp)
        )
        ArticleInfoText(
            stringResource(R.string.price_dh, formatQuantity(article.prixUnitaire)),
            style.infoTextColor,
            style.infoWeight
        )
        if (codesBarres.isNotEmpty()) {
            Text("·", fontSize = 12.5.sp, color = TextMuted, maxLines = 1, softWrap = false)
            Icon(
                painter = painterResource(id = R.drawable.ic_barcode_scan),
                contentDescription = null,
                tint = style.infoIconColor,
                modifier = Modifier.size(14.dp)
            )
            ArticleInfoText(
                codesBarres.joinToString(" / "),
                style.infoTextColor,
                style.infoWeight,
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }
}

/** Ligne 3 : zone, sur sa propre ligne qui défile quand elle est longue. */
@Composable
private fun ArticleZoneLine(zone: String, style: ArticleRowStyle) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Place,
            contentDescription = null,
            tint = style.infoIconColor,
            modifier = Modifier.size(14.dp)
        )
        EllipsisScrollBox(
            scrollState = rememberScrollState(),
            background = style.cardColor,
            ellipsisColor = style.infoTextColor,
            fontSize = 12.5.sp,
            fontWeight = style.infoWeight,
            modifier = Modifier.weight(1f)
        ) {
            ArticleInfoText(zone, style.infoTextColor, style.infoWeight)
        }
    }
}

@Composable
private fun ArticleInfoText(text: String, color: Color, fontWeight: FontWeight, modifier: Modifier = Modifier) {
    Text(
        text,
        fontSize = 12.5.sp,
        lineHeight = 17.sp,
        color = color,
        fontWeight = fontWeight,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        style = LocalTextStyle.current.copy(fontFeatureSettings = TabularNums),
        modifier = modifier
    )
}

@LightDarkPreviews
@Composable
private fun ArticleRowPreview() {
    PreviewSurface {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PreviewArticles.all.forEach { article ->
                ArticleRow(
                    item = article,
                    locked = false,
                    onEdit = {},
                    onShowDetail = {},
                    showRemarque = article.corrige
                )
            }
        }
    }
}
