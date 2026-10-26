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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.data.repository.ExcelImportExportRepository
import ma.bam.inventaire.domain.ArticleFilter
import ma.bam.inventaire.domain.ArticleFilters
import ma.bam.inventaire.domain.ArticleSort
import ma.bam.inventaire.domain.FieldFilters
import ma.bam.inventaire.data.repository.ImportedArticle
import ma.bam.inventaire.data.repository.InventoryRepository
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** Position (emplacement) sélectionnée, ou null = toutes les zones. */
    private val _positionFilter = MutableStateFlow<String?>(null)
    val positionFilter: StateFlow<String?> = _positionFilter.asStateFlow()

    /** Catégorie sélectionnée, ou null = toutes les catégories. */
    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    /** Plage de prix (PPV) sélectionnée, ou null = tous les prix. */
    private val _priceRangeFilter = MutableStateFlow<ClosedFloatingPointRange<Float>?>(null)
    val priceRangeFilter: StateFlow<ClosedFloatingPointRange<Float>?> = _priceRangeFilter.asStateFlow()

    private val _sort = MutableStateFlow(ArticleSort.DEFAUT)
    val sort: StateFlow<ArticleSort> = _sort.asStateFlow()

    val session: StateFlow<InventorySessionEntity?> = repository.observeSession(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val allArticles: StateFlow<List<StockArticleEntity>> = repository.observeArticles(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availablePositions: StateFlow<List<String>> = allArticles
        .map { articles -> articles.map { it.emplacement }.filter { it.isNotBlank() }.distinct().sorted() }
        .flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCategories: StateFlow<List<String>> = allArticles
        .map { articles -> articles.map { it.categorie }.filter { it.isNotBlank() }.distinct().sorted() }
        .flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableTvas: StateFlow<List<String>> = allArticles
        .map { articles -> articles.map { it.tva }.filter { it.isNotBlank() }.distinct().sorted() }
        .flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Bornes min/max des prix (PPV) de la session, pour le slider de filtre. */
    val priceBounds: StateFlow<ClosedFloatingPointRange<Float>> = allArticles
        .map { articles ->
            val prices = articles.map { it.prixUnitaire.toFloat() }
            val min = prices.minOrNull() ?: 0f
            val max = prices.maxOrNull() ?: 0f
            min..max
        }
        .flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f..0f)

    /**
     * Nombre d'articles de la session correspondant à chaque filtre rapide (sur la session
     * entière, indépendamment des autres filtres et de la recherche). Lecture seule, pour
     * l'affichage des compteurs et de l'avancement.
     */
    val filterCounts: StateFlow<Map<ArticleFilter, Int>> = allArticles
        .map { articles -> ArticleFilters.counts(articles) }
        .flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val fieldFilters: Flow<FieldFilters> =
        combine(_positionFilter, _categoryFilter, _priceRangeFilter) { position, category, priceRange ->
            FieldFilters(position, category, priceRange)
        }

    /** Nombre d'articles de la session par zone, pour la feuille de filtre. */
    val positionCounts: StateFlow<Map<String, Int>> = allArticles
        .map { articles -> articles.filter { it.emplacement.isNotBlank() }.groupingBy { it.emplacement }.eachCount() }
        .flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** Nombre d'articles de la session par catégorie, pour la feuille de filtre. */
    val categoryCounts: StateFlow<Map<String, Int>> = allArticles
        .map { articles -> articles.filter { it.categorie.isNotBlank() }.groupingBy { it.categorie }.eachCount() }
        .flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /**
     * Nombre de produits qu'afficherait la liste avec ces filtres (recherche en cours comprise),
     * pour le bouton « Afficher N produits » de la feuille de filtre, avant de les appliquer.
     */
    fun previewCount(
        filter: ArticleFilter,
        position: String?,
        category: String?,
        priceRange: ClosedFloatingPointRange<Float>?
    ): Int = ArticleFilters.applyFilters(
        allArticles.value, filter, _searchQuery.value, FieldFilters(position, category, priceRange)
    ).size

    val visibleArticles: StateFlow<List<StockArticleEntity>> =
        combine(allArticles, _filter, _searchQuery, fieldFilters, _sort) { articles, filter, query, fields, sort ->
            ArticleFilters.sort(ArticleFilters.applyFilters(articles, filter, query, fields), sort)
        }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _exportEvents = Channel<ExportEvent>(Channel.BUFFERED)
    val exportEvents = _exportEvents.receiveAsFlow()

    fun setFilter(filter: ArticleFilter) {
        _filter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setPositionFilter(position: String?) {
        _positionFilter.value = position
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = category
    }

    fun setPriceRangeFilter(range: ClosedFloatingPointRange<Float>?) {
        _priceRangeFilter.value = range
    }

    fun setSort(sort: ArticleSort) {
        _sort.value = sort
    }

    fun updateQuantity(articleId: Long, quantiteReelle: Double, ecartValide: Boolean) {
        viewModelScope.launch {
            repository.updateQuantity(articleId, quantiteReelle, ecartValide)
        }
    }

    /** « Enregistrer » de l'étape « Corriger la quantité » : un écart restant est enregistré comme validé. */
    fun saveCorrection(articleId: Long, quantiteReelle: Double, quantiteSysteme: Double, remarque: String) {
        viewModelScope.launch {
            repository.recordCorrection(articleId, quantiteReelle, quantiteSysteme, remarque)
        }
    }

    fun createArticle(article: ImportedArticle) {
        viewModelScope.launch {
            repository.addArticle(sessionId, article)
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
