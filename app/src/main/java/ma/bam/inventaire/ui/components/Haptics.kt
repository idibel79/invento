package ma.bam.inventaire.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/*
 * Retour haptique joué directement via le vibreur (comme le bip de scan), et non via
 * View.performHapticFeedback : ce dernier est muet quand le « Retour tactile » système est
 * désactivé, ce qui est le cas sur beaucoup de téléphones.
 */

/** Petite vibration « touche » à jouer sur un appui (bouton, tuile, chip). */
@Composable
fun rememberTapHaptic(): () -> Unit = rememberHaptic(heavy = false)

/** Vibration d'appui long (ouverture d'un détail), plus marquée. */
@Composable
fun rememberLongPressHaptic(): () -> Unit = rememberHaptic(heavy = true)

/** Joue la vibration courte une seule fois, à l'affichage d'une popup. */
@Composable
fun PopupHaptic() {
    val haptic = rememberTapHaptic()
    LaunchedEffect(Unit) { haptic() }
}

@Composable
private fun rememberHaptic(heavy: Boolean): () -> Unit {
    val context = LocalContext.current
    return remember(context, heavy) {
        val vibrator = context.vibrator()
        val play: () -> Unit = { vibrator.click(heavy) }
        play
    }
}

private fun Context.vibrator(): Vibrator =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

private fun Vibrator.click(heavy: Boolean) {
    if (!hasVibrator()) return
    // Impulsion pleine puissance plutôt qu'un effet « CLICK » prédéfini : sur Samsung ce dernier
    // est joué à intensité tactile réduite et reste imperceptible.
    val durationMs = if (heavy) 70L else 40L
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val amplitude = if (hasAmplitudeControl()) 255 else VibrationEffect.DEFAULT_AMPLITUDE
        vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
    } else {
        @Suppress("DEPRECATION")
        vibrate(durationMs)
    }
}
