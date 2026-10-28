package ma.bam.inventaire.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamNavyFill
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.ControlBorder
import ma.bam.inventaire.ui.theme.OnBrand

/*
 * Boutons d'action communs (charte « Boutons ») : 50dp de haut, coins 12dp, texte 13sp gras
 * centré qui passe sur 2 lignes centrées plutôt que d'être coupé. Dans une rangée, chaque
 * bouton prend la même largeur (weight 1f).
 */

private val ActionButtonShape = RoundedCornerShape(12.dp)
private val ActionButtonPadding = PaddingValues(horizontal = 10.dp)
private val ActionButtonHeight = 50.dp

/**
 * Paire Annuler / action (popups Saisir quantité, Écart détecté, confirmations, feuilles) :
 * secondaire en contour à gauche, principal plein à droite, même largeur et même hauteur.
 */
@Composable
fun DialogButtons(
    secondaryLabel: String,
    onSecondary: () -> Unit,
    primaryLabel: String,
    onPrimary: () -> Unit,
    primaryEnabled: Boolean = true,
    primaryColor: Color = BamNavyFill,
    secondaryIcon: ImageVector? = null,
    modifier: Modifier = Modifier.padding(top = 4.dp)
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SecondaryActionButton(secondaryLabel, onSecondary, Modifier.weight(1f), icon = secondaryIcon)
        PrimaryActionButton(
            primaryLabel,
            onPrimary,
            Modifier.weight(1f),
            enabled = primaryEnabled,
            containerColor = primaryColor
        )
    }
}

/** Bouton secondaire : contour gris, fond carte, texte marine. */
@Composable
fun SecondaryActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    OutlinedButton(
        onClick = onClick,
        shape = ActionButtonShape,
        border = BorderStroke(1.dp, ControlBorder),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = CardSurface, contentColor = BamNavy),
        contentPadding = ActionButtonPadding,
        modifier = modifier.height(ActionButtonHeight)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = BamNavy, modifier = Modifier.size(17.dp))
            Spacer(modifier = Modifier.width(6.dp))
        }
        ActionLabel(label, color = BamNavy)
    }
}

/** Bouton principal : plein (bleu Invento par défaut, rouge pour l'irréversible), texte blanc. */
@Composable
fun PrimaryActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = BamNavyFill
) {
    Button(
        enabled = enabled,
        onClick = onClick,
        shape = ActionButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = OnBrand,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor = OnBrand
        ),
        contentPadding = ActionButtonPadding,
        modifier = modifier.height(ActionButtonHeight)
    ) {
        ActionLabel(label)
    }
}

/** Libellé de bouton : passe sur 2 lignes centrées s'il est trop long (« Valider sans corriger »). */
@Composable
private fun ActionLabel(text: String, color: Color = Color.Unspecified) {
    Text(
        text,
        fontSize = 13.sp,
        lineHeight = 15.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        textAlign = TextAlign.Center,
        maxLines = 2
    )
}
