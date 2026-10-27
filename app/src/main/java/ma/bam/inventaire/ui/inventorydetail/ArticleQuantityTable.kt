package ma.bam.inventaire.ui.inventorydetail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.R
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.StatusSuccess
import ma.bam.inventaire.ui.theme.TabularNums
import ma.bam.inventaire.ui.theme.TextMuted
import ma.bam.inventaire.ui.theme.TextPrimary
import ma.bam.inventaire.ui.theme.TextSecondary
import ma.bam.inventaire.util.formatQuantity

/** Tableau Système / Compté / Écart de la carte produit. */
@Composable
internal fun QuantityTable(article: StockArticleEntity, style: ArticleRowStyle) {
    val dividerColor = TextPrimary.copy(alpha = 0.08f)
    Row(
        modifier = Modifier
            .padding(top = 6.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CardSurface.copy(alpha = 0.75f))
            .padding(vertical = 4.dp)
    ) {
        QuantityCell(label = stringResource(R.string.qty_system), modifier = Modifier.weight(1f)) {
            QuantityValue(formatQuantity(article.quantiteTheorique), TextPrimary)
        }
        QuantityCellDivider()
        QuantityCell(
            label = stringResource(R.string.qty_counted),
            modifier = Modifier.weight(1f).leftDivider(dividerColor)
        ) {
            QuantityValue(
                article.quantiteReelle?.let { formatQuantity(it) } ?: "–",
                if (style.notScanned) TextPrimary else BamPrimary
            )
        }
        QuantityCellDivider()
        QuantityCell(
            label = stringResource(if (article.corrige) R.string.qty_corrected else R.string.qty_ecart),
            modifier = Modifier.weight(1f).leftDivider(dividerColor)
        ) {
            EcartValue(article, style)
        }
    }
}

@Composable
private fun EcartValue(article: StockArticleEntity, style: ArticleRowStyle) {
    when {
        style.notScanned -> QuantityValue(
            stringResource(R.string.qty_to_count),
            TextSecondary,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Normal
        )
        style.hasEcart -> QuantityValue(
            article.ecart?.let { formatQuantity(it, showSign = true) } ?: "-",
            style.ecartColor,
            fontWeight = FontWeight.ExtraBold
        )
        else -> QuantityValue(stringResource(R.string.qty_conforme), StatusSuccess, fontSize = 13.5.sp)
    }
}

@Composable
private fun QuantityCell(label: String, modifier: Modifier = Modifier, value: @Composable () -> Unit) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            fontSize = 10.5.sp,
            lineHeight = 13.sp,
            color = TextSecondary,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
        Box(modifier = Modifier.heightIn(min = 19.dp), contentAlignment = Alignment.Center) {
            value()
        }
    }
}

/** Espace de 1dp entre deux cellules ; le trait lui-même est dessiné par [leftDivider]. */
@Composable
private fun QuantityCellDivider() {
    Spacer(modifier = Modifier.width(1.dp))
}

/**
 * Dessine le séparateur fin dans l'espace de 1dp à gauche de la cellule, sur toute sa hauteur.
 * Remplace l'ancien `height(IntrinsicSize.Min)` + `fillMaxHeight()`, dont la mesure intrinsèque
 * doublait le coût de mise en page de chaque carte pendant le défilement.
 */
private fun Modifier.leftDivider(color: Color): Modifier = drawBehind {
    val w = 1.dp.toPx()
    drawRect(color, topLeft = Offset(-w, 0f), size = Size(w, size.height))
}

@Composable
private fun QuantityValue(text: String, color: Color, fontSize: TextUnit = 15.sp, fontWeight: FontWeight = FontWeight.Bold) {
    Text(
        text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        style = LocalTextStyle.current.copy(fontFeatureSettings = TabularNums)
    )
}

/**
 * Article corrigé : mention « Quantité système corrigée par l'inventoriste » toujours affichée,
 * suivie de la remarque éventuelle dans un encadré blanc avec le texte entier.
 */
@Composable
internal fun RemarqueNote(remarque: String?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(13.dp)
        )
        Text(
            stringResource(R.string.article_corrected_note),
            fontSize = 12.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
    }
    if (remarque != null) {
        Row(
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(9.dp))
                .background(CardSurface)
                .padding(horizontal = 9.dp, vertical = 7.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.StickyNote2,
                contentDescription = null,
                tint = BamNavy,
                modifier = Modifier.padding(top = 1.dp).size(14.dp)
            )
            Text(remarque, fontSize = 12.5.sp, lineHeight = 17.5.sp, color = TextPrimary)
        }
    }
}
