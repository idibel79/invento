package ma.bam.inventaire.ui.inventorydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.R
import ma.bam.inventaire.ui.components.PillShape
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.LightDarkPreviews
import ma.bam.inventaire.ui.theme.PreviewSurface
import ma.bam.inventaire.ui.theme.SelectedCardBg
import ma.bam.inventaire.ui.theme.TabularNums

private const val PERCENT = 100

/**
 * Carte d'avancement (fond bleu clair #CEDEFF comme les cartes En cours) : articles inventoriés
 * / total, pourcentage et barre.
 */
@Composable
internal fun ProgressCard(scanned: Int, total: Int, modifier: Modifier = Modifier) {
    val progress = if (total > 0) scanned.toFloat() / total.toFloat() else 0f
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SelectedCardBg)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.detail_progress),
                    style = MaterialTheme.typography.labelMedium,
                    color = BamNavy.copy(alpha = 0.7f)
                )
                Text(
                    "$scanned / $total",
                    style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = TabularNums),
                    fontWeight = FontWeight.Bold,
                    color = BamPrimary
                )
            }
            Text(
                "${(progress * PERCENT).toInt()} %",
                fontSize = 30.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BamPrimary,
                style = LocalTextStyle.current.copy(fontFeatureSettings = TabularNums)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(PillShape),
            color = BamPrimary,
            trackColor = CardSurface,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
    }
}

@LightDarkPreviews
@Composable
private fun ProgressCardPreview() {
    PreviewSurface {
        ProgressCard(scanned = 128, total = 342, modifier = Modifier.padding(16.dp))
    }
}
