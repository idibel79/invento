package ma.bam.inventaire.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Texte sur une seule ligne qui réduit progressivement sa taille de police tant qu'il déborde,
 * au lieu de tronquer avec une ellipse. Utilisé pour les titres de popup dont le contenu (nom
 * d'article...) est variable et parfois long. [minFontSize] borne la réduction : au-delà, le
 * texte est tronqué en dernier recours pour éviter une taille illisible.
 */
@Composable
fun SingleLineAutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    minFontSize: TextUnit = 12.sp
) {
    val startSize = if (style.fontSize.isSp) style.fontSize else 16.sp
    var fontSize by remember(text) { mutableStateOf(startSize) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent { if (readyToDraw) drawContent() },
        color = color,
        fontWeight = fontWeight,
        style = style,
        fontSize = fontSize,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { result ->
            if (result.didOverflowWidth && fontSize > minFontSize) {
                fontSize = (fontSize.value * 0.92f).coerceAtLeast(minFontSize.value).sp
            } else {
                readyToDraw = true
            }
        }
    )
}
