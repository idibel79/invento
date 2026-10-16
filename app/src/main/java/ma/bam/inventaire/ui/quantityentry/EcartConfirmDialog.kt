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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
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
import ma.bam.inventaire.ui.theme.TextMuted
import ma.bam.inventaire.ui.theme.TextPrimary
import ma.bam.inventaire.ui.theme.TextSecondary
import ma.bam.inventaire.util.formatQuantity

private const val RemarqueMaxLength = 200
private val Tabular = TextStyle(fontFeatureSettings = "tnum")

/**
 * Popup « Écart détecté » à 2 étapes (maquette « Écart détecté · 3 propositions », colonnes
 * A · Écart et A · Après « Corriger ») : 1) Système / Compté / Écart avec « Corriger » et
 * « Valider » ; 2) « Corriger la quantité » avec remarque facultative.
 */
@Composable
fun EcartConfirmDialog(
    article: StockArticleEntity,
    quantiteReelle: Double,
    ecart: Double,
    onDismiss: () -> Unit,
    onValidate: () -> Unit,
    onSaveCorrection: (quantite: Double, remarque: String) -> Unit
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
    // Même règle que les cartes produit : rouge à partir de ±5, orange en dessous.
    val fort = EcartCalculator.isEcartFort(ecart)
    val ecartColor = if (fort) StatusEcart else StatusNegative
    val ecartBg = if (fort) StatusEcartBg else StatusNegativeBg
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuantityTile("Système", formatQuantity(article.quantiteTheorique), QuantityBand, TextSecondary, BamNavy)
        QuantityTile("Compté", formatQuantity(quantiteReelle), QuantityBand, TextSecondary, BamNavy)
        QuantityTile("Écart", formatQuantity(ecart, showSign = true), ecartBg, ecartColor, ecartColor)
    }
    DialogButtons(
        secondaryLabel = "Corriger",
        onSecondary = onCorrect,
        primaryLabel = "Valider",
        onPrimary = onValidate
    )
}

@Composable
private fun ColumnScope.CorrectionStep(
    article: StockArticleEntity,
    onCancel: () -> Unit,
    onSave: (Double, String) -> Unit
) {
    // Nouvelle correction : on repart de la remarque existante, que « Enregistrer » remplace.
    var remarque by remember { mutableStateOf(article.remarque.orEmpty()) }
    // Préremplie avec la quantité système (écart nul), modifiable avec − / + ou au clavier.
    var field by remember {
        val initial = formatQuantity(article.quantiteTheorique)
        mutableStateOf(TextFieldValue(initial, selection = TextRange(initial.length)))
    }
    val quantite = field.text.replace(",", ".").toDoubleOrNull()
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    DialogTitle("Corriger la quantité")
    ProductName(article)
    QuantityBandRow("Quantité système", article.quantiteTheorique)
    CountedQuantityInput(
        field = field,
        onFieldChange = { field = it },
        quantiteTheorique = article.quantiteTheorique,
        onDone = {
            keyboard?.hide()
            focusManager.clearFocus()
        }
    )
    RemarqueField(value = remarque, onValueChange = { remarque = it })
    DialogButtons(
        secondaryLabel = "Annuler",
        onSecondary = onCancel,
        primaryLabel = "Enregistrer",
        onPrimary = { quantite?.let { onSave(it, remarque) } },
        primaryEnabled = quantite != null
    )
}

@Composable
private fun QuantityBandRow(label: String, value: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ControlShape)
            .background(QuantityBand)
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Text(
            label,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = BamNavy,
            modifier = Modifier.alignByBaseline().padding(end = 10.dp)
        )
        Text(
            formatQuantity(value),
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BamNavy,
            textAlign = TextAlign.End,
            style = Tabular,
            modifier = Modifier.alignByBaseline().weight(1f)
        )
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

@Composable
private fun DialogButtons(
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
            modifier = Modifier.weight(1f).height(44.dp)
        ) {
            Text(secondaryLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BamNavy, maxLines = 1)
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
            modifier = Modifier.weight(1.3f).height(44.dp)
        ) {
            Text(primaryLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}
