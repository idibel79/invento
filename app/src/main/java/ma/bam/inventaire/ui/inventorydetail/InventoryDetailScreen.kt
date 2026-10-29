package ma.bam.inventaire.ui.inventorydetail

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ma.bam.inventaire.R
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.data.repository.ImportedArticle
import ma.bam.inventaire.domain.ArticleFilter
import ma.bam.inventaire.domain.ArticleSort
import ma.bam.inventaire.ui.components.rememberTapHaptic
import ma.bam.inventaire.ui.theme.LightDarkPreviews
import ma.bam.inventaire.ui.theme.PreviewSurface
import ma.bam.inventaire.ui.theme.StatusNegative
import ma.bam.inventaire.ui.theme.StatusNegativeBg

private const val XLSX_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

/** Écran « page produits » relié au ViewModel : collecte l'état et l'export, puis délègue l'affichage. */
@Composable
fun InventoryDetailScreen(onBack: () -> Unit, onScan: (sessionId: String) -> Unit, viewModel: InventoryDetailViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.exportEvents.collect { event ->
            when (event) {
                is ExportEvent.Ready -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = XLSX_MIME
                        putExtra(Intent.EXTRA_STREAM, event.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooserTitle = context.getString(R.string.detail_share_inventory)
                    context.startActivity(Intent.createChooser(intent, chooserTitle))
                }
                is ExportEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val actions = remember(viewModel, onBack, onScan) {
        InventoryDetailActions(
            onBack = onBack,
            onScan = { onScan(viewModel.sessionId) },
            onExport = viewModel::exportCurrentSession,
            onSearchQueryChange = viewModel::setSearchQuery,
            onFilterChange = viewModel::setFilter,
            onApplyFieldFilters = { position, category, priceRange ->
                viewModel.setPositionFilter(position)
                viewModel.setCategoryFilter(category)
                viewModel.setPriceRangeFilter(priceRange)
            },
            onSortChange = viewModel::setSort,
            previewCount = viewModel::previewCount,
            onUpdateQuantity = { id, quantite -> viewModel.updateQuantity(id, quantite, ecartValide = true) },
            onSaveCorrection = viewModel::saveCorrection,
            onCreateArticle = viewModel::createArticle
        )
    }

    InventoryDetailContent(uiState = uiState, snackbarHostState = snackbarHostState, actions = actions)
}

/** Actions de la page produits transmises au composable sans état. */
data class InventoryDetailActions(
    val onBack: () -> Unit,
    val onScan: () -> Unit,
    val onExport: () -> Unit,
    val onSearchQueryChange: (String) -> Unit,
    val onFilterChange: (ArticleFilter) -> Unit,
    /** Zone, catégorie et plage de prix (null = pas de filtre). */
    val onApplyFieldFilters: (String?, String?, ClosedFloatingPointRange<Float>?) -> Unit,
    val onSortChange: (ArticleSort) -> Unit,
    val previewCount: suspend (ArticleFilter, String?, String?, ClosedFloatingPointRange<Float>?) -> Int,
    /** Enregistre une quantité comptée (écart éventuel validé). */
    val onUpdateQuantity: (articleId: Long, quantite: Double) -> Unit,
    val onSaveCorrection: (articleId: Long, quantiteReelle: Double, remarque: String) -> Unit,
    val onCreateArticle: (ImportedArticle) -> Unit
)

/** Page produits sans état ViewModel : barre du haut, avancement, recherche, puces et liste. */
@Composable
fun InventoryDetailContent(uiState: InventoryDetailUiState, snackbarHostState: SnackbarHostState, actions: InventoryDetailActions) {
    val tapHaptic = rememberTapHaptic()
    val dialogs = remember { InventoryDetailDialogs() }
    val filterCounts = uiState.stats.filterCounts
    val locked = uiState.session?.verrouille == true
    val totalCount = filterCounts[ArticleFilter.TOUS] ?: 0
    // Pastille du bouton filtre : zone, catégorie et prix (le statut a ses propres puces).
    val activeFieldFilters = listOf(uiState.positionFilter, uiState.categoryFilter, uiState.priceRangeFilter)
        .count { it != null }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { ErrorSnackbarHost(snackbarHostState) },
        topBar = {
            InventoryDetailTopBar(
                session = uiState.session,
                totalCount = totalCount,
                onBack = actions.onBack,
                onCreate = { dialogs.showCreateDialog = true },
                onExport = actions.onExport
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = { ScanButton(enabled = !locked, onClick = actions.onScan) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ProgressCard(
                scanned = filterCounts[ArticleFilter.SCANNES] ?: 0,
                total = totalCount,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            ArticleSearchRow(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = actions.onSearchQueryChange,
                activeFieldFilters = activeFieldFilters,
                sortActive = uiState.sort != ArticleSort.DEFAUT,
                onOpenFilters = { dialogs.showFilterSheet = true },
                onOpenSort = { dialogs.showSortSheet = true }
            )
            QuickFilterRow(
                selected = uiState.filter,
                filterCounts = filterCounts,
                onSelect = { value ->
                    tapHaptic()
                    actions.onFilterChange(value)
                }
            )
            ArticleList(
                articles = uiState.articles,
                locked = locked,
                onEdit = { dialogs.editingArticle = it },
                onShowDetail = { dialogs.detailArticle = it }
            )
        }
    }

    DetailDialogsHost(dialogs = dialogs, uiState = uiState, actions = actions)
}

@Composable
private fun ErrorSnackbarHost(snackbarHostState: SnackbarHostState) {
    SnackbarHost(snackbarHostState) { data ->
        Snackbar(
            snackbarData = data,
            shape = RoundedCornerShape(14.dp),
            containerColor = StatusNegativeBg,
            contentColor = StatusNegative
        )
    }
}

@Composable
private fun ArticleList(
    articles: List<StockArticleEntity>,
    locked: Boolean,
    onEdit: (StockArticleEntity) -> Unit,
    onShowDetail: (StockArticleEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // Marge basse pour que le dernier article ne reste pas caché sous le bouton « Scanner ».
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(articles, key = { it.id }) { item ->
            ArticleRow(
                item = item,
                locked = locked,
                onEdit = { onEdit(item) },
                onShowDetail = { onShowDetail(item) },
                showRemarque = item.corrige
            )
        }
    }
}

private val NoActions = InventoryDetailActions(
    onBack = {},
    onScan = {},
    onExport = {},
    onSearchQueryChange = {},
    onFilterChange = {},
    onApplyFieldFilters = { _, _, _ -> },
    onSortChange = {},
    previewCount = { _, _, _, _ -> 0 },
    onUpdateQuantity = { _, _ -> },
    onSaveCorrection = { _, _, _ -> },
    onCreateArticle = {}
)

@LightDarkPreviews
@Composable
private fun InventoryDetailContentPreview() {
    PreviewSurface {
        InventoryDetailContent(
            uiState = PreviewArticles.uiState,
            snackbarHostState = remember { SnackbarHostState() },
            actions = NoActions
        )
    }
}

@LightDarkPreviews
@Composable
private fun InventoryDetailLockedPreview() {
    PreviewSurface {
        InventoryDetailContent(
            uiState = PreviewArticles.uiState.copy(session = PreviewArticles.session.copy(verrouille = true)),
            snackbarHostState = remember { SnackbarHostState() },
            actions = NoActions
        )
    }
}
