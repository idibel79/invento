package ma.bam.inventaire.ui.inventorydetail

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ma.bam.inventaire.R
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.data.repository.ImportedArticle
import ma.bam.inventaire.domain.EcartCalculator
import ma.bam.inventaire.ui.components.fadingEdge
import ma.bam.inventaire.ui.quantityentry.QuantityEntryDialog
import ma.bam.inventaire.ui.theme.EcartRed
import ma.bam.inventaire.ui.theme.InProgressBlue
import ma.bam.inventaire.ui.theme.NotScannedGray
import ma.bam.inventaire.ui.theme.SuccessGreen
import ma.bam.inventaire.util.formatQuantity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryDetailScreen(
    onBack: () -> Unit,
    onScan: (sessionId: String) -> Unit,
    viewModel: InventoryDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val session by viewModel.session.collectAsState()
    val articles by viewModel.visibleArticles.collectAsState()
    val filter by viewModel.filter.collectAsState()
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
    var detailArticle by remember { mutableStateOf<StockArticleEntity?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var filterSection by remember { mutableStateOf<FilterSection?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var createdArticle by remember { mutableStateOf<ImportedArticle?>(null) }
    val locked = session?.verrouille == true

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
        topBar = {
            TopAppBar(
                title = { Text(session?.numero ?: "Inventaire") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }, enabled = !locked) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add_product),
                            contentDescription = "Créer un produit",
                            modifier = Modifier.size(30.dp),
                            tint = if (locked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { viewModel.exportCurrentSession() }) {
                        Icon(Icons.Default.Share, contentDescription = "Exporter")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::setSearchQuery,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Code ou titre") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Effacer")
                            }
                        }
                    },
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { onScan(viewModel.sessionId) }, enabled = !locked) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_barcode_scan),
                        contentDescription = "Scanner un article",
                        modifier = Modifier.size(34.dp),
                        tint = if (locked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        IconButton(
                            onClick = {
                                filterSection = null
                                showFilterMenu = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FilterAlt,
                                contentDescription = "Filtrer",
                                tint = if (positionFilter != null || categoryFilter != null || priceRangeFilter != null) {
                                    InProgressBlue
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = {
                                showFilterMenu = false
                                filterSection = null
                            },
                            modifier = Modifier
                                .width(280.dp)
                                .heightIn(max = 420.dp)
                        ) {
                            when (filterSection) {
                                null -> {
                                    DropdownMenuItem(
                                        text = { Text("Tous") },
                                        onClick = {
                                            viewModel.setPositionFilter(null)
                                            viewModel.setCategoryFilter(null)
                                            viewModel.setPriceRangeFilter(null)
                                            showFilterMenu = false
                                        },
                                        leadingIcon = {
                                            PositionCheckSlot(
                                                selected = positionFilter == null &&
                                                    categoryFilter == null &&
                                                    priceRangeFilter == null
                                            )
                                        }
                                    )
                                    Divider()
                                    Text(
                                        "Filtrer par",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                    Divider()
                                    DropdownMenuItem(
                                        text = { Text("Catégories") },
                                        onClick = { filterSection = FilterSection.CATEGORIE },
                                        trailingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) },
                                        colors = filterSectionItemColors(categoryFilter != null)
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Prix") },
                                        onClick = { filterSection = FilterSection.PRIX },
                                        trailingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) },
                                        colors = filterSectionItemColors(priceRangeFilter != null)
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Zones") },
                                        onClick = { filterSection = FilterSection.ZONES },
                                        trailingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) },
                                        colors = filterSectionItemColors(positionFilter != null)
                                    )
                                }
                                FilterSection.ZONES -> {
                                    DropdownMenuItem(
                                        text = { Text("Zones", fontWeight = FontWeight.SemiBold) },
                                        onClick = { filterSection = null },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                                    )
                                    Divider()
                                    DropdownMenuItem(
                                        text = { Text("Toutes les zones") },
                                        onClick = {
                                            viewModel.setPositionFilter(null)
                                            showFilterMenu = false
                                        },
                                        leadingIcon = { PositionCheckSlot(selected = positionFilter == null) }
                                    )
                                    availablePositions.forEach { position ->
                                        DropdownMenuItem(
                                            text = { Text(position) },
                                            onClick = {
                                                viewModel.setPositionFilter(position)
                                                showFilterMenu = false
                                            },
                                            leadingIcon = { PositionCheckSlot(selected = positionFilter == position) }
                                        )
                                    }
                                }
                                FilterSection.CATEGORIE -> {
                                    DropdownMenuItem(
                                        text = { Text("Catégories", fontWeight = FontWeight.SemiBold) },
                                        onClick = { filterSection = null },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                                    )
                                    Divider()
                                    DropdownMenuItem(
                                        text = { Text("Toutes les catégories") },
                                        onClick = {
                                            viewModel.setCategoryFilter(null)
                                            showFilterMenu = false
                                        },
                                        leadingIcon = { PositionCheckSlot(selected = categoryFilter == null) }
                                    )
                                    availableCategories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category) },
                                            onClick = {
                                                viewModel.setCategoryFilter(category)
                                                showFilterMenu = false
                                            },
                                            leadingIcon = { PositionCheckSlot(selected = categoryFilter == category) }
                                        )
                                    }
                                }
                                FilterSection.PRIX -> {
                                    DropdownMenuItem(
                                        text = { Text("Prix", fontWeight = FontWeight.SemiBold) },
                                        onClick = { filterSection = null },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                                    )
                                    Divider()
                                    PriceRangeFilterContent(
                                        bounds = priceBounds,
                                        currentRange = priceRangeFilter,
                                        onApply = { range ->
                                            viewModel.setPriceRangeFilter(range)
                                            showFilterMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_sort),
                                contentDescription = "Trier",
                                modifier = Modifier.size(24.dp),
                                tint = if (sort != ArticleSort.DEFAUT) InProgressBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            Text(
                                "Trier par",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Ordre d'origine") },
                                onClick = {
                                    viewModel.setSort(ArticleSort.DEFAUT)
                                    showSortMenu = false
                                },
                                leadingIcon = { PositionCheckSlot(selected = sort == ArticleSort.DEFAUT) }
                            )
                            DropdownMenuItem(
                                text = { Text("Titre (A-Z)") },
                                onClick = {
                                    viewModel.setSort(ArticleSort.TITRE_AZ)
                                    showSortMenu = false
                                },
                                leadingIcon = { PositionCheckSlot(selected = sort == ArticleSort.TITRE_AZ) }
                            )
                            DropdownMenuItem(
                                text = { Text("Titre (Z-A)") },
                                onClick = {
                                    viewModel.setSort(ArticleSort.TITRE_ZA)
                                    showSortMenu = false
                                },
                                leadingIcon = { PositionCheckSlot(selected = sort == ArticleSort.TITRE_ZA) }
                            )
                            DropdownMenuItem(
                                text = { Text("Prix (Min-Max)") },
                                onClick = {
                                    viewModel.setSort(ArticleSort.PRIX_ASC)
                                    showSortMenu = false
                                },
                                leadingIcon = { PositionCheckSlot(selected = sort == ArticleSort.PRIX_ASC) }
                            )
                            DropdownMenuItem(
                                text = { Text("Prix (Max-Min)") },
                                onClick = {
                                    viewModel.setSort(ArticleSort.PRIX_DESC)
                                    showSortMenu = false
                                },
                                leadingIcon = { PositionCheckSlot(selected = sort == ArticleSort.PRIX_DESC) }
                            )
                            DropdownMenuItem(
                                text = { Text("Quantité (Min-Max)") },
                                onClick = {
                                    viewModel.setSort(ArticleSort.QUANTITE_ASC)
                                    showSortMenu = false
                                },
                                leadingIcon = { PositionCheckSlot(selected = sort == ArticleSort.QUANTITE_ASC) }
                            )
                            DropdownMenuItem(
                                text = { Text("Quantité (Max-Min)") },
                                onClick = {
                                    viewModel.setSort(ArticleSort.QUANTITE_DESC)
                                    showSortMenu = false
                                },
                                leadingIcon = { PositionCheckSlot(selected = sort == ArticleSort.QUANTITE_DESC) }
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filter == ArticleFilter.TOUS,
                        onClick = { viewModel.setFilter(ArticleFilter.TOUS) },
                        label = { Text("Tous") }
                    )
                    FilterChip(
                        selected = filter == ArticleFilter.SCANNES,
                        onClick = { viewModel.setFilter(ArticleFilter.SCANNES) },
                        label = { Text("Inventoriés") }
                    )
                    FilterChip(
                        selected = filter == ArticleFilter.ECARTS,
                        onClick = { viewModel.setFilter(ArticleFilter.ECARTS) },
                        label = { Text("Écarts") }
                    )
                    FilterChip(
                        selected = filter == ArticleFilter.NON_SCANNES,
                        onClick = { viewModel.setFilter(ArticleFilter.NON_SCANNES) },
                        label = { Text("Non inventoriés") }
                    )
                    FilterChip(
                        selected = filter == ArticleFilter.NEGATIFS,
                        onClick = { viewModel.setFilter(ArticleFilter.NEGATIFS) },
                        label = { Text("Négatifs") }
                    )
                    FilterChip(
                        selected = filter == ArticleFilter.VIDE,
                        onClick = { viewModel.setFilter(ArticleFilter.VIDE) },
                        label = { Text("Vides") }
                    )
                }
            }
            Divider()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(articles, key = { it.id }) { item ->
                    ArticleRow(
                        item = item,
                        locked = locked,
                        onEdit = { editingArticle = item },
                        onShowDetail = { detailArticle = item }
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
                val ecart = EcartCalculator.compute(article.quantiteTheorique, quantite)
                viewModel.updateQuantity(
                    articleId = article.id,
                    quantiteReelle = quantite,
                    ecartValide = !EcartCalculator.hasEcart(ecart)
                )
                editingArticle = null
            }
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
        ArticleCreatedDialog(
            article = article,
            onDismiss = { createdArticle = null }
        )
    }
}

/** Coche si sélectionné, sinon un espace de même taille pour garder le texte des items alignés. */
@Composable
private fun PositionCheckSlot(selected: Boolean) {
    if (selected) {
        Icon(Icons.Filled.Check, contentDescription = null, tint = InProgressBlue)
    } else {
        Spacer(modifier = Modifier.size(24.dp))
    }
}

private enum class FilterSection { ZONES, CATEGORIE, PRIX }

@Composable
private fun filterSectionItemColors(active: Boolean): MenuItemColors {
    val color = if (active) InProgressBlue else MaterialTheme.colorScheme.onSurface
    return MenuDefaults.itemColors(textColor = color, trailingIconColor = color)
}

/** Slider min/max de prix pour le filtre. [bounds] a une portée nulle si la session n'a qu'un seul prix. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriceRangeFilterContent(
    bounds: ClosedFloatingPointRange<Float>,
    currentRange: ClosedFloatingPointRange<Float>?,
    onApply: (ClosedFloatingPointRange<Float>?) -> Unit
) {
    var sliderValue by remember(bounds) { mutableStateOf(currentRange ?: bounds) }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PriceBadge(sliderValue.start)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            PriceBadge(sliderValue.endInclusive)
        }
        if (bounds.start < bounds.endInclusive) {
            Spacer(modifier = Modifier.height(4.dp))
            RangeSlider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = bounds,
                colors = SliderDefaults.colors(
                    thumbColor = InProgressBlue,
                    activeTrackColor = InProgressBlue,
                    inactiveTrackColor = InProgressBlue.copy(alpha = 0.2f)
                ),
                startThumb = { CircleThumb() },
                endThumb = { CircleThumb() },
                track = { state ->
                    SliderDefaults.Track(
                        rangeSliderState = state,
                        modifier = Modifier.height(4.dp),
                        colors = SliderDefaults.colors(
                            activeTrackColor = InProgressBlue,
                            inactiveTrackColor = InProgressBlue.copy(alpha = 0.2f)
                        ),
                        thumbTrackGapSize = 0.dp
                    )
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${formatQuantity(bounds.start.toDouble())} DH",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${formatQuantity(bounds.endInclusive.toDouble())} DH",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Un seul prix disponible",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                sliderValue = bounds
                onApply(null)
            }) { Text("Réinitialiser") }
            TextButton(onClick = { onApply(sliderValue) }) {
                Text("Appliquer", color = SuccessGreen, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PriceBadge(value: Float) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(InProgressBlue.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            "${formatQuantity(value.toDouble())} DH",
            style = MaterialTheme.typography.labelMedium,
            color = InProgressBlue,
            fontWeight = FontWeight.Medium
        )
    }
}

/** Pouce rond pour le slider de prix (au lieu de la barre par défaut de Material 3). */
@Composable
private fun CircleThumb() {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(InProgressBlue),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ArticleRow(
    item: StockArticleEntity,
    locked: Boolean,
    onEdit: () -> Unit,
    onShowDetail: () -> Unit
) {
    val article = item
    val hasEcart = EcartCalculator.hasEcart(article.ecart)
    val notScanned = article.quantiteReelle == null
    val ecartColor = article.ecart?.let { if (it < 0) EcartRed else InProgressBlue }
    val statusColor = when {
        notScanned -> NotScannedGray
        hasEcart -> EcartRed
        else -> SuccessGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (notScanned && !locked) onEdit() },
                onLongClick = onShowDetail
            ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val titleScrollState = rememberScrollState()
                Text(
                    article.designation.ifBlank { article.codeArticle },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = InProgressBlue,
                    maxLines = 1,
                    softWrap = false,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Clip,
                    modifier = Modifier
                        .fadingEdge(titleScrollState)
                        .horizontalScroll(titleScrollState)
                )
                Spacer(modifier = Modifier.height(2.dp))
                ArticleInfoRow(
                    icon = Icons.Filled.Sell,
                    text = "${formatQuantity(article.prixUnitaire)} DH"
                )
                val codesBarres = listOf(article.codeBarre1, article.codeBarre2).filter { it.isNotBlank() }
                if (codesBarres.isNotEmpty()) {
                    ArticleInfoRow(
                        painter = painterResource(id = R.drawable.ic_barcode_scan),
                        text = codesBarres.joinToString(" / ")
                    )
                }
                ArticleInfoRow(icon = Icons.Filled.Place, text = article.emplacement)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!notScanned) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(statusColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasEcart) Icons.Filled.Error else Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    val pillsScrollState = rememberScrollState()
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fadingEdge(pillsScrollState)
                            .horizontalScroll(pillsScrollState)
                    ) {
                        QuantityPill(label = "Système :", value = formatQuantity(article.quantiteTheorique))
                        QuantityPill(
                            label = "Inventaire :",
                            value = article.quantiteReelle?.let { formatQuantity(it) } ?: "-",
                            color = if (notScanned) NotScannedGray else null
                        )
                        if (!notScanned && hasEcart) {
                            QuantityPill(
                                label = "Écart",
                                value = article.ecart?.let { formatQuantity(it, showSign = true) } ?: "-",
                                color = ecartColor ?: statusColor
                            )
                        }
                    }
                }
            }
            // Flotte au-dessus du contenu sans lui retirer de largeur : le titre et la
            // rangée Système / Inventaire / Écart gardent toujours la même largeur,
            // avec ou sans stylo affiché.
            if (!notScanned && !locked) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Modifier la quantité"
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleInfoRow(icon: ImageVector, text: String) {
    ArticleInfoRow(text = text) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ArticleInfoRow(painter: Painter, text: String) {
    ArticleInfoRow(text = text) {
        Icon(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ArticleInfoRow(text: String, icon: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icon()
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun QuantityPill(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color? = null
) {
    val pillColor = color ?: MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(pillColor.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "$label $value",
            style = MaterialTheme.typography.labelSmall,
            color = pillColor,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}
