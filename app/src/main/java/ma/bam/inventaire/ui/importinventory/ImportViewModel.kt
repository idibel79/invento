package ma.bam.inventaire.ui.importinventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.bam.inventaire.data.importfile.ImportFileInfo
import ma.bam.inventaire.data.importfile.ImportFileSource
import ma.bam.inventaire.data.repository.ExcelImportExportRepository
import ma.bam.inventaire.data.repository.InventoryRepository
import ma.bam.inventaire.di.IoDispatcher

sealed interface ImportUiState {
    data object Idle : ImportUiState
    data object Loading : ImportUiState
    data class Success(val sessionId: String, val nbArticles: Int, val erreurs: List<String>) : ImportUiState
    data class Error(val message: String) : ImportUiState
}

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val fileSource: ImportFileSource,
    private val excelRepository: ExcelImportExportRepository,
    private val inventoryRepository: InventoryRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _state = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val state: StateFlow<ImportUiState> = _state.asStateFlow()

    private val _file = MutableStateFlow<ImportFileInfo?>(null)
    val file: StateFlow<ImportFileInfo?> = _file.asStateFlow()

    /** Importe le fichier désigné par [fileUri] (`Uri.toString()` du sélecteur de documents). */
    @Suppress("TooGenericExceptionCaught") // toute erreur de lecture devient un message affiché
    fun importFile(fileUri: String) {
        _state.value = ImportUiState.Loading
        viewModelScope.launch {
            try {
                val info = withContext(ioDispatcher) { fileSource.describe(fileUri) }
                _file.value = info
                val result = withContext(ioDispatcher) {
                    fileSource.open(fileUri)?.use { stream -> excelRepository.importTheoreticalStock(stream) }
                }
                if (result == null || result.articles.isEmpty()) {
                    _state.value = ImportUiState.Error(
                        "Aucun article valide trouvé dans le fichier. Vérifiez les colonnes attendues."
                    )
                    return@launch
                }
                val session = inventoryRepository.createSession(info.name, result.articles)
                _state.value = ImportUiState.Success(
                    sessionId = session.id,
                    nbArticles = result.articles.size,
                    erreurs = result.erreurs
                )
            } catch (e: Exception) {
                _state.value = ImportUiState.Error("Échec de l'import : ${e.message}")
            }
        }
    }

    fun reset() {
        _state.value = ImportUiState.Idle
        _file.value = null
    }
}
