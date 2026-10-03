package ma.bam.inventaire.ui.scan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.data.repository.InventoryRepository
import ma.bam.inventaire.data.repository.SessionCounters
import ma.bam.inventaire.domain.EcartCalculator
import ma.bam.inventaire.util.FeedbackUtil
import javax.inject.Inject

sealed interface ScanDialogState {
    data object None : ScanDialogState
    data class QuantityEntry(val article: StockArticleEntity) : ScanDialogState
    data class EcartConfirm(
        val article: StockArticleEntity,
        val quantiteReelle: Double,
        val ecart: Double
    ) : ScanDialogState
    data class UnknownArticle(val code: String) : ScanDialogState
}

data class ScanUiState(
    val counters: SessionCounters = SessionCounters(0, 0, 0),
    val dialog: ScanDialogState = ScanDialogState.None,
    val finalized: Boolean = false
)

@HiltViewModel
class ScanViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: InventoryRepository,
    private val feedbackUtil: FeedbackUtil
) : ViewModel() {

    val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    init {
        refreshCounters()
    }

    fun onCodeDetected(code: String) {
        if (_uiState.value.dialog != ScanDialogState.None) return // un dialog est déjà ouvert
        viewModelScope.launch {
            val found = repository.findArticleByCode(sessionId, code)
            if (found == null) {
                feedbackUtil.onScanError()
                _uiState.value = _uiState.value.copy(dialog = ScanDialogState.UnknownArticle(code))
            } else {
                feedbackUtil.onScanSuccess()
                _uiState.value = _uiState.value.copy(dialog = ScanDialogState.QuantityEntry(found))
            }
        }
    }

    fun confirmQuantity(article: StockArticleEntity, quantiteReelle: Double) {
        val ecart = EcartCalculator.compute(article.quantiteTheorique, quantiteReelle)
        if (EcartCalculator.hasEcart(ecart)) {
            _uiState.value = _uiState.value.copy(
                dialog = ScanDialogState.EcartConfirm(article, quantiteReelle, ecart)
            )
        } else {
            saveQuantity(article.id, quantiteReelle, ecartValide = true)
        }
    }

    fun validateEcart(article: StockArticleEntity, quantiteReelle: Double) {
        saveQuantity(article.id, quantiteReelle, ecartValide = true)
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(dialog = ScanDialogState.None)
    }

    fun finalizeInventory() {
        viewModelScope.launch {
            repository.finalizeSession(sessionId)
            _uiState.value = _uiState.value.copy(finalized = true)
        }
    }

    private fun saveQuantity(articleId: Long, quantiteReelle: Double, ecartValide: Boolean) {
        viewModelScope.launch {
            repository.recordScan(articleId, quantiteReelle, ecartValide)
            _uiState.value = _uiState.value.copy(dialog = ScanDialogState.None)
            refreshCounters()
        }
    }

    private fun refreshCounters() {
        viewModelScope.launch {
            val counters = repository.getSessionCounters(sessionId)
            _uiState.value = _uiState.value.copy(counters = counters)
        }
    }
}
