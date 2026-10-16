package ma.bam.inventaire.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import ma.bam.inventaire.R
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.PlusJakartaSans

/** Le pictogramme Invento (« i » code-barres sur carré arrondi marine). */
@Composable
fun InventoMark(size: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ic_invento_mark),
        contentDescription = null,
        modifier = modifier.size(size)
    )
}

/** Le nom « Invento » : « Invent » en marine, « o » en bleu, Plus Jakarta Sans ExtraBold. */
@Composable
fun InventoWordmark(fontSize: TextUnit, modifier: Modifier = Modifier) {
    Text(
        text = buildAnnotatedString {
            append("Invent")
            withStyle(SpanStyle(color = BamPrimary)) { append("o") }
        },
        style = TextStyle(
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.ExtraBold,
            fontSize = fontSize,
            letterSpacing = (-0.025).em,
            color = BamNavy
        ),
        maxLines = 1,
        softWrap = false,
        modifier = modifier
    )
}

/** Logo complet : pictogramme + nom, côte à côte. */
@Composable
fun InventoLockup(markSize: Dp, fontSize: TextUnit, gap: Dp, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = "Invento" }
    ) {
        InventoMark(size = markSize)
        Spacer(modifier = Modifier.width(gap))
        InventoWordmark(fontSize = fontSize)
    }
}
