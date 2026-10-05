package ma.bam.inventaire.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ma.bam.inventaire.BuildConfig
import ma.bam.inventaire.R
import ma.bam.inventaire.ui.components.SingleLineAutoSizeText
import ma.bam.inventaire.ui.inventorylist.InventoryListViewModel
import ma.bam.inventaire.ui.inventorylist.SessionListItem
import ma.bam.inventaire.ui.inventorylist.SwipeableSessionCard
import ma.bam.inventaire.ui.theme.BamGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartInventory: () -> Unit,
    onOpenSession: (sessionId: String) -> Unit,
    viewModel: InventoryListViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE) }
    var pendingDelete by remember { mutableStateOf<SessionListItem?>(null) }
    var pendingClose by remember { mutableStateOf<SessionListItem?>(null) }
    var pendingReopen by remember { mutableStateOf<SessionListItem?>(null) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_invento_transparent),
                    contentDescription = "Invento",
                    modifier = Modifier.width(120.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onStartInventory,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BamGreen)
            ) {
                Icon(
                    imageVector = Icons.Filled.AddCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Nouvel inventaire",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Mes inventaires",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            if (sessions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Aucun inventaire pour le moment.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sessions, key = { it.session.id }) { item ->
                        SwipeableSessionCard(
                            item = item,
                            dateLabel = dateFormat.format(Date(item.session.dateCreation)),
                            onClick = { onOpenSession(item.session.id) },
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

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Filled.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { SingleLineAutoSizeText("Supprimer cet inventaire ?", style = MaterialTheme.typography.titleMedium) },
            text = {
                Text(
                    "L'inventaire ${item.session.numero} sera définitivement supprimé, avec tous ses articles. Cette action est irréversible."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSession(item.session.id)
                    pendingDelete = null
                }) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Annuler") }
            }
        )
    }

    pendingClose?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingClose = null },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = BamGreen
                )
            },
            title = { SingleLineAutoSizeText("Clôturer cet inventaire ?", style = MaterialTheme.typography.titleMedium) },
            text = {
                Text(
                    "Les quantités de l'inventaire ${item.session.numero} seront verrouillées : plus aucune modification ne sera possible.",
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.closeSession(item.session.id)
                    pendingClose = null
                }) {
                    Text("Clôturer", color = BamGreen, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingClose = null }) { Text("Annuler") }
            }
        )
    }

    pendingReopen?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingReopen = null },
            icon = {
                Icon(
                    imageVector = Icons.Filled.LockOpen,
                    contentDescription = null,
                    tint = BamGreen
                )
            },
            title = { SingleLineAutoSizeText("Reprendre les modifications ?", style = MaterialTheme.typography.titleMedium) },
            text = {
                Text(
                    "L'inventaire ${item.session.numero} sera déverrouillé et ses articles pourront à nouveau être modifiés."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.reopenSession(item.session.id)
                    pendingReopen = null
                }) {
                    Text("Reprendre", color = BamGreen, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingReopen = null }) { Text("Annuler") }
            }
        )
    }
}
