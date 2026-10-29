package ma.bam.inventaire.ui.scan

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.data.repository.SessionCounters
import ma.bam.inventaire.ui.inventorydetail.PreviewArticles
import ma.bam.inventaire.ui.theme.LightDarkPreviews
import ma.bam.inventaire.ui.theme.PreviewSurface

/** Écran de scan relié au ViewModel et à la permission caméra ; l'affichage est dans [ScanContent]. */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(onBack: () -> Unit, onFinished: (sessionId: String) -> Unit, viewModel: ScanViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val manualQuery by viewModel.manualQuery.collectAsStateWithLifecycle()
    val manualResults by viewModel.manualResults.collectAsStateWithLifecycle()
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    var manualMode by remember { mutableStateOf(false) }
    var torchOn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(uiState.finalized) {
        if (uiState.finalized) onFinished(viewModel.sessionId)
    }

    LaunchedEffect(manualMode) {
        if (manualMode) torchOn = false
    }

    val actions = remember(viewModel, onBack, cameraPermission) {
        ScanActions(
            onBack = onBack,
            onRequestCameraPermission = cameraPermission::launchPermissionRequest,
            onToggleManualMode = { manualMode = !manualMode },
            onToggleTorch = { torchOn = !torchOn },
            onCodeDetected = viewModel::onCodeDetected,
            onManualQueryChange = viewModel::setManualQuery,
            onSelectManualArticle = viewModel::selectManualArticle,
            onDismissDialog = viewModel::dismissDialog,
            onConfirmQuantity = viewModel::confirmQuantity,
            onValidateEcart = viewModel::validateEcart,
            onBackToQuantityEntry = viewModel::backToQuantityEntry,
            onSaveCorrection = viewModel::saveCorrection,
            onFinalize = viewModel::finalizeInventory
        )
    }

    ScanContent(
        uiState = uiState,
        cameraGranted = cameraPermission.status.isGranted,
        manualMode = manualMode,
        torchOn = torchOn,
        manualQuery = manualQuery,
        manualResults = manualResults,
        actions = actions
    )
}

/** Actions de l'écran de scan transmises au composable sans état. */
data class ScanActions(
    val onBack: () -> Unit,
    val onRequestCameraPermission: () -> Unit,
    val onToggleManualMode: () -> Unit,
    val onToggleTorch: () -> Unit,
    val onCodeDetected: (String) -> Unit,
    val onManualQueryChange: (String) -> Unit,
    val onSelectManualArticle: (StockArticleEntity) -> Unit,
    val onDismissDialog: () -> Unit,
    val onConfirmQuantity: (StockArticleEntity, Double) -> Unit,
    val onValidateEcart: (StockArticleEntity, Double) -> Unit,
    val onBackToQuantityEntry: (StockArticleEntity, Double) -> Unit,
    val onSaveCorrection: (
        article: StockArticleEntity,
        quantiteReelle: Double,
        remarque: String
    ) -> Unit,
    val onFinalize: () -> Unit
)

/**
 * Écran de scan sans ViewModel : caméra plein écran (ou saisie manuelle, ou demande
 * d'autorisation), barre du haut posée dessus, barre du bas et popups.
 */
@Composable
fun ScanContent(
    uiState: ScanUiState,
    cameraGranted: Boolean,
    manualMode: Boolean,
    torchOn: Boolean,
    manualQuery: String,
    manualResults: List<StockArticleEntity>,
    actions: ScanActions
) {
    var showFinalizeConfirm by remember { mutableStateOf(false) }
    val cameraVisible = !manualMode && cameraGranted
    var sheetHeight by remember { mutableStateOf(0.dp) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (cameraVisible) Color.Black else MaterialTheme.colorScheme.background)
    ) {
        if (cameraVisible) {
            // Caméra plein écran : le centre de l'aperçu (fenêtre de visée) reste le centre de l'image analysée.
            CameraPreview(torchEnabled = torchOn, onBarcodeDetected = actions.onCodeDetected)
        } else if (!cameraGranted && !manualMode) {
            CameraPermissionPane(bottomPadding = sheetHeight, onRequestPermission = actions.onRequestCameraPermission)
        } else {
            ManualEntryPane(
                query = manualQuery,
                results = manualResults,
                bottomPadding = sheetHeight,
                onQueryChange = actions.onManualQueryChange,
                onSelect = actions.onSelectManualArticle
            )
        }

        ScanTopBar(
            onCamera = cameraVisible,
            torchOn = torchOn,
            onBack = actions.onBack,
            onToggleTorch = actions.onToggleTorch
        )

        ScanBottomBar(
            counters = uiState.counters,
            manualMode = manualMode,
            onToggleManualMode = actions.onToggleManualMode,
            onFinish = { showFinalizeConfirm = true },
            onHeightChange = { sheetHeight = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    ScanDialogHost(dialog = uiState.dialog, actions = actions)

    if (showFinalizeConfirm) {
        FinalizeConfirmDialog(
            counters = uiState.counters,
            onConfirm = {
                showFinalizeConfirm = false
                actions.onFinalize()
            },
            onDismiss = { showFinalizeConfirm = false }
        )
    }
}

private val NoScanActions = ScanActions(
    onBack = {},
    onRequestCameraPermission = {},
    onToggleManualMode = {},
    onToggleTorch = {},
    onCodeDetected = {},
    onManualQueryChange = {},
    onSelectManualArticle = {},
    onDismissDialog = {},
    onConfirmQuantity = { _, _ -> },
    onValidateEcart = { _, _ -> },
    onBackToQuantityEntry = { _, _ -> },
    onSaveCorrection = { _, _, _ -> },
    onFinalize = {}
)

private val previewCounters = SessionCounters(
    total = 342,
    scanned = 128,
    ecarts = 9,
    pendingEcarts = 3,
    negativeStock = 0,
    emptyStock = 0
)

@LightDarkPreviews
@Composable
private fun ScanManualPreview() {
    PreviewSurface {
        ScanContent(
            uiState = ScanUiState(counters = previewCounters),
            cameraGranted = true,
            manualMode = true,
            torchOn = false,
            manualQuery = "doli",
            manualResults = PreviewArticles.all.take(2),
            actions = NoScanActions
        )
    }
}

@LightDarkPreviews
@Composable
private fun ScanPermissionPreview() {
    PreviewSurface {
        ScanContent(
            uiState = ScanUiState(counters = previewCounters),
            cameraGranted = false,
            manualMode = false,
            torchOn = false,
            manualQuery = "",
            manualResults = emptyList(),
            actions = NoScanActions
        )
    }
}
