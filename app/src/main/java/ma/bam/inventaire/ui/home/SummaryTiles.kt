package ma.bam.inventaire.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.ui.inventorylist.SessionListItem
import ma.bam.inventaire.ui.theme.BamNavyFill
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.TabularNums
import ma.bam.inventaire.ui.theme.TextSecondary

/** Tuiles de synthèse de l'accueil, une par [HomeFilter] ; toucher une tuile filtre la liste. */
@Composable
internal fun SummaryTiles(sessions: List<SessionListItem>, filter: HomeFilter?, onToggleFilter: (HomeFilter) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        HomeFilter.entries.forEach { tile ->
            SummaryTile(
                label = stringResource(tile.labelRes),
                value = sessions.count { tile.matches(it) },
                color = tile.color,
                selected = filter == tile,
                onClick = { onToggleFilter(tile) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryTile(label: String, value: Int, color: Color, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
