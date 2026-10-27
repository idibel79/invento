package ma.bam.inventaire.ui.importinventory

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.R
import ma.bam.inventaire.data.importfile.ImportFileInfo
import ma.bam.inventaire.ui.components.PrimaryActionButton
import ma.bam.inventaire.ui.components.SecondaryActionButton
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BorderColor
import ma.bam.inventaire.ui.theme.SoftBand
import ma.bam.inventaire.ui.theme.TextPrimary
import ma.bam.inventaire.ui.theme.TextSecondary

private const val BYTES_PER_KO = 1024L

@Composable
internal fun ImportSheetHandle() {
    Box(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 2.dp)
            .size(width = 36.dp, height = 4.dp)
            .clip(CircleShape)
            .background(BorderColor)
    )
}

/** Colonnes attendues dans le fichier Excel, dans l'ordre de l'export. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ColumnChips() {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        stringArrayResource(R.array.import_expected_columns).forEach { column ->
            Text(
                text = column,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = BamNavy,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SoftBand)
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            )
        }
    }
}

/** Ligne du fichier choisi ; la croix (si [onClear]) ramène au choix du fichier. */
@Composable
internal fun FileRow(file: ImportFileInfo?, onClear: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SoftBand)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(imageVector = ExcelFileIcon, contentDescription = null, modifier = Modifier.size(30.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file?.name ?: "",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            file?.sizeBytes?.let { bytes ->
                Text(
                    text = stringResource(R.string.import_size_ko, formatKo(bytes)),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
        if (onClear != null) {
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.import_remove_file),
                tint = TextSecondary,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onClear)
                    .size(16.dp)
            )
        }
    }
}

/** Taille en Ko arrondie, au moins 1. */
internal fun formatKo(bytes: Long): String = maxOf(1L, (bytes + BYTES_PER_KO / 2) / BYTES_PER_KO).toString()

/** Pastille ronde avec icône, titre et sous-texte, centrés (états réussite et erreur). */
@Composable
internal fun StatusHeader(
    circleColor: Color,
    icon: ImageVector,
    iconSize: Dp,
    iconTint: Color,
    title: String,
    subtitle: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(circleColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(iconSize))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        subtitle()
    }
}

@Composable
internal fun CancelButton(modifier: Modifier, onClick: () -> Unit) {
    SecondaryActionButton(stringResource(R.string.common_cancel), onClick, modifier)
}

@Composable
internal fun PrimaryButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    PrimaryActionButton(text, onClick, modifier)
}
