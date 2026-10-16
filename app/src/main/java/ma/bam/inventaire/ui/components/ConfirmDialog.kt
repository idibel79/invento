package ma.bam.inventaire.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamNavyFill
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.ControlBorder
import ma.bam.inventaire.ui.theme.LightInventoPalette
import ma.bam.inventaire.ui.theme.OnBrand
import ma.bam.inventaire.ui.theme.TrackColor
import androidx.compose.runtime.ReadOnlyComposable
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.BamPrimaryContainer
import ma.bam.inventaire.ui.theme.StatusEcart
import ma.bam.inventaire.ui.theme.StatusEcartBg
import ma.bam.inventaire.ui.theme.TabularNums
import ma.bam.inventaire.ui.theme.TextPrimary
import ma.bam.inventaire.ui.theme.TextSecondary
import kotlin.math.roundToInt

/** Couleurs d'une popup : marine pour toutes les actions, rouge seulement pour l'irréversible. */
enum class ConfirmTone {
    NAVY, DANGER;

    /** Icône (sur [iconBg]). */
    val accent: Color
        @Composable get() = if (this == NAVY) BamNavy else StatusEcart

    val iconBg: Color
        @Composable get() = if (this == NAVY) BamPrimaryContainer else StatusEcartBg

    /** Fond du bouton principal, sous texte blanc (reste foncé en mode sombre). */
    val fill: Color
        @Composable get() = if (this == NAVY) BamNavyFill else LightInventoPalette.ecart
}


/**
 * Popup de confirmation commune (maquette « Popups de confirmation », ligne A) : icône dans un
 * rond teinté, titre gras centré, texte centré, barre de progression optionnelle, puis
 * Annuler en contour à gauche et l'action en bouton plein à droite.
 */
@Composable
fun ConfirmDialog(
    icon: ImageVector,
    tone: ConfirmTone,
    title: String,
    message: AnnotatedString,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String = "Annuler",
    progress: Float? = null
) {
    PopupHaptic()
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardSurface,
            shadowElevation = 6.dp,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 340.dp)
                .fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(tone.iconBg)
                ) {
                    Icon(icon, contentDescription = null, tint = tone.accent, modifier = Modifier.size(26.dp))
                }
                Text(
                    // Espace insécable avant « ? » pour qu'il ne parte jamais seul à la ligne.
                    text = title.replace(" ?", " ?"),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W800,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                Text(
                    text = message,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                if (progress != null) {
                    val fraction = progress.coerceIn(0f, 1f)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(TrackColor)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .height(6.dp)
                                    .background(BamPrimary)
                            )
                        }
                        Text(
                            text = "${(fraction * 100).roundToInt()} %",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BamNavy,
                            style = TextStyle(fontFeatureSettings = TabularNums)
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ControlBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = CardSurface, contentColor = BamNavy),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text(dismissLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = tone.fill, contentColor = OnBrand),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.weight(1.3f).height(44.dp)
                    ) {
                        Text(confirmLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }
    }
}

/** Phrase avec un segment en gras marine (numéro d'inventaire, « N / M »). */
@Composable
@ReadOnlyComposable
fun boldNavy(before: String, bold: String, after: String): AnnotatedString = buildAnnotatedString {
    append(before)
    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = BamNavy)) { append(bold) }
    append(after)
}
