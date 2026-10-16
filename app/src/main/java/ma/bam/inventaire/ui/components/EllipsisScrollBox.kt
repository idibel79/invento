package ma.bam.inventaire.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

/**
 * Contenu d'une ligne qui défile horizontalement (glisser pour lire la suite) et qui affiche
 * « … » au bord droit tant que le texte continue au-delà de la zone visible. Le « … » est
 * posé sur [background] (la couleur, opaque, du fond derrière) pour masquer la fin coupée,
 * et disparaît une fois arrivé au bout du texte.
 */
@Composable
fun EllipsisScrollBox(
    scrollState: ScrollState,
    background: Color,
    ellipsisColor: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // derivedStateOf : ne recompose que quand le « … » apparaît ou disparaît, pas à chaque pixel défilé.
    val showEllipsis by remember(scrollState) {
        derivedStateOf { scrollState.maxValue > 0 && scrollState.value < scrollState.maxValue }
    }
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        Box(modifier = Modifier.horizontalScroll(scrollState)) { content() }
        if (showEllipsis) {
            Text(
                "…",
                fontSize = fontSize,
                fontWeight = fontWeight,
                color = ellipsisColor,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(background)
                    .padding(start = 2.dp)
            )
        }
    }
}
