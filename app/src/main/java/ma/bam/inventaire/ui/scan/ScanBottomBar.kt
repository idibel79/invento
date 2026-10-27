package ma.bam.inventaire.ui.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.R
import ma.bam.inventaire.data.repository.SessionCounters
import ma.bam.inventaire.ui.components.DialogButtons
import ma.bam.inventaire.ui.components.PillShape
import ma.bam.inventaire.ui.components.pluralLabel
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.LightDarkPreviews
import ma.bam.inventaire.ui.theme.PreviewSurface
import ma.bam.inventaire.ui.theme.StatusEcart
import ma.bam.inventaire.ui.theme.StatusEcartBg
import ma.bam.inventaire.ui.theme.TabularNums
import ma.bam.inventaire.ui.theme.TextPrimary
import ma.bam.inventaire.ui.theme.TextSecondary
import ma.bam.inventaire.ui.theme.TrackColor

/** Opacité de l'ombre de la barre du bas. */
private const val SHADOW_ALPHA = 0.3f

/**
 * Barre du bas : progression, écarts à valider et actions (la même en scan et en saisie
 * manuelle). Sa hauteur est remontée par [onHeightChange] pour décaler le contenu au-dessus.
 */
@Composable
internal fun ScanBottomBar(
    counters: SessionCounters,
    manualMode: Boolean,
    onToggleManualMode: () -> Unit,
    onFinish: () -> Unit,
    onHeightChange: (Dp) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { onHeightChange(with(density) { it.height.toDp() }) }
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = TextPrimary.copy(alpha = SHADOW_ALPHA),
                spotColor = TextPrimary.copy(alpha = SHADOW_ALPHA)
            )
            .clip(shape)
            .background(CardSurface)
            .navigationBarsPadding()
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ScanProgressRow(counters)
        DialogButtons(
            secondaryLabel = stringResource(if (manualMode) R.string.scan_mode_camera else R.string.scan_mode_manual),
            secondaryIcon = if (manualMode) Icons.Outlined.QrCodeScanner else Icons.Filled.Keyboard,
            onSecondary = onToggleManualMode,
            primaryLabel = stringResource(R.string.scan_finish),
            onPrimary = onFinish,
            modifier = Modifier
        )
    }
}

/** « Scannés N / T », barre de progression et pastille « N écarts à valider ». */
@Composable
private fun ScanProgressRow(counters: SessionCounters) {
    val progress = if (counters.total > 0) counters.scanned.toFloat() / counters.total.toFloat() else 0f
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val scannedLabel = stringResource(R.string.scan_scanned)
        Text(
            buildAnnotatedString {
                append(scannedLabel)
                append(" ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = BamNavy)) {
                    append("${counters.scanned} / ${counters.total}")
                }
            },
            fontSize = 12.5.sp,
            color = TextSecondary,
            maxLines = 1,
            softWrap = false,
            style = LocalTextStyle.current.copy(fontFeatureSettings = TabularNums)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(TrackColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(BamPrimary)
            )
        }
        if (counters.pendingEcarts > 0) {
            Text(
                pluralLabel(R.plurals.scan_pending_ecarts, counters.pendingEcarts),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = StatusEcart,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .clip(PillShape)
                    .background(StatusEcartBg)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@LightDarkPreviews
@Composable
private fun ScanBottomBarPreview() {
    PreviewSurface {
        ScanBottomBar(
            counters = SessionCounters(
                total = 342,
                scanned = 128,
                ecarts = 9,
                pendingEcarts = 3,
                negativeStock = 0,
                emptyStock = 0
            ),
            manualMode = false,
            onToggleManualMode = {},
            onFinish = {},
            onHeightChange = {}
        )
    }
}
