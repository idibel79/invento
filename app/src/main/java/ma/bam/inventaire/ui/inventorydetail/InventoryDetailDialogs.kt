package ma.bam.inventaire.ui.inventorydetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.data.repository.ImportedArticle
import ma.bam.inventaire.domain.ArticleFilter
import ma.bam.inventaire.domain.EcartCalculator
import ma.bam.inventaire.ui.components.rememberTapHaptic
import ma.bam.inventaire.ui.quantityentry.EcartConfirmDialog
import ma.bam.inventaire.ui.quantityentry.QuantityEntryDialog

/** Saisie avec écart en attente de la popup « Écart détecté » (Valider ou Corriger). */
internal data class PendingEcart(val article: StockArticleEntity, val quantiteReelle: Double, val ecart: Double)

/** Popups et feuilles ouvertes sur la page produits (état d'affichage local à l'écran). */
@Stable
internal class InventoryDetailDialogs {
    var editingArticle by mutableStateOf<StockArticleEntity?>(null)

    /** Quantité reprise dans « Saisir quantité » au retour depuis « Écart détecté » (null = quantité comptée). */
    var editingQuantity by mutableStateOf<Double?>(null)
    var pendingEcart by mutableStateOf<PendingEcart?>(null)
    var detailArticle by mutableStateOf<StockArticleEntity?>(null)
    var showFilterSheet by mutableStateOf(false)
    var showSortSheet by mutableStateOf(false)
    var showCreateDialog by mutableStateOf(false)
    var createdArticle by mutableStateOf<ImportedArticle?>(null)
}

@Composable
internal fun DetailDialogsHost(dialogs: InventoryDetailDialogs, uiState: InventoryDetailUiState, actions: InventoryDetailActions) {
    QuantityDialogs(dialogs, actions)
    FilterAndSortSheets(dialogs, uiState, actions)

    dialogs.detailArticle?.let { article ->
        ArticleDetailDialog(
            article = article,
            onDismiss = { dialogs.detailArticle = null }
        )
    }

    if (dialogs.showCreateDialog) {
        CreateArticleDialog(
            availableCategories = uiState.stats.categories,
            availableTvas = uiState.stats.tvas,
            availableZones = uiState.stats.positions,
            onDismiss = { dialogs.showCreateDialog = false },
            onConfirm = { article ->
                actions.onCreateArticle(article)
                dialogs.showCreateDialog = false
                dialogs.createdArticle = article
            }
        )
    }

    dialogs.createdArticle?.let { article ->
        ArticleCreatedSheet(
            article = article,
            onCreateAnother = {
                dialogs.createdArticle = null
                dialogs.showCreateDialog = true
            },
            onDismiss = { dialogs.createdArticle = null }
        )
    }
}

/** Saisie de quantité puis, en cas d'écart, popup « Écart détecté » (même enchaînement qu'au scan). */
@Composable
private fun QuantityDialogs(dialogs: InventoryDetailDialogs, actions: InventoryDetailActions) {
    dialogs.editingArticle?.let { article ->
        QuantityEntryDialog(
            article = article,
            initialQuantity = dialogs.editingQuantity ?: article.quantiteReelle,
            onDismiss = {
                dialogs.editingArticle = null
                dialogs.editingQuantity = null
            },
            onConfirm = { quantite ->
                val ecart = EcartCalculator.compute(article.quantiteTheorique, quantite)
                if (EcartCalculator.hasEcart(ecart)) {
                    dialogs.pendingEcart = PendingEcart(article, quantite, ecart)
                } else {
                    actions.onUpdateQuantity(article.id, quantite)
                }
                dialogs.editingArticle = null
                dialogs.editingQuantity = null
            }
        )
    }

    dialogs.pendingEcart?.let { pending ->
        EcartConfirmDialog(
            article = pending.article,
            quantiteReelle = pending.quantiteReelle,
            ecart = pending.ecart,
            onDismiss = { dialogs.pendingEcart = null },
            onBack = {
                dialogs.pendingEcart = null
                dialogs.editingQuantity = pending.quantiteReelle
                dialogs.editingArticle = pending.article
            },
            onValidate = {
                actions.onUpdateQuantity(pending.article.id, pending.quantiteReelle)
                dialogs.pendingEcart = null
            },
            onSaveCorrection = { remarque ->
                actions.onSaveCorrection(pending.article.id, pending.quantiteReelle, remarque)
                dialogs.pendingEcart = null
            }
        )
    }
}

@Composable
private fun FilterAndSortSheets(dialogs: InventoryDetailDialogs, uiState: InventoryDetailUiState, actions: InventoryDetailActions) {
    val tapHaptic = rememberTapHaptic()
    if (dialogs.showFilterSheet) {
        val stats = uiState.stats
        FilterSheet(
            filter = uiState.filter,
            filterCounts = stats.filterCounts,
            position = uiState.positionFilter,
            positions = stats.positions,
            positionCounts = stats.positionCounts,
            category = uiState.categoryFilter,
            categories = stats.categories,
            categoryCounts = stats.categoryCounts,
            priceRange = uiState.priceRangeFilter,
            priceBounds = stats.priceBounds,
            appliedCount = uiState.articles.size,
            previewCount = actions.previewCount,
            onApply = { newFilter, position, category, priceRange ->
                tapHaptic()
                actions.onFilterChange(newFilter)
                actions.onApplyFieldFilters(position, category, priceRange)
                dialogs.showFilterSheet = false
            },
            onReset = {
                actions.onFilterChange(ArticleFilter.TOUS)
                actions.onApplyFieldFilters(null, null, null)
            },
            onDismiss = { dialogs.showFilterSheet = false }
        )
    }

    if (dialogs.showSortSheet) {
        SortSheet(
            sort = uiState.sort,
            onSelect = { newSort ->
                tapHaptic()
                actions.onSortChange(newSort)
                dialogs.showSortSheet = false
            },
            onDismiss = { dialogs.showSortSheet = false }
        )
    }
}
