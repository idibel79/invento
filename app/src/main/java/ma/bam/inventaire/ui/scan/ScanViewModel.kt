package ma.bam.inventaire.ui.scan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.data.repository.InventoryRepository
import ma.bam.inventaire.data.repository.SessionCounters
import ma.bam.inventaire.di.DefaultDispatcher
import ma.bam.inventaire.domain.EcartCalculator
import ma.bam.inventaire.domain.matchesQuery
import ma.bam.inventaire.util.ScanFeedback

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
    val counters: SessionCounters = SessionCounters.ZERO,
    val dialog: ScanDialogState = ScanDialogState.None,
    val finalized: Boolean = false
)

@HiltViewModel
class ScanViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: InventoryRepository,
    private val feedbackUtil: ScanFeedback,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    /** État propre à l'écran (dialog, finalisation) ; les compteurs viennent du dépôt. */
    private val _uiState = MutableStateFlow(ScanUiState())

    /** Compteurs réémis par la base à chaque enregistrement : plus de rafraîchissement manuel. */
    val uiState: StateFlow<ScanUiState> =
        combine(_uiState, repository.observeSessionCounters(sessionId)) { state, counters ->
            state.copy(counters = counters)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, ScanUiState())

    private val _manualQuery = MutableStateFlow("")
    val manualQuery: StateFlow<String> = _manualQuery.asStateFlow()

    /** Produits correspondant à la saisie manuelle (titre, code article ou code-barres). */
    val manualResults: StateFlow<List<StockArticleEntity>> =
        combine(repository.observeArticles(sessionId), _manualQuery) { articles, query ->
            if (query.isBlank()) {
                emptyList()
            } else {
                articles.asSequence().filter { it.matchesQuery(query) }.take(MAX_MANUAL_RESULTS).toList()
            }
        }.flowOn(defaultDispatcher).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun setManualQuery(query: String) {
        _manualQuery.value = query
    }

    fun selectManualArticle(article: StockArticleEntity) {
        if (_uiState.value.dialog != ScanDialogState.None) return // un dialog est déjà ouvert
        feedbackUtil.onScanSuccess()
        _manualQuery.value = ""
        _uiState.value = _uiState.value.copy(dialog = ScanDialogState.QuantityEntry(article))
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

    /** « Enregistrer » de l'étape « Corriger la quantité » : un écart restant est enregistré comme validé. */
    fun saveCorrection(article: StockArticleEntity, quantiteReelle: Double, quantiteSysteme: Double, remarque: String) {
        save { repository.recordCorrection(article.id, quantiteReelle, quantiteSysteme, remarque) }
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
        save { repository.updateQuantity(articleId, quantiteReelle, ecartValide) }
    }

    private fun save(write: suspend () -> Unit) {
        viewModelScope.launch {
            write()
            _uiState.value = _uiState.value.copy(dialog = ScanDialogState.None)
        }
    }

    private companion object {
        const val MAX_MANUAL_RESULTS = 20
    }
}
