package ma.bam.inventaire.ui.inventorydetail

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.StockArticleWithBarcodes
import ma.bam.inventaire.data.repository.ExcelImportExportRepository
import ma.bam.inventaire.data.repository.InventoryRepository
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

enum class ArticleFilter { TOUS, ECARTS, NON_SCANNES }

sealed interface ExportEvent {
    data class Ready(val uri: Uri) : ExportEvent
    data class Error(val message: String) : ExportEvent
}

@HiltViewModel
class InventoryDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val repository: InventoryRepository,
    private val excelRepository: ExcelImportExportRepository
) : ViewModel() {

    val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    private val _filter = MutableStateFlow(ArticleFilter.TOUS)
    val filter: StateFlow<ArticleFilter> = _filter.asStateFlow()

    val session: StateFlow<InventorySessionEntity?> = repository.observeSession(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val allArticles: StateFlow<List<StockArticleWithBarcodes>> = repository.observeArticles(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val visibleArticles: StateFlow<List<StockArticleWithBarcodes>> = combine(allArticles, _filter) { articles, filter ->
        when (filter) {
            ArticleFilter.TOUS -> articles
            ArticleFilter.ECARTS -> articles.filter { it.article.ecart != null && it.article.ecart != 0.0 }
            ArticleFilter.NON_SCANNES -> articles.filter { it.article.quantiteReelle == null }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _exportEvents = Channel<ExportEvent>(Channel.BUFFERED)
    val exportEvents = _exportEvents.receiveAsFlow()

    fun setFilter(filter: ArticleFilter) {
        _filter.value = filter
    }

    fun updateQuantity(articleId: Long, quantiteReelle: Double, ecartValide: Boolean) {
        viewModelScope.launch {
            repository.updateQuantity(articleId, quantiteReelle, ecartValide)
        }
    }

    fun exportCurrentSession() {
        viewModelScope.launch {
            try {
                val sessionSnapshot = checkNotNull(session.value) { "Session introuvable" }
                val articleSnapshot = allArticles.value
                val uri = withContext(Dispatchers.IO) {
                    val exportsDir = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
                    val file = File(exportsDir, "${sessionSnapshot.numero}.xlsx")
                    FileOutputStream(file).use { out ->
                        excelRepository.exportSessions(out, listOf(sessionSnapshot to articleSnapshot))
                    }
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                }
                _exportEvents.send(ExportEvent.Ready(uri))
            } catch (e: Exception) {
                _exportEvents.send(ExportEvent.Error("Échec de l'export : ${e.message}"))
            }
        }
    }
}
