package ma.bam.inventaire.ui.inventorydetail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Search
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
import ma.bam.inventaire.domain.EcartCalculator
import ma.bam.inventaire.ui.quantityentry.QuantityEntryDialog
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
    var editingArticle by remember { mutableStateOf<StockArticleEntity?>(null) }
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
                    label = { Text("Code-barres") },
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
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                    label = { Text("Scannés") }
                )
                FilterChip(
                    selected = filter == ArticleFilter.ECARTS,
                    onClick = { viewModel.setFilter(ArticleFilter.ECARTS) },
                    label = { Text("Écarts") }
                )
                FilterChip(
                    selected = filter == ArticleFilter.NON_SCANNES,
                    onClick = { viewModel.setFilter(ArticleFilter.NON_SCANNES) },
                    label = { Text("Non scannés") }
                )
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
                        onEdit = { editingArticle = item }
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
}

@Composable
private fun ArticleRow(
    item: StockArticleEntity,
    locked: Boolean,
    onEdit: () -> Unit
) {
    val article = item
    val hasEcart = EcartCalculator.hasEcart(article.ecart)
    val notScanned = article.quantiteReelle == null
    val statusColor = when {
        notScanned -> NotScannedGray
        hasEcart -> MaterialTheme.colorScheme.error
        else -> SuccessGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (notScanned && !locked) it.clickable(onClick = onEdit) else it },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        article.designation.ifBlank { article.codeArticle },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = InProgressBlue,
                        maxLines = 1,
                        softWrap = false,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Clip,
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    ArticleInfoRow(
                        painter = painterResource(id = R.drawable.ic_pid_cross),
                        text = article.codeArticle
                    )
                    val codesBarres = listOf(article.codeBarre1, article.codeBarre2).filter { it.isNotBlank() }
                    if (codesBarres.isNotEmpty()) {
                        ArticleInfoRow(
                            painter = painterResource(id = R.drawable.ic_barcode_scan),
                            text = codesBarres.joinToString(" / ")
                        )
                    }
                    ArticleInfoRow(icon = Icons.Filled.Place, text = article.emplacement)
                }
                if (!notScanned && !locked) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Modifier la quantité"
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!notScanned) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (hasEcart) statusColor else statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasEcart) Icons.Filled.PriorityHigh else Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = if (hasEcart) Color.White else statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QuantityPill(label = "Stock", value = formatQuantity(article.quantiteTheorique))
                    QuantityPill(
                        label = "Réel",
                        value = article.quantiteReelle?.let { formatQuantity(it) } ?: "-",
                        color = if (notScanned) NotScannedGray else null
                    )
                    if (!notScanned) {
                        QuantityPill(
                            label = "Écart",
                            value = article.ecart?.let { formatQuantity(it, showSign = true) } ?: "-",
                            color = statusColor
                        )
                    }
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
