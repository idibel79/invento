package ma.bam.inventaire.ui.quantityentry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.R
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.ui.components.DialogButtons
import ma.bam.inventaire.ui.components.PopupHaptic
import ma.bam.inventaire.ui.inventorydetail.PreviewArticles
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.LightDarkPreviews
import ma.bam.inventaire.ui.theme.PreviewSurface
import ma.bam.inventaire.ui.theme.StatusSuccess
import ma.bam.inventaire.ui.theme.StatusSuccessBg
import ma.bam.inventaire.ui.theme.TextPrimary
import ma.bam.inventaire.util.formatQuantity

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

    EntryPopup(onDismiss = onDismiss, contentPadding = EcartPopupPadding) {
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

private val EcartPopupPadding = PaddingValues(start = 18.dp, top = 22.dp, end = 18.dp, bottom = 16.dp)

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
    DialogTitle(stringResource(R.string.ecart_detected_title))
    ProductName(article)
    EcartTiles(article.quantiteTheorique, quantiteReelle, ecart)
    DialogButtons(
        secondaryLabel = stringResource(R.string.ecart_validate_without),
        onSecondary = onValidate,
        primaryLabel = stringResource(R.string.ecart_correct_then_validate),
        onPrimary = onCorrect
    )
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
    DialogTitle(stringResource(R.string.ecart_correct_title))
    ProductName(article)
    // Rappel de l'écart d'origine, avant correction.
    EcartTiles(article.quantiteTheorique, quantiteReelle, ecart)
    // La quantité système (fichier importé) prend la quantité saisie, sans ajustement possible.
    SystemQuantityDisplay(quantiteReelle)
    RemarqueField(value = remarque, onValueChange = { remarque = it })
    DialogButtons(
        secondaryLabel = stringResource(R.string.common_cancel),
        onSecondary = onCancel,
        primaryLabel = stringResource(R.string.action_save),
        onPrimary = { onSave(quantiteReelle, remarque) }
    )
}

/** « Nouvelle quantité système » en vert, en lecture seule : elle vaut la quantité saisie. */
@Composable
private fun SystemQuantityDisplay(quantite: Double) {
    Column {
        FieldLabel(stringResource(R.string.ecart_new_system_qty), color = StatusSuccess)
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

@LightDarkPreviews
@Composable
private fun EcartStepPreview() {
    val article = PreviewArticles.all.first()
    PreviewSurface(modifier = Modifier.padding(16.dp)) {
        EntryPopupCard(contentPadding = EcartPopupPadding) {
            EcartStep(
                article = article,
                quantiteReelle = article.quantiteTheorique + PREVIEW_ECART_FORT,
                ecart = PREVIEW_ECART_FORT,
                onCorrect = {},
                onValidate = {}
            )
        }
    }
}

@LightDarkPreviews
@Composable
private fun CorrectionStepPreview() {
    val article = PreviewArticles.all.first()
    PreviewSurface(modifier = Modifier.padding(16.dp)) {
        EntryPopupCard(contentPadding = EcartPopupPadding) {
            CorrectionStep(
                article = article,
                quantiteReelle = article.quantiteTheorique - 2,
                ecart = -2.0,
                onCancel = {},
                onSave = { _, _ -> }
            )
        }
    }
}

private const val PREVIEW_ECART_FORT = 6.0
