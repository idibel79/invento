package ma.bam.inventaire.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Padding des boutons côte à côte (deux par ligne) : plus serré que celui par défaut de
 * Material (24dp) pour laisser la place au libellé sur un téléphone étroit.
 */
val CompactButtonPadding = PaddingValues(horizontal = 12.dp)

/**
 * Contenu d'un bouton en demi-largeur : icône 18dp + libellé sur une seule ligne, dont la
 * police se réduit automatiquement s'il ne tient pas (téléphone étroit, police système agrandie)
 * au lieu de passer à la ligne.
 */
@Composable
fun RowScope.ButtonLabel(text: String, icon: ImageVector? = null, painter: Painter? = null) {
    when {
        icon != null -> Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        painter != null -> Icon(painter, contentDescription = null, modifier = Modifier.size(18.dp))
    }
    if (icon != null || painter != null) Spacer(modifier = Modifier.width(6.dp))
    SingleLineAutoSizeText(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        minFontSize = 11.sp,
        modifier = Modifier.weight(1f, fill = false)
    )
}
