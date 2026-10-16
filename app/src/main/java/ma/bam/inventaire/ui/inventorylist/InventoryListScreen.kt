package ma.bam.inventaire.ui.inventorylist

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import ma.bam.inventaire.ui.theme.AppBackground
import ma.bam.inventaire.ui.theme.BamCyan
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.LightInventoPalette
import ma.bam.inventaire.ui.theme.SelectedCardBg
import ma.bam.inventaire.ui.theme.TextPrimary
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.onSizeChanged
import kotlin.math.abs
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.BamPrimaryContainer
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
    // Il faut swiper jusqu'à la moitié de la largeur, dans un sens ou l'autre, pour déclencher
    // une action. Le seuil positionnel seul ne suffit pas : un swipe rapide et court déclenche
    // quand même via la vitesse du geste. On vérifie donc la distance réellement parcourue.
    val cardWidth = remember { floatArrayOf(0f) }
    val stateRef = remember { arrayOfNulls<SwipeToDismissBoxState>(1) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            val offset = stateRef[0]?.let { runCatching { abs(it.requireOffset()) }.getOrNull() } ?: 0f
            val farEnough = cardWidth[0] > 0f && offset >= cardWidth[0] * 0.5f
            if (farEnough) {
                when (value) {
                    SwipeToDismissBoxValue.EndToStart -> onRequestDelete()
                    SwipeToDismissBoxValue.StartToEnd -> onRequestClose()
                    SwipeToDismissBoxValue.Settled -> Unit
                }
            }
            false
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.5f }
    )
    stateRef[0] = dismissState
    val progress = if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) {
        dismissState.progress.coerceIn(0f, 1f)
    } else {
        0f
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.onSizeChanged { cardWidth[0] = it.width.toFloat() },
        enableDismissFromStartToEnd = !item.session.verrouille,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> SwipeActionBackground(
                    alignment = Alignment.CenterEnd,
                    color = LightInventoPalette.ecart,
                    progress = progress,
                    icon = Icons.Filled.Delete,
                    label = "Supprimer"
                )
                SwipeToDismissBoxValue.StartToEnd -> SwipeActionBackground(
                    alignment = Alignment.CenterStart,
                    color = LightInventoPalette.success,
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
            .clip(RoundedCornerShape(18.dp))
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


/** Couleurs d'une carte d'inventaire selon son état (En cours / Finalisé / En attente). */
private data class SessionCardColors(
    val background: Color,
    val titleText: Color,
    val text: Color,
    val dateText: Color,
    val accent: Color,
    val ringProgress: Color,
    val ringPercent: Color,
    val track: Color,
    val badgeBackground: Color,
    val badgeText: Color,
    val emptyChipBackground: Color,
    val emptyChipText: Color
)

@Composable
fun InventorySessionCard(
    item: SessionListItem,
    dateLabel: String,
    onClick: () -> Unit,
    onLockClick: () -> Unit = {}
) {
    val closed = item.session.verrouille
    val allScanned = item.counters.total > 0 && item.counters.scanned >= item.counters.total
    val notStarted = item.counters.scanned == 0
    val colors = when {
        // Finalisé : fond vert clair #E3F5EA, textes en bleu Invento, anneau vert.
        allScanned -> SessionCardColors(
            background = StatusSuccessBg,
            titleText = BamNavy,
            text = BamNavy,
            dateText = BamNavy,
            accent = BamNavy,
            ringProgress = StatusSuccess,
            ringPercent = StatusSuccess,
            track = CardSurface,
            badgeBackground = StatusSuccess,
            badgeText = CardSurface,
            emptyChipBackground = CardSurface.copy(alpha = 0.6f),
            emptyChipText = TextMuted
        )
        notStarted -> SessionCardColors(
            background = CardSurface,
            titleText = BamNavy,
            text = BamNavy,
            dateText = BamNavy,
            accent = TextMuted,
            ringProgress = TextMuted,
            ringPercent = TextMuted,
            track = StatusPendingBg,
            badgeBackground = StatusPending,
            badgeText = StatusPendingBg,
            emptyChipBackground = AppBackground,
            emptyChipText = TextMuted
        )
        // En cours : fond bleu clair #CEDEFF, textes en bleu Invento, anneau bleu.
        else -> SessionCardColors(
            background = SelectedCardBg,
            titleText = BamNavy,
            text = BamNavy,
            dateText = BamNavy,
            accent = BamNavy,
            ringProgress = BamPrimary,
            ringPercent = BamPrimary,
            track = CardSurface,
            badgeBackground = BamPrimary,
            badgeText = CardSurface,
            emptyChipBackground = CardSurface.copy(alpha = 0.6f),
            emptyChipText = TextMuted
        )
    }
    val progress = if (item.counters.total > 0) {
        item.counters.scanned.toFloat() / item.counters.total.toFloat()
    } else {
        0f
    }
    val percent = (progress * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(
                    progress = progress,
                    percent = percent,
                    color = colors.ringProgress,
                    trackColor = colors.track,
                    percentColor = colors.ringPercent
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            item.session.numero,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.titleText,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            style = LocalTextStyle.current.copy(fontFeatureSettings = TabularNums),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                allScanned -> "Finalisé"
                                notStarted -> "En attente"
                                else -> "En cours"
                            },
                            fontSize = 11.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.badgeText,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.badgeBackground)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                dateLabel,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.dateText,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                style = LocalTextStyle.current.copy(fontFeatureSettings = TabularNums),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(
                                "${item.counters.scanned} / ${item.counters.total} articles",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.text,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                style = LocalTextStyle.current.copy(fontFeatureSettings = TabularNums),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        // Cadenas : centré entre le statut et les cases du bas (le décalage
                        // compense l'espace de 10dp sous cette ligne), aligné à droite.
                        Box(
                            modifier = Modifier
                                .offset(y = 5.dp)
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .let { if (closed) it.clickable(onClick = onLockClick) else it },
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = if (closed) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                contentDescription = if (closed) "Inventaire clôturé, toucher pour déclôturer" else "Inventaire non clôturé",
                                tint = colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CounterChip(
                    count = item.counters.ecarts,
                    singular = "écart",
                    color = StatusEcart,
                    background = StatusEcartBg,
                    emptyBackground = colors.emptyChipBackground,
                    emptyText = colors.emptyChipText,
                    modifier = Modifier.weight(1f)
                )
                CounterChip(
                    count = item.counters.negativeStock,
                    singular = "négatif",
                    color = StatusNegative,
                    background = StatusNegativeBg,
                    emptyBackground = colors.emptyChipBackground,
                    emptyText = colors.emptyChipText,
                    modifier = Modifier.weight(1f)
                )
                CounterChip(
                    count = item.counters.emptyStock,
                    singular = "vide",
                    color = StatusPending,
                    background = StatusPendingBg,
                    emptyBackground = colors.emptyChipBackground,
                    emptyText = colors.emptyChipText,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Anneau de progression de 54dp (trait 6dp) avec le pourcentage au centre. */
@Composable
private fun ProgressRing(progress: Float, percent: Int, color: Color, trackColor: Color, percentColor: Color) {
    Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 6.dp.toPx()
            val inset = strokeWidth / 2f + 1.dp.toPx()
            val arcSize = Size(size.width - 2 * inset, size.height - 2 * inset)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            if (progress > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        Text(
            "$percent%",
            fontSize = if (percent >= 100) 11.sp else 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = percentColor,
            maxLines = 1,
            softWrap = false,
            style = LocalTextStyle.current.copy(fontFeatureSettings = TabularNums)
        )
    }
}

/** Case de compteur (écarts, négatifs, vides), toujours affichée ; grisée à 0. */
@Composable
private fun CounterChip(
    count: Int,
    singular: String,
    color: Color,
    background: Color,
    emptyBackground: Color,
    emptyText: Color,
    modifier: Modifier = Modifier
) {
    val empty = count == 0
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (empty) emptyBackground else background)
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            count.toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (empty) emptyText else color,
            maxLines = 1,
            softWrap = false,
            style = LocalTextStyle.current.copy(fontFeatureSettings = TabularNums)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            if (count == 1) singular else "${singular}s",
            fontSize = 11.sp,
            color = if (empty) emptyText else color,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}
