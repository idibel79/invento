package ma.bam.inventaire.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.BuildConfig
import ma.bam.inventaire.R
import ma.bam.inventaire.ui.components.InventoMark
import ma.bam.inventaire.ui.components.InventoWordmark
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.BamPrimaryContainer
import ma.bam.inventaire.ui.theme.PlusJakartaSans
import ma.bam.inventaire.ui.theme.TextMuted
import ma.bam.inventaire.ui.theme.TextSecondary

/**
 * Écran de démarrage : fond blanc, logo, nom, sous-titre, barre de chargement fine qui se
 * remplit sur [durationMs] (la durée d'affichage décidée par MainActivity) et version en bas.
 */
@Composable
fun SplashScreen(durationMs: Long) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = durationMs.toInt(), easing = LinearEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InventoMark(size = 116.dp)
            Spacer(modifier = Modifier.height(24.dp))
            InventoWordmark(fontSize = 40.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = TextSecondary
                )
            )
            Spacer(modifier = Modifier.height(40.dp))
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BamPrimaryContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .size(width = 120.dp * progress.value, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(BamPrimary)
                )
            }
        }
        Text(
            text = stringResource(R.string.splash_version, BuildConfig.VERSION_NAME),
            style = TextStyle(fontFamily = PlusJakartaSans, fontSize = 12.sp, color = TextMuted),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 48.dp)
        )
    }
}
