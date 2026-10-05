package ma.bam.inventaire.ui.inventorylist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ma.bam.inventaire.R
import ma.bam.inventaire.data.local.entity.InventoryStatus
import ma.bam.inventaire.ui.theme.EcartRed
import ma.bam.inventaire.ui.theme.InProgressBlue
import ma.bam.inventaire.ui.theme.NotScannedGray
import ma.bam.inventaire.ui.theme.SuccessGreen

/**
 * Carte de session d'inventaire avec swipe-to-delete (vers la gauche, rouge) et
 * swipe-to-close (vers la droite, vert). Réutilisée par [ma.bam.inventaire.ui.home.HomeScreen]
 * pour afficher la liste des inventaires.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableSessionCard(
    item: SessionListItem,
    dateLabel: String,
    onClick: () -> Unit,
    onRequestDelete: () -> Unit,
    onRequestClose: () -> Unit,
    onLockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> onRequestDelete()
                SwipeToDismissBoxValue.StartToEnd -> onRequestClose()
                SwipeToDismissBoxValue.Settled -> Unit
            }
            false
        },
        // Il faut swiper presque jusqu'au bout (90% de la largeur), dans un sens ou l'autre,
        // pour déclencher une action : pas juste un petit glissement.
        positionalThreshold = { totalDistance -> totalDistance * 0.9f }
    )
    val progress = if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) {
        dismissState.progress.coerceIn(0f, 1f)
    } else {
        0f
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = item.session.statut != InventoryStatus.FINALISE,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> SwipeActionBackground(
                    alignment = Alignment.CenterEnd,
                    color = EcartRed,
                    progress = progress,
                    icon = Icons.Filled.Delete,
                    label = "Supprimer"
                )
                SwipeToDismissBoxValue.StartToEnd -> SwipeActionBackground(
                    alignment = Alignment.CenterStart,
                    color = SuccessGreen,
                    progress = progress,
                    icon = Icons.Filled.Lock,
                    label = "Clôturer"
                )
                SwipeToDismissBoxValue.Settled -> Unit
            }
        }
    ) {
        InventorySessionCard(
            item = item,
            dateLabel = dateLabel,
            onClick = onClick,
            onLockClick = onLockClick
        )
    }
}

@Composable
private fun SwipeActionBackground(
    alignment: Alignment,
    color: Color,
    progress: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.5f + 0.5f * progress)),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .scale(0.85f + 0.15f * progress)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun InventorySessionCard(
    item: SessionListItem,
    dateLabel: String,
    onClick: () -> Unit,
    onLockClick: () -> Unit = {}
) {
    val closed = item.session.statut == InventoryStatus.FINALISE
    val allScanned = item.counters.total > 0 && item.counters.scanned >= item.counters.total
    val notStarted = item.counters.scanned == 0
    val statusColor = when {
        allScanned -> SuccessGreen
        notStarted -> NotScannedGray
        else -> InProgressBlue
    }
    val progress = if (item.counters.total > 0) {
        item.counters.scanned.toFloat() / item.counters.total.toFloat()
    } else {
        0f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            allScanned -> Icons.Filled.CheckCircle
                            notStarted -> Icons.Filled.Schedule
                            else -> Icons.Filled.Sync
                        },
                        contentDescription = null,
                        tint = statusColor
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.session.numero,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        dateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusChip(
                    label = when {
                        allScanned -> "Finalisé"
                        notStarted -> "En attente"
                        else -> "En cours"
                    },
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Butt,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_barcode_scan),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${item.counters.scanned}/${item.counters.total} articles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (item.counters.pendingEcarts > 0) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(EcartRed.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = EcartRed
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${item.counters.pendingEcarts} écart${if (item.counters.pendingEcarts > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EcartRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (closed) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onLockClick)
                            .background(SuccessGreen.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = SuccessGreen
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Inventaire clôturé",
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
