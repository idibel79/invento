package ma.bam.inventaire.ui.inventorydetail

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ma.bam.inventaire.R
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.data.repository.ImportedArticle
import ma.bam.inventaire.domain.EcartCalculator
import ma.bam.inventaire.ui.components.Pill
import ma.bam.inventaire.ui.components.PillShape
import ma.bam.inventaire.ui.components.EllipsisScrollBox
import ma.bam.inventaire.ui.components.rememberLongPressHaptic
import ma.bam.inventaire.ui.components.rememberTapHaptic
import ma.bam.inventaire.ui.quantityentry.EcartConfirmDialog
import ma.bam.inventaire.ui.quantityentry.QuantityEntryDialog
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamNavyFill
import ma.bam.inventaire.ui.theme.BamPrimaryFill
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.OnBrand
import ma.bam.inventaire.ui.theme.SelectedCardBg
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.StatusEcart
import ma.bam.inventaire.ui.theme.StatusEcartBg
import ma.bam.inventaire.ui.theme.StatusNegative
import ma.bam.inventaire.ui.theme.StatusNegativeBg
import ma.bam.inventaire.ui.theme.StatusPending
import ma.bam.inventaire.ui.theme.StatusPendingBg
import ma.bam.inventaire.ui.theme.StatusSuccess
import ma.bam.inventaire.ui.theme.StatusSuccessBg
import ma.bam.inventaire.ui.theme.TabularNums
import ma.bam.inventaire.ui.theme.TextMuted
import ma.bam.inventaire.ui.theme.TextPrimary
import ma.bam.inventaire.ui.theme.TextSecondary
import ma.bam.inventaire.util.formatQuantity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryDetailScreen(
    onBack: () -> Unit,
    onScan: (sessionId: String) -> Unit,
    viewModel: InventoryDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val tapHaptic = rememberTapHaptic()
    val session by viewModel.session.collectAsState()
    val articles by viewModel.visibleArticles.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val filterCounts by viewModel.filterCounts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val positionFilter by viewModel.positionFilter.collectAsState()
    val availablePositions by viewModel.availablePositions.collectAsState()
    val categoryFilter by viewModel.categoryFilter.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val availableTvas by viewModel.availableTvas.collectAsState()
    val priceRangeFilter by viewModel.priceRangeFilter.collectAsState()
    val priceBounds by viewModel.priceBounds.collectAsState()
    val sort by viewModel.sort.collectAsState()
    var editingArticle by remember { mutableStateOf<StockArticleEntity?>(null) }
    var pendingEcart by remember { mutableStateOf<PendingEcart?>(null) }
    var detailArticle by remember { mutableStateOf<StockArticleEntity?>(null) }
    val positionCounts by viewModel.positionCounts.collectAsState()
    val categoryCounts by viewModel.categoryCounts.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    // Pastille du bouton filtre : zone, catégorie et prix (le statut a ses propres puces).
    val activeFieldFilters = listOf(positionFilter != null, categoryFilter != null, priceRangeFilter != null).count { it }
    var showCreateDialog by remember { mutableStateOf(false) }
    var createdArticle by remember { mutableStateOf<ImportedArticle?>(null) }
    val locked = session?.verrouille == true
    val totalCount = filterCounts[ArticleFilter.TOUS] ?: 0
    val scannedCount = filterCounts[ArticleFilter.SCANNES] ?: 0

    LaunchedEffect(Unit) {
        viewModel.exportEvents.collect { event ->
            when (event) {
                is ExportEvent.Ready -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        putExtra(Intent.EXTRA_STREAM, event.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Partager l'inventaire"))
                }
                is ExportEvent.Error -> { /* affiché via le texte d'état si besoin */ }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            session?.numero ?: "Inventaire",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BamNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (session != null) {
                            Text(
                                "$totalCount article${if (totalCount > 1) "s" else ""} · ${if (locked) "clôturé" else "ouvert"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        tapHaptic()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            tapHaptic()
                            showCreateDialog = true
                        },
                        enabled = !locked
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add_product),
                            contentDescription = "Créer un produit",
                            modifier = Modifier
                                .size(26.dp)
                                .alpha(if (locked) 0.38f else 1f),
                            tint = Color.Unspecified
                        )
                    }
                    IconButton(onClick = {
                        tapHaptic()
                        viewModel.exportCurrentSession()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_export),
                            contentDescription = "Exporter",
                            modifier = Modifier.size(26.dp),
                            tint = Color.Unspecified
                        )
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            Button(
                onClick = {
                    tapHaptic()
                    onScan(viewModel.sessionId)
                },
                enabled = !locked,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 8.dp),
                contentPadding = PaddingValues(horizontal = 28.dp),
                modifier = Modifier.height(58.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_barcode_scan),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Scanner", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ProgressCard(
                scanned = scannedCount,
                total = totalCount,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::setSearchQuery,
                    placeholder = { Text("Code ou titre", color = BamNavy.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BamNavy) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Effacer")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = BamNavy,
                        unfocusedTextColor = BamNavy,
                        cursorColor = BamNavy
                    ),
                    modifier = Modifier.weight(1f)
                )
                SquareToolButton(
                    active = activeFieldFilters > 0,
                    badgeCount = activeFieldFilters,
                    onClick = {
                        tapHaptic()
                        showFilterSheet = true
                    }
                ) { tint ->
                    Icon(
                        imageVector = Icons.Filled.FilterAlt,
                        contentDescription = "Filtrer",
                        tint = tint
                    )
                }
                SquareToolButton(
                    active = sort != ArticleSort.DEFAUT,
                    onClick = {
                        tapHaptic()
                        showSortSheet = true
                    }
                ) { tint ->
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sort),
                        contentDescription = "Trier",
                        modifier = Modifier.size(24.dp),
                        tint = tint
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    ArticleFilter.TOUS to "Tous",
                    ArticleFilter.SCANNES to "Inventoriés",
                    ArticleFilter.ECARTS to "Écarts",
                    ArticleFilter.CORRIGES to "Corrigés",
                    ArticleFilter.NON_SCANNES to "Non inventoriés",
                    ArticleFilter.NEGATIFS to "Négatifs",
                    ArticleFilter.VIDE to "Vides"
                ).forEach { (value, label) ->
                    QuickFilterChip(
                        label = label,
                        count = filterCounts[value],
                        selected = filter == value,
                        onClick = {
                            tapHaptic()
                            viewModel.setFilter(value)
                        }
                    )
                }
            }

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
                        onEdit = { editingArticle = item },
                        onShowDetail = { detailArticle = item },
                        showRemarque = item.corrige
                    )
                }
            }
        }
    }

    editingArticle?.let { article ->
        QuantityEntryDialog(
            article = article,
            onDismiss = { editingArticle = null },
            onConfirm = { quantite ->
                // Même enchaînement qu'après un scan : un écart ouvre la popup « Écart détecté ».
                val ecart = EcartCalculator.compute(article.quantiteTheorique, quantite)
                if (EcartCalculator.hasEcart(ecart)) {
                    pendingEcart = PendingEcart(article, quantite, ecart)
                } else {
                    viewModel.updateQuantity(article.id, quantite, ecartValide = true)
                }
                editingArticle = null
            }
        )
    }

    pendingEcart?.let { pending ->
        EcartConfirmDialog(
            article = pending.article,
            quantiteReelle = pending.quantiteReelle,
            ecart = pending.ecart,
            onDismiss = { pendingEcart = null },
            onValidate = {
                viewModel.updateQuantity(pending.article.id, pending.quantiteReelle, ecartValide = true)
                pendingEcart = null
            },
            onSaveCorrection = { quantiteSysteme, remarque ->
                viewModel.saveCorrection(pending.article.id, pending.quantiteReelle, quantiteSysteme, remarque)
                pendingEcart = null
            }
        )
    }

    if (showFilterSheet) {
        FilterSheet(
            filter = filter,
            filterCounts = filterCounts,
            position = positionFilter,
            positions = availablePositions,
            positionCounts = positionCounts,
            category = categoryFilter,
            categories = availableCategories,
            categoryCounts = categoryCounts,
            priceRange = priceRangeFilter,
            priceBounds = priceBounds,
            previewCount = viewModel::previewCount,
            onApply = { newFilter, position, category, priceRange ->
                tapHaptic()
                viewModel.setFilter(newFilter)
                viewModel.setPositionFilter(position)
                viewModel.setCategoryFilter(category)
                viewModel.setPriceRangeFilter(priceRange)
                showFilterSheet = false
            },
            onReset = {
                viewModel.setFilter(ArticleFilter.TOUS)
                viewModel.setPositionFilter(null)
                viewModel.setCategoryFilter(null)
                viewModel.setPriceRangeFilter(null)
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    if (showSortSheet) {
        SortSheet(
            sort = sort,
            onSelect = { newSort ->
                tapHaptic()
                viewModel.setSort(newSort)
                showSortSheet = false
            },
            onDismiss = { showSortSheet = false }
        )
    }

    detailArticle?.let { article ->
        ArticleDetailDialog(
            article = article,
            onDismiss = { detailArticle = null }
        )
    }

    if (showCreateDialog) {
        CreateArticleDialog(
            availableCategories = availableCategories,
            availableTvas = availableTvas,
            availableZones = availablePositions,
            onDismiss = { showCreateDialog = false },
            onConfirm = { article ->
                viewModel.createArticle(article)
                showCreateDialog = false
                createdArticle = article
            }
        )
    }

    createdArticle?.let { article ->
        ArticleCreatedSheet(
            article = article,
            onCreateAnother = {
                createdArticle = null
                showCreateDialog = true
            },
            onDismiss = { createdArticle = null }
        )
    }
}

/** Carte d'avancement (fond bleu clair #CEDEFF comme les cartes En cours) : articles inventoriés / total, pourcentage et barre. */
@Composable
private fun ProgressCard(scanned: Int, total: Int, modifier: Modifier = Modifier) {
    val progress = if (total > 0) scanned.toFloat() / total.toFloat() else 0f
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SelectedCardBg)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Avancement",
                    style = MaterialTheme.typography.labelMedium,
                    color = BamNavy.copy(alpha = 0.7f)
                )
                Text(
                    "$scanned / $total",
                    style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = TabularNums),
                    fontWeight = FontWeight.Bold,
                    color = BamPrimary
                )
            }
            Text(
                "${(progress * 100).toInt()} %",
                fontSize = 30.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BamPrimary,
                style = LocalTextStyle.current.copy(fontFeatureSettings = TabularNums)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(PillShape),
            color = BamPrimary,
            trackColor = CardSurface,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
    }
}

/**
 * Bouton carré (filtre, tri) posé à côté de la recherche. Actif = fond primaire clair et
 * petit point en haut à droite.
 */
@Composable
private fun SquareToolButton(
    active: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0,
    icon: @Composable (tint: Color) -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(shape)
            .background(if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline,
                shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        icon(BamNavy)
        if (badgeCount > 0) {
            // Pastille avec le nombre de filtres actifs.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-5).dp, y = 5.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(BamPrimaryFill),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    badgeCount.toString(),
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBrand
                )
            }
        } else if (active) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-9).dp, y = 9.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

/** Filtre rapide en pilule ; sélectionné = fond foncé, texte blanc. Affiche le compteur s'il est connu. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickFilterChip(
    label: String,
    count: Int?,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(label, fontWeight = FontWeight.SemiBold)
            if (count != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    count.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = TabularNums),
                    fontWeight = FontWeight.Bold,
                    color = if (selected) OnBrand.copy(alpha = 0.7f) else BamNavy.copy(alpha = 0.7f)
                )
            }
        },
        shape = PillShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = CardSurface,
            labelColor = BamNavy,
            selectedContainerColor = BamNavyFill,
            selectedLabelColor = OnBrand
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = BamNavyFill
        )
    )
}

/**
 * Carte produit de la liste d'inventaire. Avec [onSelect] (saisie manuelle du scan), un appui
 * sélectionne le produit : pas d'appui long ni de crayon.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable

internal fun ArticleRow(
    item: StockArticleEntity,
    locked: Boolean,
    onEdit: () -> Unit,
    onShowDetail: () -> Unit,
    onSelect: (() -> Unit)? = null,
    /** Article corrigé : affiche la remarque (ou la mention de correction) sous les quantités. */
    showRemarque: Boolean = false
) {
    val article = item
    val tapHaptic = rememberTapHaptic()
    val longPressHaptic = rememberLongPressHaptic()
    val hasEcart = EcartCalculator.hasEcart(article.ecart)
    val notScanned = article.quantiteReelle == null
    // Rouge si |écart| >= 5, orange si écart plus petit (entre -5 et +5 exclus), vert si conforme,
    // gris si non inventorié.
    val ecartFaible = hasEcart && !EcartCalculator.isEcartFort(article.ecart)
    val ecartColor = if (ecartFaible) StatusNegative else StatusEcart
    val cardColor = when {
        notScanned -> StatusPendingBg
        ecartFaible -> StatusNegativeBg
        hasEcart -> StatusEcartBg
        else -> StatusSuccessBg
    }
    val titleColor = when {
        notScanned -> ArticleMutedColor
        hasEcart -> ecartColor
        else -> StatusSuccess
    }
    val infoTextColor = if (notScanned) TextSecondary else BamNavy
    val infoWeight = if (notScanned) FontWeight.Normal else FontWeight.SemiBold
    val infoIconColor = if (notScanned) TextMuted else TextSecondary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onSelect != null) Modifier.clickable(onClick = onSelect)
                else Modifier.combinedClickable(
                    onClick = {
                        if (notScanned && !locked) {
                            tapHaptic()
                            onEdit()
                        }
                    },
                    onLongClick = {
                        longPressHaptic()
                        onShowDetail()
                    }
                )
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            // Ligne 1 : titre qui défile + stylo (absent si non inventorié). Hauteur fixe
            // pour que toutes les cartes aient la même hauteur, avec ou sans stylo.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.heightIn(min = 24.dp)
            ) {
                val titleScrollState = rememberScrollState()
                EllipsisScrollBox(
                    scrollState = titleScrollState,
                    background = cardColor,
                    ellipsisColor = titleColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        article.designation.ifBlank { article.codeArticle },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip
                    )
                }
                if (!notScanned && !locked && onSelect == null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable {
                                tapHaptic()
                                onEdit()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Modifier la quantité",
                            tint = titleColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Ligne 2 : prix · code-barres.
            val codesBarres = listOf(article.codeBarre1, article.codeBarre2).filter { it.isNotBlank() }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Sell,
                    contentDescription = null,
                    tint = infoIconColor,
                    modifier = Modifier.size(14.dp)
                )
                ArticleInfoText("${formatQuantity(article.prixUnitaire)} DH", infoTextColor, infoWeight)
                if (codesBarres.isNotEmpty()) {
                    Text("·", fontSize = 12.5.sp, color = TextMuted, maxLines = 1, softWrap = false)
                    Icon(
                        painter = painterResource(id = R.drawable.ic_barcode_scan),
                        contentDescription = null,
                        tint = infoIconColor,
                        modifier = Modifier.size(14.dp)
                    )
                    ArticleInfoText(
                        codesBarres.joinToString(" / "),
                        infoTextColor,
                        infoWeight,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }

            // Ligne 3 : zone, sur sa propre ligne qui défile quand elle est longue.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = null,
                    tint = infoIconColor,
                    modifier = Modifier.size(14.dp)
                )
                val zoneScrollState = rememberScrollState()
                EllipsisScrollBox(
                    scrollState = zoneScrollState,
                    background = cardColor,
                    ellipsisColor = infoTextColor,
                    fontSize = 12.5.sp,
                    fontWeight = infoWeight,
                    modifier = Modifier.weight(1f)
                ) {
                    ArticleInfoText(article.emplacement, infoTextColor, infoWeight)
                }
            }

            // Tableau Système / Compté / Écart.
            Row(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardSurface.copy(alpha = 0.75f))
                    .padding(vertical = 4.dp)
            ) {
                QuantityCell(label = "Système", modifier = Modifier.weight(1f)) {
                    QuantityValue(formatQuantity(article.quantiteTheorique), TextPrimary)
                }
                QuantityCellDivider()
                QuantityCell(label = "Compté", modifier = Modifier.weight(1f).leftDivider(QuantityDividerColor)) {
                    QuantityValue(
                        article.quantiteReelle?.let { formatQuantity(it) } ?: "–",
                        if (notScanned) TextPrimary else BamPrimary
                    )
                }
                QuantityCellDivider()
                QuantityCell(label = if (article.corrige) "Corrigé" else "Écart", modifier = Modifier.weight(1f).leftDivider(QuantityDividerColor)) {
                    when {
                        notScanned -> QuantityValue("À compter", ArticleMutedColor, fontSize = 13.5.sp, fontWeight = FontWeight.Normal)
                        hasEcart -> QuantityValue(
                            article.ecart?.let { formatQuantity(it, showSign = true) } ?: "-",
                            ecartColor,
                            fontWeight = FontWeight.ExtraBold
                        )
                        else -> QuantityValue("Conforme", StatusSuccess, fontSize = 13.5.sp)
                    }
                }
            }
            if (showRemarque) {
                RemarqueNote(article.remarque, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

/** Saisie avec écart en attente de la popup « Écart détecté » (Valider ou Corriger). */
private data class PendingEcart(val article: StockArticleEntity, val quantiteReelle: Double, val ecart: Double)

private val ArticleMutedColor: Color
    @Composable get() = TextSecondary

@Composable
private fun ArticleInfoText(text: String, color: Color, fontWeight: FontWeight, modifier: Modifier = Modifier) {
    Text(
        text,
        fontSize = 12.5.sp,
        lineHeight = 17.sp,
        color = color,
        fontWeight = fontWeight,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        style = LocalTextStyle.current.copy(fontFeatureSettings = TabularNums),
        modifier = modifier
    )
}

@Composable
private fun QuantityCell(label: String, modifier: Modifier = Modifier, value: @Composable () -> Unit) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            fontSize = 10.5.sp,
            lineHeight = 13.sp,
            color = TextSecondary,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
        Box(modifier = Modifier.heightIn(min = 19.dp), contentAlignment = Alignment.Center) {
            value()
        }
    }
}

/** Espace de 1dp entre deux cellules ; le trait lui-même est dessiné par [leftDivider]. */
@Composable
private fun QuantityCellDivider() {
    Spacer(modifier = Modifier.width(1.dp))
}

private val QuantityDividerColor: Color
    @Composable get() = TextPrimary.copy(alpha = 0.08f)

/**
 * Dessine le séparateur fin dans l'espace de 1dp à gauche de la cellule, sur toute sa hauteur.
 * Remplace l'ancien `height(IntrinsicSize.Min)` + `fillMaxHeight()`, dont la mesure intrinsèque
 * doublait le coût de mise en page de chaque carte pendant le défilement.
 */
private fun Modifier.leftDivider(color: Color): Modifier = drawBehind {
    val w = 1.dp.toPx()
    drawRect(color, topLeft = Offset(-w, 0f), size = Size(w, size.height))
}

@Composable
private fun QuantityValue(
    text: String,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit = 15.sp,
    fontWeight: FontWeight = FontWeight.Bold
) {
    Text(
        text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        style = LocalTextStyle.current.copy(fontFeatureSettings = TabularNums)
    )
}

/**
 * Article corrigé : mention « Quantité système corrigée par l'inventoriste » toujours affichée,
 * suivie de la remarque éventuelle dans un encadré blanc avec le texte entier.
 */
@Composable
private fun RemarqueNote(remarque: String?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(13.dp)
        )
        Text("Quantité système corrigée par l'inventoriste", fontSize = 12.sp, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, color = TextMuted)
    }
    if (remarque != null) {
        Row(
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(9.dp))
                .background(CardSurface)
                .padding(horizontal = 9.dp, vertical = 7.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.StickyNote2,
                contentDescription = null,
                tint = BamNavy,
                modifier = Modifier.padding(top = 1.dp).size(14.dp)
            )
            Text(remarque, fontSize = 12.5.sp, lineHeight = 17.5.sp, color = TextPrimary)
        }
    }
}
