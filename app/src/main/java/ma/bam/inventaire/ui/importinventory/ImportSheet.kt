package ma.bam.inventaire.ui.importinventory

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import ma.bam.inventaire.ui.theme.*
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

private val SheetTitle: Color @Composable get() = TextPrimary
private val Navy: Color @Composable get() = BamNavy
private val Muted: Color @Composable get() = TextSecondary
private val Accent: Color @Composable get() = BamPrimary
private val SoftBg: Color @Composable get() = SoftBand
private val ButtonBorder: Color @Composable get() = ControlBorder

private const val XLSX_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

/** Colonnes attendues dans le fichier Excel, dans l'ordre de l'export. */
private val ExpectedColumns = listOf(
    "PID", "Produit", "Catégorie", "TVA", "PPV", "PPH", "Zone", "Stock",
    "Code barre 1", "Code barre 2", "Stock min", "Stock max", "Date de péremption"
)

/**
 * Feuille du bas « Nouvel inventaire », ouverte depuis l'accueil.
 * Pendant l'import, elle ne se ferme ni par glissement ni par appui à l'extérieur.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportSheet(
    onDismiss: () -> Unit,
    onOpenSession: (sessionId: String) -> Unit,
    viewModel: ImportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val file by viewModel.file.collectAsState()
    val isLoading by rememberUpdatedState(state is ImportUiState.Loading)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden || !isLoading }
    )
    val scope = rememberCoroutineScope()

    val close: () -> Unit = {
        viewModel.reset()
        onDismiss()
    }
    val hideThen: (() -> Unit) -> Unit = { action ->
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importFile(it) }
    }
    val pickFile = { filePicker.launch(arrayOf(XLSX_MIME)) }

    ModalBottomSheet(
        onDismissRequest = { if (!isLoading) close() },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        containerColor = CardSurface,
        dragHandle = { ImportSheetHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Nouvel inventaire",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = SheetTitle
            )

            when (val current = state) {
                is ImportUiState.Idle -> IdleContent(
                    onPick = pickFile,
                    onCancel = { hideThen(close) }
                )
                is ImportUiState.Loading -> {
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
                            color = Accent,
                            trackColor = TrackColor,
                            strokeCap = StrokeCap.Round,
                            gapSize = 0.dp
                        )
                        Text(
                            text = "Import en cours…",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 12.5.sp,
                            color = Muted
                        )
                    }
                }
                is ImportUiState.Success -> {
                    FileRow(file = file, onClear = viewModel::reset)
                    SuccessContent(current)
                    PrimaryButton(
                        text = "Ouvrir l’inventaire",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val sessionId = current.sessionId
                            hideThen {
                                close()
                                onOpenSession(sessionId)
                            }
                        }
                    )
                }
                is ImportUiState.Error -> {
                    FileRow(file = file, onClear = viewModel::reset)
                    StatusHeader(
                        circleColor = StatusEcartBg,
                        icon = Icons.Filled.Close,
                        iconSize = 24.dp,
                        iconTint = StatusEcart,
                        title = "Import impossible"
                    ) {
                        Text(
                            text = current.message,
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
                        CancelButton(modifier = Modifier.weight(1f), onClick = { hideThen(close) })
                        PrimaryButton(
                            text = "Choisir un autre fichier",
                            modifier = Modifier.weight(1.6f),
                            onClick = pickFile
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportSheetHandle() {
    Box(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 2.dp)
            .size(width = 36.dp, height = 4.dp)
            .clip(CircleShape)
            .background(BorderColor)
    )
}

@Composable
private fun IdleContent(onPick: () -> Unit, onCancel: () -> Unit) {
    var columnsExpanded by remember { mutableStateOf(false) }

    Text(
        text = "Importez la liste du stock système exportée depuis votre logiciel de gestion.",
        fontSize = 12.5.sp,
        color = Muted
    )

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
                    topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
                    size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
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
        Text(text = "Choisir un fichier Excel", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Format .xlsx uniquement", fontSize = 11.5.sp, color = Muted)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { columnsExpanded = !columnsExpanded },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (columnsExpanded) Icons.Filled.KeyboardArrowDown
            else Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Accent,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Colonnes attendues dans le fichier",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Accent
        )
    }
    if (columnsExpanded) ColumnChips()

    CancelButton(modifier = Modifier.fillMaxWidth(), onClick = onCancel)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnChips() {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        ExpectedColumns.forEach { column ->
            Text(
                text = column,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Navy,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SoftBg)
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            )
        }
    }
}

/** Ligne du fichier choisi ; la croix (si [onClear]) ramène au choix du fichier. */
@Composable
private fun FileRow(file: ImportFileInfo?, onClear: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SoftBg)
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
                color = SheetTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            file?.sizeBytes?.let { bytes ->
                Text(text = "${formatKo(bytes)} Ko", fontSize = 11.sp, color = Muted)
            }
        }
        if (onClear != null) {
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Retirer le fichier",
                tint = Muted,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onClear)
                    .size(16.dp)
            )
        }
    }
}

private fun formatKo(bytes: Long): String =
    maxOf(1L, (bytes + 512) / 1024).toString()

@Composable
private fun SuccessContent(success: ImportUiState.Success) {
    var errorsExpanded by remember { mutableStateOf(false) }

    StatusHeader(
        circleColor = StatusSuccessBg,
        icon = Icons.Filled.Check,
        iconSize = 26.dp,
        iconTint = StatusSuccess,
        title = "Import réussi"
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Navy)) {
                    append(success.nbArticles.toString())
                }
                append(if (success.nbArticles > 1) " articles importés" else " article importé")
            },
            fontSize = 13.sp,
            color = Muted
        )
    }

    if (success.erreurs.isNotEmpty()) {
        val warn = StatusNegative
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(StatusNegativeBg)
                .padding(horizontal = 11.dp, vertical = 9.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val n = success.erreurs.size
                Text(
                    text = if (n > 1) "$n lignes ignorées" else "$n ligne ignorée",
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
                    Text(
                        text = if (errorsExpanded) "Masquer" else "Voir le détail",
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
                success.erreurs.forEach { erreur ->
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(text = "• $erreur", fontSize = 11.5.sp, lineHeight = 16.sp, color = warn)
                }
            }
        }
    }
}

/** Pastille ronde avec icône, titre et sous-texte, centrés (états réussite et erreur). */
@Composable
private fun StatusHeader(
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
        Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SheetTitle)
        Spacer(modifier = Modifier.height(6.dp))
        subtitle()
    }
}

@Composable
private fun CancelButton(modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ButtonBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy)
    ) {
        Text("Annuler", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy, maxLines = 1)
    }
}

@Composable
private fun PrimaryButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BamNavyFill, contentColor = OnBrand)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Icône fichier Excel (viewBox 40 × 40), commune à tous les états de la feuille. */
private val ExcelFileIcon: ImageVector by lazy {
    val green = SolidColor(Color(0xFF1D6F42))
    ImageVector.Builder(
        name = "ExcelFile",
        defaultWidth = 40.dp,
        defaultHeight = 40.dp,
        viewportWidth = 40f,
        viewportHeight = 40f
    ).apply {
        addPath(
            pathData = addPathNodes("M9 3h15l9 9v23a2 2 0 0 1-2 2H9a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z"),
            fill = SolidColor(Color(0xFFE3F5EA)),
            stroke = green,
            strokeLineWidth = 2f
        )
        addPath(
            pathData = addPathNodes("M24 3v9h9"),
            stroke = green,
            strokeLineWidth = 2f
        )
        // rect x=4 y=18 w=20 h=13 rx=2
        path(fill = green) {
            moveTo(6f, 18f)
            horizontalLineTo(22f)
            arcTo(2f, 2f, 0f, false, true, 24f, 20f)
            verticalLineTo(29f)
            arcTo(2f, 2f, 0f, false, true, 22f, 31f)
            horizontalLineTo(6f)
            arcTo(2f, 2f, 0f, false, true, 4f, 29f)
            verticalLineTo(20f)
            arcTo(2f, 2f, 0f, false, true, 6f, 18f)
            close()
        }
        addPath(
            pathData = addPathNodes("M9 21.5l6 6M15 21.5l-6 6"),
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        )
    }.build()
}
