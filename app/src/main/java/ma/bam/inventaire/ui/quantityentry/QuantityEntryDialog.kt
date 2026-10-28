package ma.bam.inventaire.ui.quantityentry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ma.bam.inventaire.R
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.ui.components.DialogButtons
import ma.bam.inventaire.ui.inventorydetail.BarcodeIcon
import ma.bam.inventaire.ui.inventorydetail.PreviewArticles
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.ControlBorder
import ma.bam.inventaire.ui.theme.LightDarkPreviews
import ma.bam.inventaire.ui.theme.PreviewSurface
import ma.bam.inventaire.ui.theme.SoftBand
import ma.bam.inventaire.ui.theme.TextSecondary
import ma.bam.inventaire.util.formatQuantity

internal val StepBorder: Color @Composable get() = ControlBorder
internal val QuantityBand: Color @Composable get() = SoftBand
internal val ControlShape = RoundedCornerShape(12.dp)
internal val Scrim = Color(0xFF0F1A33).copy(alpha = 0.42f)

/**
 * Popup centrée « Saisir quantité » (maquette « Saisir quantité · feuille → popup », colonne
 * A · Popup) : titre, zone et codes-barres en entier, bandeau « Quantité système », puis un grand
 * champ avec boutons − et +. Toucher en dehors de la carte ferme la popup (= Annuler).
 */
@Composable
fun QuantityEntryDialog(article: StockArticleEntity, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
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

    EntryPopup(onDismiss = onDismiss, contentPadding = QuantityPopupPadding) {
        // Clavier et focus pris dans la fenêtre de la popup (ceux de l'écran derrière n'ont pas d'effet ici).
        val keyboard = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        LaunchedEffect(Unit) {
            // Laisse la popup s'attacher à sa fenêtre avant de demander le focus.
            delay(100)
            runCatching { focusRequester.requestFocus() }
            keyboard?.show()
        }
        QuantityEntryContent(
            article = article,
            field = field,
            onFieldChange = { field = it },
            onDone = {
                keyboard?.hide()
                focusManager.clearFocus()
            },
            onDismiss = onDismiss,
            onConfirm = { confirm() },
            confirmEnabled = quantite != null,
            modifier = Modifier.focusRequester(focusRequester)
        )
    }
}

private val QuantityPopupPadding = PaddingValues(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 16.dp)

/**
 * Contenu de la popup « Saisir quantité », sans état : titre, zone, codes-barres, quantités et boutons.
 * [modifier] s'applique au champ de la quantité comptée (demande de focus).
 */
@Composable
private fun QuantityEntryContent(
    article: StockArticleEntity,
    field: TextFieldValue,
    onFieldChange: (TextFieldValue) -> Unit,
    onDone: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        article.designation.ifBlank { article.codeArticle },
        fontSize = 16.sp,
        lineHeight = 21.6.sp,
        fontWeight = FontWeight.ExtraBold,
        color = BamNavy
    )
    if (article.emplacement.isNotBlank()) {
        ZoneField(article.emplacement)
    }
    val codes = listOf(article.codeBarre1, article.codeBarre2).filter { it.isNotBlank() }
    if (codes.isNotEmpty()) {
        Column {
            FieldLabel(stringResource(R.string.field_barcode))
            codes.forEach { BarcodeLine(it) }
        }
    }
    SystemQuantityBand(article.quantiteTheorique)
    CountedQuantityInput(
        field = field,
        onFieldChange = onFieldChange,
        quantiteTheorique = article.quantiteTheorique,
        onDone = onDone,
        modifier = modifier
    )
    DialogButtons(
        secondaryLabel = stringResource(R.string.common_cancel),
        onSecondary = onDismiss,
        primaryLabel = stringResource(R.string.action_validate),
        onPrimary = onConfirm,
        primaryEnabled = confirmEnabled
    )
}

@Composable
private fun ZoneField(zone: String) {
    Column {
        FieldLabel(stringResource(R.string.field_zone))
        Text(
            zone,
            fontSize = 13.5.sp,
            lineHeight = 18.9.sp,
            fontWeight = FontWeight.Bold,
            color = BamNavy,
            modifier = Modifier.padding(top = 1.dp)
        )
    }
}

/** Bandeau « Quantité système » : libellé à gauche, quantité à droite. */
@Composable
private fun SystemQuantityBand(quantiteTheorique: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ControlShape)
            .background(QuantityBand)
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Text(
            stringResource(R.string.field_system_qty),
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = BamNavy,
            modifier = Modifier.alignByBaseline().padding(end = 10.dp)
        )
        Text(
            formatQuantity(quantiteTheorique),
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BamNavy,
            textAlign = TextAlign.End,
            style = TextStyle(fontFeatureSettings = "tnum"),
            modifier = Modifier.alignByBaseline().weight(1f)
        )
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

@LightDarkPreviews
@Composable
private fun QuantityEntryContentPreview() {
    val article = PreviewArticles.all.first()
    PreviewSurface(modifier = Modifier.padding(16.dp)) {
        EntryPopupCard(contentPadding = QuantityPopupPadding) {
            QuantityEntryContent(
                article = article,
                field = TextFieldValue(formatQuantity(article.quantiteTheorique + 2)),
                onFieldChange = {},
                onDone = {},
                onDismiss = {},
                onConfirm = {},
                confirmEnabled = true
            )
        }
    }
}
