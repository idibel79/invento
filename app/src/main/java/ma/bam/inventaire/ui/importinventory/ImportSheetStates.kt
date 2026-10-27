package ma.bam.inventaire.ui.importinventory

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.R
import ma.bam.inventaire.data.importfile.ImportFileInfo
import ma.bam.inventaire.ui.components.pluralLabel
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.BamPrimaryContainer
import ma.bam.inventaire.ui.theme.StatusEcart
import ma.bam.inventaire.ui.theme.StatusEcartBg
import ma.bam.inventaire.ui.theme.StatusNegative
import ma.bam.inventaire.ui.theme.StatusNegativeBg
import ma.bam.inventaire.ui.theme.StatusSuccess
import ma.bam.inventaire.ui.theme.StatusSuccessBg
import ma.bam.inventaire.ui.theme.TextSecondary
import ma.bam.inventaire.ui.theme.TrackColor

/** État initial : explication, zone de choix du fichier, colonnes attendues (repliables), Annuler. */
@Composable
internal fun IdleContent(onPick: () -> Unit, onCancel: () -> Unit) {
    var columnsExpanded by remember { mutableStateOf(false) }

    Text(
        text = stringResource(R.string.import_intro),
        fontSize = 12.5.sp,
        color = TextSecondary
    )
    FilePickZone(onPick = onPick)
    ExpectedColumnsToggle(expanded = columnsExpanded, onToggle = { columnsExpanded = !columnsExpanded })
    if (columnsExpanded) ColumnChips()
    CancelButton(modifier = Modifier.fillMaxWidth(), onClick = onCancel)
}

/** Zone en pointillés « Choisir un fichier Excel ». */
@Composable
private fun FilePickZone(onPick: () -> Unit) {
    val dashColor = BamPrimary.copy(alpha = 0.45f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BamPrimaryContainer)
            .drawBehind {
                val stroke = 2.dp.toPx()
                drawRoundRect(
                    color = dashColor,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(size.width - stroke, size.height - stroke),
                    cornerRadius = CornerRadius(16.dp.toPx() - stroke / 2),
                    style = Stroke(
                        width = stroke,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
                    )
                )
            }
            .clickable(onClick = onPick)
            .padding(horizontal = 12.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(imageVector = ExcelFileIcon, contentDescription = null, modifier = Modifier.size(44.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.import_pick),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = BamNavy
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.import_format), fontSize = 11.5.sp, color = TextSecondary)
    }
}

@Composable
private fun ExpectedColumnsToggle(expanded: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (expanded) {
                Icons.Filled.KeyboardArrowDown
            } else {
                Icons.AutoMirrored.Filled.KeyboardArrowRight
            },
            contentDescription = null,
            tint = BamPrimary,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.import_expected_columns),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = BamPrimary
        )
    }
}

/** Import en cours : fichier choisi (sans croix) et barre de progression indéterminée. */
@Composable
internal fun LoadingContent(file: ImportFileInfo?) {
    FileRow(file = file, onClear = null)
    Column(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = BamPrimary,
            trackColor = TrackColor,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp
        )
        Text(
            text = stringResource(R.string.import_loading),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 12.5.sp,
            color = TextSecondary
        )
    }
}

/** Import réussi : nombre d'articles, lignes ignorées éventuelles et « Ouvrir l'inventaire ». */
@Composable
internal fun SuccessContent(success: ImportUiState.Success, file: ImportFileInfo?, onClearFile: () -> Unit, onOpenSession: () -> Unit) {
    FileRow(file = file, onClear = onClearFile)
    StatusHeader(
        circleColor = StatusSuccessBg,
        icon = Icons.Filled.Check,
        iconSize = 26.dp,
        iconTint = StatusSuccess,
        title = stringResource(R.string.import_success)
    ) {
        val importedLabel = pluralLabel(R.plurals.import_articles_imported, success.nbArticles)
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = BamNavy)) {
                    append(success.nbArticles.toString())
                }
                append(" ")
                append(importedLabel)
            },
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
    if (success.erreurs.isNotEmpty()) {
        IgnoredLinesCard(erreurs = success.erreurs)
    }
    PrimaryButton(
        text = stringResource(R.string.import_open),
        modifier = Modifier.fillMaxWidth(),
        onClick = onOpenSession
    )
}

/** Encadré « N lignes ignorées » avec le détail repliable. */
@Composable
private fun IgnoredLinesCard(erreurs: List<String>) {
    var errorsExpanded by remember { mutableStateOf(false) }
    val warn = StatusNegative
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(StatusNegativeBg)
            .padding(horizontal = 11.dp, vertical = 9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = pluralLabel(R.plurals.import_ignored_lines, erreurs.size),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = warn,
                modifier = Modifier.weight(1f)
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { errorsExpanded = !errorsExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                val toggleLabel = if (errorsExpanded) R.string.import_hide_details else R.string.import_show_details
                Text(
                    text = stringResource(toggleLabel),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = warn
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = if (errorsExpanded) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                    contentDescription = null,
                    tint = warn,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        if (errorsExpanded) {
            erreurs.forEach { erreur ->
                Spacer(modifier = Modifier.height(5.dp))
                Text(text = "• $erreur", fontSize = 11.5.sp, lineHeight = 16.sp, color = warn)
            }
        }
    }
}

/** Échec : message, colonnes attendues, Annuler / Choisir un autre fichier. */
@Composable
internal fun ErrorContent(message: String, file: ImportFileInfo?, onClearFile: () -> Unit, onCancel: () -> Unit, onPick: () -> Unit) {
    FileRow(file = file, onClear = onClearFile)
    StatusHeader(
        circleColor = StatusEcartBg,
        icon = Icons.Filled.Close,
        iconSize = 24.dp,
        iconTint = StatusEcart,
        title = stringResource(R.string.import_failed)
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
            fontSize = 12.5.sp,
            color = StatusEcart
        )
    }
    ColumnChips()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CancelButton(modifier = Modifier.weight(1f), onClick = onCancel)
        PrimaryButton(
            text = stringResource(R.string.import_pick_other),
            modifier = Modifier.weight(1f),
            onClick = onPick
        )
    }
}
