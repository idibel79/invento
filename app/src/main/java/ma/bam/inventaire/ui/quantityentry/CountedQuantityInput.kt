package ma.bam.inventaire.ui.quantityentry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
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
import ma.bam.inventaire.R
import ma.bam.inventaire.domain.EcartCalculator
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.StatusEcart
import ma.bam.inventaire.ui.theme.StatusEcartBg
import ma.bam.inventaire.ui.theme.StatusNegative
import ma.bam.inventaire.ui.theme.StatusNegativeBg
import ma.bam.inventaire.ui.theme.StatusSuccess
import ma.bam.inventaire.ui.theme.StatusSuccessBg
import ma.bam.inventaire.util.formatQuantity

/** Couleurs selon la quantité saisie : orange en dessous, vert si égale, rouge au-dessus. */
private data class EcartColors(val color: Color, val background: Color)

/**
 * Champ « Quantité comptée » avec − / + et le bandeau d'écart en dessous. Couleurs selon l'écart :
 * vert si conforme, orange pour un petit écart, rouge à partir de ±[EcartCalculator.SEUIL_ECART_FORT].
 * Partagé par la popup de saisie et l'étape « Corriger la quantité » de la popup Écart détecté.
 * [modifier] s'applique au champ de saisie lui-même (ex. demande de focus).
 */
@Composable
internal fun CountedQuantityInput(
    field: TextFieldValue,
    onFieldChange: (TextFieldValue) -> Unit,
    quantiteTheorique: Double,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quantite = field.text.replace(",", ".").toDoubleOrNull()
    // Écart arrondi à 0 quand il est négligeable, pour distinguer « conforme » / petit écart (orange) / écart ≥ 5 (rouge).
    val ecart = quantite
        ?.let { EcartCalculator.compute(quantiteTheorique, it) }
        ?.let { if (EcartCalculator.hasEcart(it)) it else 0.0 }
    val ecartState = ecart?.let { ecartColorsOf(it) }
    fun setQuantity(value: Double) {
        val formatted = formatQuantity(value)
        onFieldChange(TextFieldValue(formatted, selection = TextRange(formatted.length)))
    }

    Column {
        FieldLabel(stringResource(R.string.field_counted_qty))
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StepButton(sign = "−", description = stringResource(R.string.qty_decrease)) {
                setQuantity(((quantite ?: 0.0) - 1.0).coerceAtLeast(0.0))
            }
            CountedQuantityField(
                field = field,
                onFieldChange = onFieldChange,
                textColor = ecartState?.color ?: BamNavy,
                onDone = onDone,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .then(modifier)
            )
            StepButton(sign = "+", description = stringResource(R.string.qty_increase)) {
                setQuantity((quantite ?: 0.0) + 1.0)
            }
        }
    }
    if (ecart != null && ecartState != null) {
        EcartBanner(ecart = ecart, colors = ecartState)
    }
}

/** Vert si conforme (écart déjà ramené à 0), rouge à partir de ±5, orange en dessous. */
@Composable
private fun ecartColorsOf(ecart: Double): EcartColors = when {
    ecart == 0.0 -> EcartColors(StatusSuccess, StatusSuccessBg)
    EcartCalculator.isEcartFort(ecart) -> EcartColors(StatusEcart, StatusEcartBg)
    else -> EcartColors(StatusNegative, StatusNegativeBg)
}

/** Grand champ de la quantité comptée, encadré en marine, chiffres colorés selon l'écart. */
@Composable
private fun CountedQuantityField(
    field: TextFieldValue,
    onFieldChange: (TextFieldValue) -> Unit,
    textColor: Color,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            color = textColor
        ),
        cursorBrush = SolidColor(BamPrimary),
        modifier = modifier,
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
}

/** Bandeau sous le champ : « Conforme » ou « Écart de ±x ». */
@Composable
private fun EcartBanner(ecart: Double, colors: EcartColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ControlShape)
            .background(colors.background)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (ecart == 0.0) Icons.Filled.CheckCircleOutline else Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = colors.color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            if (ecart == 0.0) {
                stringResource(R.string.qty_ecart_conforme)
            } else {
                stringResource(R.string.qty_ecart_of, formatQuantity(ecart, showSign = true))
            },
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.color,
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
