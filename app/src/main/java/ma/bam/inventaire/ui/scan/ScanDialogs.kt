package ma.bam.inventaire.ui.scan

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ma.bam.inventaire.R
import ma.bam.inventaire.data.repository.SessionCounters
import ma.bam.inventaire.ui.components.ConfirmDialog
import ma.bam.inventaire.ui.components.ConfirmTone
import ma.bam.inventaire.ui.components.boldNavy
import ma.bam.inventaire.ui.quantityentry.EcartConfirmDialog
import ma.bam.inventaire.ui.quantityentry.QuantityEntryDialog
import ma.bam.inventaire.ui.quantityentry.UnknownArticleDialog
import ma.bam.inventaire.ui.theme.LightDarkPreviews
import ma.bam.inventaire.ui.theme.PreviewSurface

/** Popup ouverte par le ViewModel après un scan : saisie, écart détecté ou code inconnu. */
@Composable
internal fun ScanDialogHost(dialog: ScanDialogState, actions: ScanActions) {
    when (dialog) {
        is ScanDialogState.QuantityEntry -> QuantityEntryDialog(
            article = dialog.article,
            onDismiss = actions.onDismissDialog,
            onConfirm = { quantite -> actions.onConfirmQuantity(dialog.article, quantite) }
        )
        is ScanDialogState.EcartConfirm -> EcartConfirmDialog(
            article = dialog.article,
            quantiteReelle = dialog.quantiteReelle,
            ecart = dialog.ecart,
            onDismiss = actions.onDismissDialog,
            onValidate = { actions.onValidateEcart(dialog.article, dialog.quantiteReelle) },
            onSaveCorrection = { quantiteSysteme, remarque ->
                actions.onSaveCorrection(dialog.article, dialog.quantiteReelle, quantiteSysteme, remarque)
            }
        )
        is ScanDialogState.UnknownArticle -> UnknownArticleDialog(
            code = dialog.code,
            onDismiss = actions.onDismissDialog
        )
        ScanDialogState.None -> Unit
    }
}

/** Confirmation « Terminer cet inventaire ? » avec l'avancement et les écarts restants. */
@Composable
internal fun FinalizeConfirmDialog(counters: SessionCounters, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val outcome = if (counters.pendingEcarts > 0) {
        stringResource(R.string.scan_finalize_pending, counters.pendingEcarts.toString())
    } else {
        stringResource(R.string.scan_finalize_saved)
    }
    ConfirmDialog(
        icon = Icons.Outlined.CheckCircle,
        tone = ConfirmTone.NAVY,
        title = stringResource(R.string.scan_finalize_title),
        message = boldNavy(
            "",
            "${counters.scanned} / ${counters.total}",
            " " + stringResource(R.string.scan_finalize_scanned) + " " + outcome
        ),
        progress = if (counters.total > 0) counters.scanned.toFloat() / counters.total else 0f,
        confirmLabel = stringResource(R.string.scan_finalize_confirm),
        dismissLabel = stringResource(R.string.scan_finalize_continue),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@LightDarkPreviews
@Composable
private fun FinalizeConfirmDialogPreview() {
    PreviewSurface {
        FinalizeConfirmDialog(
            counters = SessionCounters(
                total = 342,
                scanned = 128,
                ecarts = 9,
                pendingEcarts = 3,
                negativeStock = 0,
                emptyStock = 0
            ),
            onConfirm = {},
            onDismiss = {}
        )
    }
}
