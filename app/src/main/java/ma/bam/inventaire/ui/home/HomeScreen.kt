package ma.bam.inventaire.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ma.bam.inventaire.ui.components.ConfirmDialog
import ma.bam.inventaire.ui.components.ConfirmTone
import ma.bam.inventaire.ui.components.InventoLockup
import ma.bam.inventaire.ui.components.boldNavy
import ma.bam.inventaire.ui.components.rememberTapHaptic
import ma.bam.inventaire.ui.importinventory.ImportSheet
import ma.bam.inventaire.ui.inventorylist.InventoryListViewModel
import ma.bam.inventaire.ui.inventorylist.SessionListItem
import ma.bam.inventaire.ui.inventorylist.SwipeableSessionCard
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamNavyFill
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.StatusEcart
import ma.bam.inventaire.ui.theme.StatusSuccess
import ma.bam.inventaire.ui.theme.TabularNums
import ma.bam.inventaire.ui.theme.TextMuted
import ma.bam.inventaire.ui.theme.TextPrimary
import ma.bam.inventaire.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenSession: (sessionId: String) -> Unit,
    viewModel: InventoryListViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    var showImport by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE) }
    var pendingDelete by remember { mutableStateOf<SessionListItem?>(null) }
    var pendingClose by remember { mutableStateOf<SessionListItem?>(null) }
    var pendingReopen by remember { mutableStateOf<SessionListItem?>(null) }

    val ongoingCount = sessions.count { it.isOngoing() }
    val pendingCount = sessions.count { it.isPending() }
    val ecartsCount = sessions.count { it.hasEcarts() }
    val finalizedCount = sessions.count { it.isFinalized() }
    // Filtre de la liste choisi en touchant une tuile ; toucher la même tuile l'annule.
    var filter by remember { mutableStateOf<HomeFilter?>(null) }
    val visibleSessions = when (filter) {
        HomeFilter.ONGOING -> sessions.filter { it.isOngoing() }
        HomeFilter.PENDING -> sessions.filter { it.isPending() }
        HomeFilter.ECARTS -> sessions.filter { it.hasEcarts() }
        HomeFilter.FINALIZED -> sessions.filter { it.isFinalized() }
        null -> sessions
    }
    val tapHaptic = rememberTapHaptic()
    val toggleFilter: (HomeFilter) -> Unit = {
        tapHaptic()
        filter = if (filter == it) null else it
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    tapHaptic()
                    showImport = true
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nouvel inventaire", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            InventoLockup(
                markSize = 40.dp,
                fontSize = 26.sp,
                gap = 10.dp,
                modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 18.dp, bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HomeFilter.entries.forEach { tile ->
                    SummaryTile(
                        label = tile.label,
                        value = when (tile) {
                            HomeFilter.ONGOING -> ongoingCount
                            HomeFilter.PENDING -> pendingCount
                            HomeFilter.ECARTS -> ecartsCount
                            HomeFilter.FINALIZED -> finalizedCount
                        },
                        color = tile.color,
                        selected = filter == tile,
                        onClick = { toggleFilter(tile) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = filter?.let { "Mes inventaires · ${it.label}" } ?: "Mes inventaires",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                if (filter != null) {
                    Text(
                        text = "Tout afficher",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BamPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { filter = null }
                    )
                }
            }

            if (visibleSessions.isEmpty() && filter != null) {
                Text(
                    text = "Aucun inventaire",
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                )
            } else if (visibleSessions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Aucun inventaire pour le moment.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    // Marge basse pour que la dernière carte ne reste pas cachée sous le bouton flottant.
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(visibleSessions, key = { it.session.id }) { item ->
                        SwipeableSessionCard(
                            item = item,
                            dateLabel = dateFormat.format(Date(item.session.dateCreation)),
                            onClick = {
                                tapHaptic()
                                onOpenSession(item.session.id)
                            },
                            onRequestDelete = { pendingDelete = item },
                            onRequestClose = { pendingClose = item },
                            onLockClick = { pendingReopen = item },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    if (showImport) {
        ImportSheet(
            onDismiss = { showImport = false },
            onOpenSession = onOpenSession
        )
    }

    pendingDelete?.let { item ->
        ConfirmDialog(
            icon = Icons.Outlined.Delete,
            tone = ConfirmTone.DANGER,
            title = "Supprimer cet inventaire ?",
            message = boldNavy(
                "L'inventaire ", item.session.numero,
                " sera définitivement supprimé, avec tous ses articles. Cette action est irréversible."
            ),
            confirmLabel = "Supprimer",
            onConfirm = {
                viewModel.deleteSession(item.session.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }

    pendingClose?.let { item ->
        ConfirmDialog(
            icon = Icons.Outlined.Lock,
            tone = ConfirmTone.NAVY,
            title = "Clôturer cet inventaire ?",
            message = boldNavy(
                "L'inventaire ", item.session.numero,
                " sera verrouillé : plus aucune modification ne sera possible."
            ),
            confirmLabel = "Clôturer",
            onConfirm = {
                viewModel.closeSession(item.session.id)
                pendingClose = null
            },
            onDismiss = { pendingClose = null }
        )
    }

    pendingReopen?.let { item ->
        ConfirmDialog(
            icon = Icons.Outlined.LockOpen,
            tone = ConfirmTone.NAVY,
            title = "Reprendre les modifications ?",
            message = boldNavy(
                "L'inventaire ", item.session.numero,
                " sera déverrouillé et ses articles pourront à nouveau être modifiés."
            ),
            confirmLabel = "Reprendre",
            onConfirm = {
                viewModel.reopenSession(item.session.id)
                pendingReopen = null
            },
            onDismiss = { pendingReopen = null }
        )
    }
}

/** Filtres de la liste, un par tuile, dans l'ordre d'affichage. */
private enum class HomeFilter(val label: String) {
    ONGOING("En cours"),
    PENDING("En attente"),
    ECARTS("Écarts"),
    FINALIZED("Finalisés");

    val color: Color
        @Composable get() = when (this) {
            ONGOING -> BamPrimary
            PENDING -> TextSecondary
            ECARTS -> StatusEcart
            FINALIZED -> StatusSuccess
        }
}

/** Finalisé = tous les articles comptés (carte verte) ou clôturé (cadenas fermé). */
private fun SessionListItem.isFinalized(): Boolean =
    session.verrouille || (counters.total > 0 && counters.scanned >= counters.total)

/** Non finalisé, au moins un article compté. */
private fun SessionListItem.isOngoing(): Boolean = !isFinalized() && counters.scanned > 0

/** Non finalisé, aucun article compté. */
private fun SessionListItem.isPending(): Boolean = !isFinalized() && counters.scanned == 0

/** Au moins un écart (validé ou non), même définition que le filtre « Écarts » de la page produits. */
private fun SessionListItem.hasEcarts(): Boolean = counters.ecarts > 0

@Composable
private fun SummaryTile(
    label: String,
    value: Int,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) BamNavyFill else CardSurface,
        shadowElevation = if (selected) 0.dp else 1.dp,
        border = BorderStroke(1.5.dp, if (selected) BamNavyFill else Color.Transparent),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 9.dp)
        ) {
            Text(
                text = value.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (selected) Color.White else color,
                style = LocalTextStyle.current.copy(fontFeatureSettings = TabularNums),
                maxLines = 1
            )
            Text(
                text = label,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else TextSecondary,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
