package ma.bam.inventaire.ui.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ma.bam.inventaire.R
import ma.bam.inventaire.ui.components.rememberTapHaptic
import ma.bam.inventaire.ui.theme.LightDarkPreviews
import ma.bam.inventaire.ui.theme.PreviewSurface

/** Opacité du fond blanc des boutons posés sur la caméra. */
private const val OVERLAY_ALPHA = 0.14f

/** Barre du haut transparente, posée sur la caméra : retour, titre et flash (caméra seulement). */
@Composable
internal fun ScanTopBar(onCamera: Boolean, torchOn: Boolean, onBack: () -> Unit, onToggleTorch: () -> Unit) {
    val tapHaptic = rememberTapHaptic()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OverlayIconButton(
            onClick = {
                tapHaptic()
                onBack()
            },
            onCamera = onCamera
        ) { tint ->
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.common_back),
                tint = tint
            )
        }
        Text(
            stringResource(R.string.scan_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (onCamera) Color.White else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        if (onCamera) {
            OverlayIconButton(onClick = onToggleTorch, onCamera = true) { tint ->
                Icon(
                    imageVector = if (torchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                    contentDescription = stringResource(
                        if (torchOn) R.string.scan_torch_off else R.string.scan_torch_on
                    ),
                    tint = tint
                )
            }
        } else {
            Spacer(modifier = Modifier.size(44.dp))
        }
    }
}

/** Bouton carré arrondi de la barre du haut : fond blanc translucide sur la caméra. */
@Composable
private fun OverlayIconButton(onClick: () -> Unit, onCamera: Boolean, icon: @Composable (tint: Color) -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (onCamera) Color.White.copy(alpha = OVERLAY_ALPHA) else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        icon(if (onCamera) Color.White else MaterialTheme.colorScheme.onSurface)
    }
}

@LightDarkPreviews
@Composable
private fun ScanTopBarPreview() {
    PreviewSurface {
        Column {
            Box(modifier = Modifier.background(Color.Black)) {
                ScanTopBar(onCamera = true, torchOn = true, onBack = {}, onToggleTorch = {})
            }
            ScanTopBar(onCamera = false, torchOn = false, onBack = {}, onToggleTorch = {})
        }
    }
}
