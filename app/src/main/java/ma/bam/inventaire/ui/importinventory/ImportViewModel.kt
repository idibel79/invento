package ma.bam.inventaire.ui.importinventory

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.bam.inventaire.data.repository.ExcelImportExportRepository
import ma.bam.inventaire.data.repository.InventoryRepository
import javax.inject.Inject

sealed interface ImportUiState {
    data object Idle : ImportUiState
    data object Loading : ImportUiState
    data class Success(val sessionId: String, val nbArticles: Int, val erreurs: List<String>) : ImportUiState
    data class Error(val message: String) : ImportUiState
}

@HiltViewModel
class ImportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val excelRepository: ExcelImportExportRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val state: StateFlow<ImportUiState> = _state.asStateFlow()

    fun importFile(uri: Uri) {
        _state.value = ImportUiState.Loading
        viewModelScope.launch {
            try {
                val fileName = queryFileName(uri) ?: "inventaire.xlsx"
                val result = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        excelRepository.importTheoreticalStock(stream)
                    }
                }
                if (result == null || result.articles.isEmpty()) {
                    _state.value = ImportUiState.Error(
                        "Aucun article valide trouvé dans le fichier. Vérifiez les colonnes attendues (code_article, code_barre, designation, quantite_theorique...)."
                    )
                    return@launch
                }
                val session = inventoryRepository.createSession(fileName, result.articles)
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
    }

    private fun queryFileName(uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return null
        cursor.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index >= 0) return it.getString(index)
            }
        }
        return null
    }
}
