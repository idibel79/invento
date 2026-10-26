package ma.bam.inventaire.ui.inventorydetail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.bam.inventaire.data.export.SessionExporter
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.data.repository.ImportedArticle
import ma.bam.inventaire.data.repository.InventoryRepository
import ma.bam.inventaire.di.DefaultDispatcher
import ma.bam.inventaire.domain.ArticleFilter
import ma.bam.inventaire.domain.ArticleFilters
import ma.bam.inventaire.domain.ArticleSort
import ma.bam.inventaire.domain.ArticleStats
import ma.bam.inventaire.domain.FieldFilters

sealed interface ExportEvent {
    data class Ready(val uri: Uri) : ExportEvent
    data class Error(val message: String) : ExportEvent
}

/** Choix de l'utilisateur sur la page produits : filtre rapide, recherche, filtres de champ et tri. */
data class ArticleQuery(
    val filter: ArticleFilter = ArticleFilter.TOUS,
    val searchQuery: String = "",
    val fields: FieldFilters = FieldFilters(),
    val sort: ArticleSort = ArticleSort.DEFAUT
)

/** État complet de la page produits. */
data class InventoryDetailUiState(
    val session: InventorySessionEntity? = null,
    /** Articles affichés : filtrés, recherchés et triés selon [query]. */
    val articles: List<StockArticleEntity> = emptyList(),
    val query: ArticleQuery = ArticleQuery(),
    /** Agrégats sur la session entière (compteurs, zones, catégories, TVA, bornes de prix). */
    val stats: ArticleStats = ArticleStats()
) {
    val filter: ArticleFilter get() = query.filter
    val searchQuery: String get() = query.searchQuery

    /** Zone sélectionnée, ou null = toutes les zones. */
    val positionFilter: String? get() = query.fields.position

    /** Catégorie sélectionnée, ou null = toutes les catégories. */
    val categoryFilter: String? get() = query.fields.category

    /** Plage de prix (PPV) sélectionnée, ou null = tous les prix. */
    val priceRangeFilter: ClosedFloatingPointRange<Float>? get() = query.fields.priceRange
    val sort: ArticleSort get() = query.sort
}

@HiltViewModel
class InventoryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: InventoryRepository,
    private val sessionExporter: SessionExporter,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    private val query = MutableStateFlow(ArticleQuery())

    private val session: StateFlow<InventorySessionEntity?> = repository.observeSession(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val allArticles: StateFlow<List<StockArticleEntity>> = repository.observeArticles(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Agrégats recalculés (en une passe) seulement quand la liste change, pas à chaque frappe. */
    private val stats = allArticles
        .map { ArticleStats.of(it) }
        .flowOn(defaultDispatcher)

    private val visibleArticles = combine(allArticles, query) { articles, q ->
        ArticleFilters.sort(ArticleFilters.applyFilters(articles, q.filter, q.searchQuery, q.fields), q.sort)
    }.flowOn(defaultDispatcher)

    /*
     * La requête est lue directement (sans passer par le calcul en arrière-plan) pour que le champ
     * de recherche et les puces réagissent immédiatement ; la liste suit dès qu'elle est recalculée.
     */
    val uiState: StateFlow<InventoryDetailUiState> =
        combine(session, query, visibleArticles, stats) { session, q, articles, stats ->
            InventoryDetailUiState(session = session, articles = articles, query = q, stats = stats)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InventoryDetailUiState())

    private val _exportEvents = Channel<ExportEvent>(Channel.BUFFERED)
    val exportEvents = _exportEvents.receiveAsFlow()

    /**
     * Nombre de produits qu'afficherait la liste avec ces filtres (recherche en cours comprise),
     * pour le bouton « Afficher N produits » de la feuille de filtre, avant de les appliquer.
     * Calculé hors du thread principal.
     */
    suspend fun previewCount(
        filter: ArticleFilter,
        position: String?,
        category: String?,
        priceRange: ClosedFloatingPointRange<Float>?
    ): Int {
        val articles = allArticles.value
        val search = query.value.searchQuery
        return withContext(defaultDispatcher) {
            ArticleFilters.applyFilters(articles, filter, search, FieldFilters(position, category, priceRange)).size
        }
    }

    fun setFilter(filter: ArticleFilter) {
        query.update { it.copy(filter = filter) }
    }

    fun setSearchQuery(searchQuery: String) {
        query.update { it.copy(searchQuery = searchQuery) }
    }

    fun setPositionFilter(position: String?) {
        query.update { it.copy(fields = it.fields.copy(position = position)) }
    }

    fun setCategoryFilter(category: String?) {
        query.update { it.copy(fields = it.fields.copy(category = category)) }
    }

    fun setPriceRangeFilter(range: ClosedFloatingPointRange<Float>?) {
        query.update { it.copy(fields = it.fields.copy(priceRange = range)) }
    }

    fun setSort(sort: ArticleSort) {
        query.update { it.copy(sort = sort) }
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

    @Suppress("TooGenericExceptionCaught") // toute erreur d'écriture devient un message affiché
    fun exportCurrentSession() {
        viewModelScope.launch {
            try {
                val sessionSnapshot = checkNotNull(session.value) { "Session introuvable" }
                val uri = sessionExporter.export(sessionSnapshot, allArticles.value)
                _exportEvents.send(ExportEvent.Ready(uri))
            } catch (e: Exception) {
                _exportEvents.send(ExportEvent.Error("Échec de l'export : ${e.message}"))
            }
        }
    }
}
