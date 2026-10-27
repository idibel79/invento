package ma.bam.inventaire.ui.importinventory

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ma.bam.inventaire.R
import ma.bam.inventaire.data.importfile.ImportFileInfo
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.LightDarkPreviews
import ma.bam.inventaire.ui.theme.PreviewSurface
import ma.bam.inventaire.ui.theme.TextPrimary

private const val XLSX_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

/**
 * Feuille du bas « Nouvel inventaire », ouverte depuis l'accueil.
 * Pendant l'import, elle ne se ferme ni par glissement ni par appui à l'extérieur.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportSheet(onDismiss: () -> Unit, onOpenSession: (sessionId: String) -> Unit, viewModel: ImportViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val file by viewModel.file.collectAsStateWithLifecycle()
    val isLoading by rememberUpdatedState(state is ImportUiState.Loading)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden || !isLoading }
    )
    val scope = rememberCoroutineScope()

    val close: () -> Unit = {
        viewModel.reset()
        onDismiss()
    }
    val hideThen: (() -> Unit) -> Unit = { action ->
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importFile(it.toString()) }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isLoading) close() },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        containerColor = CardSurface,
        dragHandle = { ImportSheetHandle() }
    ) {
        ImportSheetContent(
            state = state,
            file = file,
            onPick = { filePicker.launch(arrayOf(XLSX_MIME)) },
            onCancel = { hideThen(close) },
            onClearFile = viewModel::reset,
            onOpenSession = { sessionId ->
                hideThen {
                    close()
                    onOpenSession(sessionId)
                }
            }
        )
    }
}

/** Contenu de la feuille « Nouvel inventaire » selon l'état de l'import (sans ViewModel). */
@Composable
fun ImportSheetContent(
    state: ImportUiState,
    file: ImportFileInfo?,
    onPick: () -> Unit,
    onCancel: () -> Unit,
    onClearFile: () -> Unit,
    onOpenSession: (sessionId: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.import_title),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        when (state) {
            is ImportUiState.Idle -> IdleContent(onPick = onPick, onCancel = onCancel)
            is ImportUiState.Loading -> LoadingContent(file = file)
            is ImportUiState.Success -> SuccessContent(
                success = state,
                file = file,
                onClearFile = onClearFile,
                onOpenSession = { onOpenSession(state.sessionId) }
            )
            is ImportUiState.Error -> ErrorContent(
                message = state.message,
                file = file,
                onClearFile = onClearFile,
                onCancel = onCancel,
                onPick = onPick
            )
        }
    }
}

private val previewFile = ImportFileInfo(name = "stock_pharmacie_octobre.xlsx", sizeBytes = 48_300)

@Composable
private fun ImportSheetPreview(state: ImportUiState, file: ImportFileInfo? = previewFile) {
    PreviewSurface {
        Box(modifier = Modifier.background(CardSurface)) {
            ImportSheetContent(
                state = state,
                file = file,
                onPick = {},
                onCancel = {},
                onClearFile = {},
                onOpenSession = {}
            )
        }
    }
}

@LightDarkPreviews
@Composable
private fun ImportIdlePreview() {
    ImportSheetPreview(state = ImportUiState.Idle, file = null)
}

@LightDarkPreviews
@Composable
private fun ImportLoadingPreview() {
    ImportSheetPreview(state = ImportUiState.Loading)
}

@LightDarkPreviews
@Composable
private fun ImportSuccessPreview() {
    ImportSheetPreview(
        state = ImportUiState.Success(
            sessionId = "preview",
            nbArticles = 342,
            erreurs = listOf("Ligne 12 : quantité invalide", "Ligne 57 : PID manquant")
        )
    )
}

@LightDarkPreviews
@Composable
private fun ImportErrorPreview() {
    ImportSheetPreview(
        state = ImportUiState.Error("Aucun article valide trouvé dans le fichier. Vérifiez les colonnes attendues.")
    )
}
