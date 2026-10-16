package ma.bam.inventaire.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Estompe le bord droit d'un contenu scrollable horizontalement (le fait disparaître
 * progressivement vers la couleur de fond) pour signaler qu'il continue au-delà de la
 * zone visible. À chaîner AVANT [androidx.compose.foundation.horizontalScroll] (et avec le
 * même [ScrollState]) pour que la zone de fondu se base sur la largeur réellement visible.
 */
fun Modifier.fadingEdge(scrollState: ScrollState, edgeWidth: Dp = 40.dp): Modifier =
    this
        .graphicsLayer { alpha = 0.99f }
        .drawWithContent {
            drawContent()
            val edgePx = edgeWidth.toPx().coerceAtMost(size.width / 2f)
            if (scrollState.maxValue > 0 && scrollState.value < scrollState.maxValue) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Black, Color.Transparent),
                        startX = size.width - edgePx,
                        endX = size.width
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
        }
