package ma.bam.inventaire.ui.home

import androidx.annotation.StringRes
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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import ma.bam.inventaire.R
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.InventoryStatus
import ma.bam.inventaire.data.repository.SessionCounters
import ma.bam.inventaire.ui.components.ConfirmDialog
import ma.bam.inventaire.ui.components.ConfirmTone
import ma.bam.inventaire.ui.components.InventoLockup
import ma.bam.inventaire.ui.components.boldNavy
import ma.bam.inventaire.ui.components.rememberTapHaptic
import ma.bam.inventaire.ui.importinventory.ImportSheet
import ma.bam.inventaire.ui.inventorylist.InventoryListViewModel
import ma.bam.inventaire.ui.inventorylist.SessionListItem
import ma.bam.inventaire.ui.inventorylist.SwipeableSessionCard
import ma.bam.inventaire.ui.theme.BamNavyFill
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.LightDarkPreviews
import ma.bam.inventaire.ui.theme.OnBrand
import ma.bam.inventaire.ui.theme.PreviewSurface
import ma.bam.inventaire.ui.theme.TextMuted
import ma.bam.inventaire.ui.theme.TextPrimary

/** Actions sur un inventaire demandées depuis sa carte, chacune confirmée par une popup. */
private enum class SessionAction(
    val icon: ImageVector,
    val tone: ConfirmTone,
    @StringRes val titleRes: Int,
    @StringRes val messageRes: Int,
    @StringRes val confirmRes: Int
) {
    DELETE(Icons.Outlined.Delete, ConfirmTone.DANGER, R.string.home_delete_title, R.string.home_delete_message, R.string.action_delete),
    CLOSE(Icons.Outlined.Lock, ConfirmTone.NAVY, R.string.home_close_title, R.string.home_close_message, R.string.action_close),
    REOPEN(
        Icons.Outlined.LockOpen,
        ConfirmTone.NAVY,
        R.string.home_reopen_title,
        R.string.home_reopen_message,
        R.string.home_reopen_confirm
    )
}

/** Actions de l'accueil, remontées par la partie sans état [HomeContent]. */
private class HomeActions(
    val onImport: () -> Unit,
    val onToggleFilter: (HomeFilter) -> Unit,
    val onClearFilter: () -> Unit,
    val onOpen: (SessionListItem) -> Unit,
    val onRequest: (SessionAction, SessionListItem) -> Unit
)

@Composable
fun HomeScreen(onOpenSession: (sessionId: String) -> Unit, viewModel: InventoryListViewModel = hiltViewModel()) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    var showImport by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<Pair<SessionAction, SessionListItem>?>(null) }
    // Filtre de la liste choisi en touchant une tuile ; toucher la même tuile l'annule.
    var filter by remember { mutableStateOf<HomeFilter?>(null) }
    val tapHaptic = rememberTapHaptic()

    HomeContent(
        sessions = sessions,
        filter = filter,
        actions = HomeActions(
            onImport = {
                tapHaptic()
                showImport = true
            },
            onToggleFilter = {
                tapHaptic()
                filter = if (filter == it) null else it
            },
            onClearFilter = { filter = null },
            onOpen = {
                tapHaptic()
                onOpenSession(it.session.id)
            },
            onRequest = { action, item -> pending = action to item }
        )
    )

    if (showImport) {
        ImportSheet(
            onDismiss = { showImport = false },
            onOpenSession = onOpenSession
        )
    }
    pending?.let { (action, item) ->
        SessionConfirmDialog(
            action = action,
            item = item,
            onConfirm = {
                val id = item.session.id
                when (action) {
                    SessionAction.DELETE -> viewModel.deleteSession(id)
                    SessionAction.CLOSE -> viewModel.closeSession(id)
                    SessionAction.REOPEN -> viewModel.reopenSession(id)
                }
                pending = null
            },
            onDismiss = { pending = null }
        )
    }
}

/** Confirmation d'une [SessionAction] : « Inventaire N° … » en gras dans le message. */
@Composable
private fun SessionConfirmDialog(action: SessionAction, item: SessionListItem, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ConfirmDialog(
        icon = action.icon,
        tone = action.tone,
        title = stringResource(action.titleRes),
        message = boldNavy(
            stringResource(R.string.inventory_prefix),
            item.session.numero,
            stringResource(action.messageRes)
        ),
        confirmLabel = stringResource(action.confirmRes),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/** Accueil sans état : logo, tuiles de synthèse (filtres), titre de la liste et inventaires. */
@Composable
private fun HomeContent(sessions: List<SessionListItem>, filter: HomeFilter?, actions: HomeActions) {
    val visibleSessions = filter?.let { f -> sessions.filter { f.matches(it) } } ?: sessions

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = actions.onImport,
                shape = RoundedCornerShape(16.dp),
                containerColor = BamNavyFill,
                contentColor = OnBrand,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.import_title), fontWeight = FontWeight.Bold) }
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
            SummaryTiles(sessions = sessions, filter = filter, onToggleFilter = actions.onToggleFilter)
            ListHeader(filter = filter, onClearFilter = actions.onClearFilter)
            when {
                visibleSessions.isEmpty() && filter != null -> Text(
                    text = stringResource(R.string.home_no_inventory),
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                )
                visibleSessions.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.home_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> SessionList(sessions = visibleSessions, actions = actions)
            }
        }
    }
}

/** « Mes inventaires » (ou filtré) et le lien « Tout afficher » quand un filtre est actif. */
@Composable
private fun ListHeader(filter: HomeFilter?, onClearFilter: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = filter?.let { stringResource(R.string.home_my_inventories_filtered, stringResource(it.labelRes)) }
                ?: stringResource(R.string.home_my_inventories),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        if (filter != null) {
            Text(
                text = stringResource(R.string.home_show_all),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = BamPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onClearFilter)
            )
        }
    }
}

@Composable
private fun SessionList(sessions: List<SessionListItem>, actions: HomeActions) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // Marge basse pour que la dernière carte ne reste pas cachée sous le bouton flottant.
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sessions, key = { it.session.id }) { item ->
            SwipeableSessionCard(
                item = item,
                dateLabel = dateFormat.format(Date(item.session.dateCreation)),
                onClick = { actions.onOpen(item) },
                onRequestDelete = { actions.onRequest(SessionAction.DELETE, item) },
                onRequestClose = { actions.onRequest(SessionAction.CLOSE, item) },
                onLockClick = { actions.onRequest(SessionAction.REOPEN, item) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

private fun previewSession(id: Int, scanned: Int, ecarts: Int, verrouille: Boolean = false) = SessionListItem(
    session = InventorySessionEntity(
        id = "preview-$id",
        numero = "INV-2026-00$id",
        dateCreation = 0L,
        dateFinalisation = null,
        statut = InventoryStatus.EN_COURS,
        nomFichierSource = "stock.xlsx",
        verrouille = verrouille
    ),
    counters = SessionCounters(
        total = PREVIEW_TOTAL,
        scanned = scanned,
        ecarts = ecarts,
        pendingEcarts = ecarts,
        negativeStock = 0,
        emptyStock = 0
    )
)

private const val PREVIEW_TOTAL = 120
private val NoHomeActions = HomeActions({}, {}, {}, {}, { _, _ -> })

@LightDarkPreviews
@Composable
private fun HomeContentPreview() {
    PreviewSurface {
        HomeContent(
            sessions = listOf(
                previewSession(id = 1, scanned = 48, ecarts = 3),
                previewSession(id = 2, scanned = 0, ecarts = 0)
            ),
            filter = null,
            actions = NoHomeActions
        )
    }
}

@LightDarkPreviews
@Composable
private fun HomeContentFilteredPreview() {
    PreviewSurface {
        HomeContent(
            sessions = listOf(previewSession(id = 3, scanned = PREVIEW_TOTAL, ecarts = 0, verrouille = true)),
            filter = HomeFilter.ECARTS,
            actions = NoHomeActions
        )
    }
}
