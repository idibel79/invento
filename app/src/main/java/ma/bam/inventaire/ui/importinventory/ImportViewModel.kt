package ma.bam.inventaire.ui.importinventory

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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

/** Fichier choisi, affiché dans la feuille d'import (taille en octets, null si inconnue). */
data class ImportFileInfo(val name: String, val sizeBytes: Long?)

@HiltViewModel
class ImportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val excelRepository: ExcelImportExportRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val state: StateFlow<ImportUiState> = _state.asStateFlow()

    private val _file = MutableStateFlow<ImportFileInfo?>(null)
    val file: StateFlow<ImportFileInfo?> = _file.asStateFlow()

    fun importFile(uri: Uri) {
        val info = queryFileInfo(uri)
        _file.value = info
        _state.value = ImportUiState.Loading
        viewModelScope.launch {
            try {
                val fileName = info.name
                val result = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        excelRepository.importTheoreticalStock(stream)
                    }
                }
                if (result == null || result.articles.isEmpty()) {
                    _state.value = ImportUiState.Error(
                        "Aucun article valide trouvé dans le fichier. Vérifiez les colonnes attendues."
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
        _file.value = null
    }

    private fun queryFileInfo(uri: Uri): ImportFileInfo {
        var name: String? = null
        var size: Long? = null
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) name = it.getString(nameIndex)
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex >= 0 && !it.isNull(sizeIndex)) size = it.getLong(sizeIndex)
                }
            }
        } catch (_: Exception) {
            // Métadonnées indisponibles : nom par défaut, taille inconnue.
        }
        return ImportFileInfo(name ?: "inventaire.xlsx", size)
    }
}
