package ma.bam.inventaire.ui.quantityentry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.ui.components.PopupHaptic
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamNavyFill
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.ControlBorder
import ma.bam.inventaire.ui.theme.OnBrand
import ma.bam.inventaire.ui.theme.SoftBand
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.domain.EcartCalculator
import ma.bam.inventaire.ui.theme.StatusEcart
import ma.bam.inventaire.ui.theme.StatusNegative
import ma.bam.inventaire.ui.theme.StatusNegativeBg
import ma.bam.inventaire.ui.theme.StatusEcartBg
import ma.bam.inventaire.ui.theme.StatusSuccess
import ma.bam.inventaire.ui.theme.StatusSuccessBg
import ma.bam.inventaire.ui.theme.TextMuted
import ma.bam.inventaire.ui.theme.TextPrimary
import ma.bam.inventaire.ui.theme.TextSecondary
import ma.bam.inventaire.util.formatQuantity

private const val RemarqueMaxLength = 200
private val Tabular = TextStyle(fontFeatureSettings = "tnum")

/**
 * Popup « Écart détecté » à 2 étapes (maquette « Écart détecté · 3 propositions », colonnes
 * A · Écart et A · Après « Corriger ») : 1) Système / Compté / Écart avec « Corriger » et
 * « Valider » ; 2) « Corriger la quantité système ? » : la quantité système prend la valeur
 * comptée (lecture seule), avec remarque facultative.
 */
@Composable
fun EcartConfirmDialog(
    article: StockArticleEntity,
    quantiteReelle: Double,
    ecart: Double,
    onDismiss: () -> Unit,
    onValidate: () -> Unit,
    onSaveCorrection: (nouvelleQuantiteSysteme: Double, remarque: String) -> Unit
) {
    PopupHaptic()
    var correcting by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        // Le voile est dessiné ici (#0F1A33 à 42 %) : on coupe l'assombrissement par défaut de la fenêtre.
        (LocalView.current.parent as? DialogWindowProvider)?.window?.setDimAmount(0f)
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Scrim)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = CardSurface,
                    shadowElevation = 10.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(start = 18.dp, top = 22.dp, end = 18.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (correcting) {
                            CorrectionStep(
                                article = article,
                                quantiteReelle = quantiteReelle,
                                ecart = ecart,
                                onCancel = { correcting = false },
                                onSave = onSaveCorrection
                            )
                        } else {
                            EcartStep(
                                article = article,
                                quantiteReelle = quantiteReelle,
                                ecart = ecart,
                                onCorrect = { correcting = true },
                                onValidate = onValidate
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.EcartStep(
    article: StockArticleEntity,
    quantiteReelle: Double,
    ecart: Double,
    onCorrect: () -> Unit,
    onValidate: () -> Unit
) {
    val (ecartColor, ecartBg) = ecartColors(ecart)
    Box(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .size(52.dp)
            .clip(CircleShape)
            .background(ecartBg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = ecartColor,
            modifier = Modifier.size(26.dp)
        )
    }
    DialogTitle("Écart détecté")
    ProductName(article)
    EcartTiles(article.quantiteTheorique, quantiteReelle, ecart)
    DialogButtons(
        secondaryLabel = "Valider sans corriger",
        onSecondary = onValidate,
        primaryLabel = "Corriger puis valider",
        onPrimary = onCorrect
    )
}

/** Même règle que les cartes produit : rouge à partir de ±5, orange en dessous. */
@Composable
private fun ecartColors(ecart: Double): Pair<Color, Color> =
    if (EcartCalculator.isEcartFort(ecart)) StatusEcart to StatusEcartBg
    else StatusNegative to StatusNegativeBg

/** Système / Compté / Écart, avec la quantité système d'avant toute correction. */
@Composable
private fun EcartTiles(quantiteSysteme: Double, quantiteReelle: Double, ecart: Double) {
    val (ecartColor, ecartBg) = ecartColors(ecart)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuantityTile("Système", formatQuantity(quantiteSysteme), QuantityBand, TextSecondary, BamNavy)
        QuantityTile("Compté", formatQuantity(quantiteReelle), QuantityBand, TextSecondary, BamNavy)
        QuantityTile("Écart", formatQuantity(ecart, showSign = true), ecartBg, ecartColor, ecartColor)
    }
}

@Composable
private fun ColumnScope.CorrectionStep(
    article: StockArticleEntity,
    quantiteReelle: Double,
    ecart: Double,
    onCancel: () -> Unit,
    onSave: (Double, String) -> Unit
) {
    // Nouvelle correction : on repart de la remarque existante, que « Enregistrer » remplace.
    var remarque by remember { mutableStateOf(article.remarque.orEmpty()) }
    DialogTitle("Corriger la quantité système ?")
    ProductName(article)
    // Rappel de l'écart d'origine, avant correction.
    EcartTiles(article.quantiteTheorique, quantiteReelle, ecart)
    // La quantité système (fichier importé) prend la quantité saisie, sans ajustement possible.
    SystemQuantityDisplay(quantiteReelle)
    RemarqueField(value = remarque, onValueChange = { remarque = it })
    DialogButtons(
        secondaryLabel = "Annuler",
        onSecondary = onCancel,
        primaryLabel = "Enregistrer",
        onPrimary = { onSave(quantiteReelle, remarque) }
    )
}

/** « Nouvelle quantité système » en vert, en lecture seule : elle vaut la quantité saisie. */
@Composable
private fun SystemQuantityDisplay(quantite: Double) {
    Column {
        FieldLabel("Nouvelle quantité système", color = StatusSuccess)
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(ControlShape)
                .background(StatusSuccessBg)
                .border(2.dp, StatusSuccess, ControlShape)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                formatQuantity(quantite),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = StatusSuccess,
                style = Tabular
            )
        }
    }
}

@Composable
private fun RemarqueField(value: String, onValueChange: (String) -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Outlined.StickyNote2,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            FieldLabel("Remarque (facultatif)")
        }
        Spacer(modifier = Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.take(RemarqueMaxLength)) },
            maxLines = 3,
            interactionSource = interaction,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences
            ),
            textStyle = TextStyle(fontSize = 13.5.sp, lineHeight = 19.575.sp, color = TextPrimary),
            cursorBrush = SolidColor(BamPrimary),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 66.dp)
                        .border(
                            if (focused) 2.dp else 1.dp,
                            if (focused) BamNavy else StepBorder,
                            ControlShape
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    if (value.isEmpty()) {
                        Text(
                            "Ex. : 2 boîtes abîmées en réserve",
                            fontSize = 13.5.sp,
                            lineHeight = 19.575.sp,
                            color = TextMuted
                        )
                    }
                    inner()
                }
            }
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            "${value.length} / $RemarqueMaxLength",
            fontSize = 11.sp,
            color = TextMuted,
            textAlign = TextAlign.End,
            style = Tabular,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DialogTitle(text: String) {
    Text(
        text,
        fontSize = 17.sp,
        fontWeight = FontWeight.ExtraBold,
        color = TextPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ProductName(article: StockArticleEntity) {
    Text(
        article.designation.ifBlank { article.codeArticle },
        fontSize = 14.sp,
        lineHeight = 19.6.sp,
        fontWeight = FontWeight.Bold,
        color = BamNavy,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun RowScope.QuantityTile(
    label: String,
    value: String,
    background: Color,
    labelColor: Color,
    valueColor: Color
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(ControlShape)
            .background(background)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = labelColor, maxLines = 1)
        Text(
            value,
            fontSize = 22.sp,
            lineHeight = 26.4.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor,
            style = Tabular,
            maxLines = 1
        )
    }
}

/** Paire de boutons des popups de saisie (Saisir quantité, Écart détecté) : même largeur, 50dp. */
@Composable
internal fun DialogButtons(
    secondaryLabel: String,
    onSecondary: () -> Unit,
    primaryLabel: String,
    onPrimary: () -> Unit,
    primaryEnabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onSecondary,
            shape = ControlShape,
            border = BorderStroke(1.dp, StepBorder),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = CardSurface),
            contentPadding = ButtonPadding,
            modifier = Modifier.weight(1f).height(50.dp)
        ) {
            ButtonLabel(secondaryLabel, color = BamNavy)
        }
        Button(
            enabled = primaryEnabled,
            onClick = onPrimary,
            shape = ControlShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = BamNavyFill,
                contentColor = OnBrand,
                disabledContainerColor = BamNavyFill.copy(alpha = 0.4f),
                disabledContentColor = OnBrand
            ),
            contentPadding = ButtonPadding,
            modifier = Modifier.weight(1f).height(50.dp)
        ) {
            ButtonLabel(primaryLabel)
        }
    }
}

private val ButtonPadding = PaddingValues(horizontal = 10.dp)

/** Libellé de bouton : passe sur 2 lignes centrées s'il est trop long (« Valider sans corriger »). */
@Composable
private fun ButtonLabel(text: String, color: Color = Color.Unspecified) {
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
