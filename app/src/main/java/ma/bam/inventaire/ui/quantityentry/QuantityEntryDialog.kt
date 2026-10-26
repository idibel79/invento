package ma.bam.inventaire.ui.quantityentry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import kotlinx.coroutines.delay
import ma.bam.inventaire.ui.components.DialogButtons
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.domain.EcartCalculator
import ma.bam.inventaire.ui.inventorydetail.BarcodeIcon
import ma.bam.inventaire.ui.theme.StatusEcart
import ma.bam.inventaire.ui.theme.StatusEcartBg
import ma.bam.inventaire.ui.theme.StatusNegative
import ma.bam.inventaire.ui.theme.StatusNegativeBg
import ma.bam.inventaire.ui.theme.StatusSuccess
import ma.bam.inventaire.ui.theme.StatusSuccessBg
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.ControlBorder
import ma.bam.inventaire.ui.theme.SoftBand
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.TextSecondary
import ma.bam.inventaire.util.formatQuantity

internal val StepBorder: Color @Composable get() = ControlBorder
internal val QuantityBand: Color @Composable get() = SoftBand
internal val ControlShape = RoundedCornerShape(12.dp)
internal val Scrim = Color(0xFF0F1A33).copy(alpha = 0.42f)

/** Couleurs selon la quantité saisie : orange en dessous, vert si égale, rouge au-dessus. */
private data class EcartColors(val color: Color, val background: Color)

/**
 * Popup centrée « Saisir quantité » (maquette « Saisir quantité · feuille → popup », colonne
 * A · Popup) : titre, zone et codes-barres en entier, bandeau « Quantité système », puis un grand
 * champ avec boutons − et +. Toucher en dehors de la carte ferme la popup (= Annuler).
 */
@Composable
fun QuantityEntryDialog(
    article: StockArticleEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var field by remember {
        val initial = article.quantiteReelle?.let { formatQuantity(it) }.orEmpty()
        mutableStateOf(TextFieldValue(initial, selection = TextRange(initial.length)))
    }
    val quantite = field.text.replace(",", ".").toDoubleOrNull()
    fun confirm() {
        val value = quantite ?: return
        onConfirm(value)
    }

    val focusRequester = remember { FocusRequester() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        // Clavier et focus pris dans la fenêtre de la popup (ceux de l'écran derrière n'ont pas d'effet ici).
        val keyboard = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        fun closeKeyboard() {
            keyboard?.hide()
            focusManager.clearFocus()
        }
        LaunchedEffect(Unit) {
            // Laisse la popup s'attacher à sa fenêtre avant de demander le focus.
            delay(100)
            runCatching { focusRequester.requestFocus() }
            keyboard?.show()
        }
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
                            .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            article.designation.ifBlank { article.codeArticle },
                            fontSize = 16.sp,
                            lineHeight = 21.6.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BamNavy
                        )
                        if (article.emplacement.isNotBlank()) {
                            Column {
                                FieldLabel("Zone")
                                Text(
                                    article.emplacement,
                                    fontSize = 13.5.sp,
                                    lineHeight = 18.9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BamNavy,
                                    modifier = Modifier.padding(top = 1.dp)
                                )
                            }
                        }
                        val codes = listOf(article.codeBarre1, article.codeBarre2).filter { it.isNotBlank() }
                        if (codes.isNotEmpty()) {
                            Column {
                                FieldLabel("Code-barres")
                                codes.forEach { BarcodeLine(it) }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(ControlShape)
                                .background(QuantityBand)
                                .padding(horizontal = 12.dp, vertical = 9.dp)
                        ) {
                            Text(
                                "Quantité système",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = BamNavy,
                                modifier = Modifier.alignByBaseline().padding(end = 10.dp)
                            )
                            Text(
                                formatQuantity(article.quantiteTheorique),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BamNavy,
                                textAlign = TextAlign.End,
                                style = TextStyle(fontFeatureSettings = "tnum"),
                                modifier = Modifier.alignByBaseline().weight(1f)
                            )
                        }
                        CountedQuantityInput(
                            field = field,
                            onFieldChange = { field = it },
                            quantiteTheorique = article.quantiteTheorique,
                            onDone = { closeKeyboard() },
                            fieldModifier = Modifier.focusRequester(focusRequester)
                        )
                        DialogButtons(
                            secondaryLabel = "Annuler",
                            onSecondary = onDismiss,
                            primaryLabel = "Valider",
                            onPrimary = { confirm() },
                            primaryEnabled = quantite != null
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun FieldLabel(label: String, color: Color = TextSecondary) {
    Text(
        label.uppercase(),
        fontSize = 10.5.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.525.sp,
        color = color
    )
}

/**
 * Champ « Quantité comptée » avec − / + et le bandeau d'écart en dessous. Couleurs selon l'écart :
 * vert si conforme, orange pour un petit écart, rouge à partir de ±[EcartCalculator.SEUIL_ECART_FORT].
 * Partagé par la popup de saisie et l'étape « Corriger la quantité » de la popup Écart détecté.
 */
@Composable
internal fun CountedQuantityInput(
    field: TextFieldValue,
    onFieldChange: (TextFieldValue) -> Unit,
    quantiteTheorique: Double,
    onDone: () -> Unit,
    fieldModifier: Modifier = Modifier
) {
    val quantite = field.text.replace(",", ".").toDoubleOrNull()
    // Écart arrondi à 0 quand il est négligeable, pour distinguer « conforme » / petit écart (orange) / écart ≥ 5 (rouge).
    val ecart = quantite
        ?.let { EcartCalculator.compute(quantiteTheorique, it) }
        ?.let { if (EcartCalculator.hasEcart(it)) it else 0.0 }
    val ecartState = ecart?.let {
        when {
            it == 0.0 -> EcartColors(StatusSuccess, StatusSuccessBg)
            EcartCalculator.isEcartFort(it) -> EcartColors(StatusEcart, StatusEcartBg)
            else -> EcartColors(StatusNegative, StatusNegativeBg)
        }
    }
    fun setQuantity(value: Double) {
        val formatted = formatQuantity(value)
        onFieldChange(TextFieldValue(formatted, selection = TextRange(formatted.length)))
    }

    Column {
        FieldLabel("Quantité comptée")
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StepButton(sign = "−", description = "Diminuer") {
                setQuantity(((quantite ?: 0.0) - 1.0).coerceAtLeast(0.0))
            }
            BasicTextField(
                value = field,
                onValueChange = onFieldChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                // « OK » ferme seulement le clavier : seul « Valider » enregistre.
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    fontFeatureSettings = "tnum",
                    color = ecartState?.color ?: BamNavy
                ),
                cursorBrush = SolidColor(BamPrimary),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .then(fieldModifier),
                decorationBox = { inner ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .border(2.dp, BamNavy, ControlShape)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) { inner() }
                }
            )
            StepButton(sign = "+", description = "Augmenter") {
                setQuantity((quantite ?: 0.0) + 1.0)
            }
        }
    }
    if (ecartState != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(ControlShape)
                .background(ecartState.background)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (ecart == 0.0) Icons.Filled.CheckCircleOutline else Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = ecartState.color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (ecart == 0.0) {
                    "Écart conforme par rapport au système"
                } else {
                    "Écart de ${formatQuantity(ecart!!, showSign = true)} par rapport au système"
                },
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = ecartState.color,
                style = TextStyle(fontFeatureSettings = "tnum")
            )
        }
    }
}

@Composable
private fun BarcodeLine(code: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(BarcodeIcon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            code,
            fontSize = 13.5.sp,
            lineHeight = 18.9.sp,
            fontWeight = FontWeight.Bold,
            color = BamNavy,
            style = TextStyle(fontFeatureSettings = "tnum")
        )
    }
}

@Composable
internal fun StepButton(sign: String, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(ControlShape)
            .border(1.dp, StepBorder, ControlShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Text(sign, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = BamNavy)
    }
}
