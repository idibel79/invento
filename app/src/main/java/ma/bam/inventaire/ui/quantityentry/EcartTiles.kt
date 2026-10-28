package ma.bam.inventaire.ui.quantityentry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.R
import ma.bam.inventaire.domain.EcartCalculator
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.StatusEcart
import ma.bam.inventaire.ui.theme.StatusEcartBg
import ma.bam.inventaire.ui.theme.StatusNegative
import ma.bam.inventaire.ui.theme.StatusNegativeBg
import ma.bam.inventaire.ui.theme.TextSecondary
import ma.bam.inventaire.util.formatQuantity

/** Chiffres à chasse fixe des popups de saisie. */
internal val Tabular = TextStyle(fontFeatureSettings = "tnum")

/** Même règle que les cartes produit : rouge à partir de ±5, orange en dessous. */
@Composable
internal fun ecartColors(ecart: Double): Pair<Color, Color> = if (EcartCalculator.isEcartFort(ecart)) {
    StatusEcart to StatusEcartBg
} else {
    StatusNegative to StatusNegativeBg
}

/** Système / Compté / Écart, avec la quantité système d'avant toute correction. */
@Composable
internal fun EcartTiles(quantiteSysteme: Double, quantiteReelle: Double, ecart: Double) {
    val (ecartColor, ecartBg) = ecartColors(ecart)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuantityTile(stringResource(R.string.qty_system), formatQuantity(quantiteSysteme), QuantityBand, TextSecondary, BamNavy)
        QuantityTile(stringResource(R.string.qty_counted), formatQuantity(quantiteReelle), QuantityBand, TextSecondary, BamNavy)
        QuantityTile(stringResource(R.string.qty_ecart), formatQuantity(ecart, showSign = true), ecartBg, ecartColor, ecartColor)
    }
}

@Composable
private fun RowScope.QuantityTile(label: String, value: String, background: Color, labelColor: Color, valueColor: Color) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(ControlShape)
            .background(background)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = labelColor, maxLines = 1)
        Text(
            value,
            fontSize = 22.sp,
            lineHeight = 26.4.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor,
            style = Tabular,
            maxLines = 1
        )
    }
}
