package ma.bam.inventaire.ui.scan

import android.Manifest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import ma.bam.inventaire.ui.components.CompactButtonPadding
import ma.bam.inventaire.ui.components.PillShape
import ma.bam.inventaire.ui.components.ConfirmDialog
import ma.bam.inventaire.ui.components.ConfirmTone
import ma.bam.inventaire.ui.components.boldNavy
import ma.bam.inventaire.ui.components.rememberTapHaptic
import ma.bam.inventaire.ui.inventorydetail.ArticleRow
import ma.bam.inventaire.ui.quantityentry.EcartConfirmDialog
import ma.bam.inventaire.ui.quantityentry.QuantityEntryDialog
import ma.bam.inventaire.ui.quantityentry.UnknownArticleDialog
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamNavyFill
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.ControlBorder
import ma.bam.inventaire.ui.theme.OnBrand
import ma.bam.inventaire.ui.theme.TrackColor
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.StatusEcart
import ma.bam.inventaire.ui.theme.StatusEcartBg
import ma.bam.inventaire.ui.theme.TabularNums
import ma.bam.inventaire.ui.theme.TextMuted
import ma.bam.inventaire.ui.theme.TextPrimary
import ma.bam.inventaire.ui.theme.TextSecondary

private val FieldBorder: Color @Composable get() = TrackColor
private val ProgressTrack: Color @Composable get() = TrackColor
private val ButtonOutline: Color @Composable get() = ControlBorder

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onFinished: (sessionId: String) -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tapHaptic = rememberTapHaptic()
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    var manualMode by remember { mutableStateOf(false) }
    val manualQuery by viewModel.manualQuery.collectAsState()
    val manualResults by viewModel.manualResults.collectAsState()
    var showFinalizeConfirm by remember { mutableStateOf(false) }
    var torchOn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(uiState.finalized) {
        if (uiState.finalized) onFinished(viewModel.sessionId)
    }

    LaunchedEffect(manualMode) {
        if (manualMode) torchOn = false
    }

    val cameraVisible = !manualMode && cameraPermission.status.isGranted
    val density = LocalDensity.current
    var sheetHeight by remember { mutableStateOf(0.dp) }
    val counters = uiState.counters
    val progress = if (counters.total > 0) counters.scanned.toFloat() / counters.total.toFloat() else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (cameraVisible) Color.Black else MaterialTheme.colorScheme.background)
    ) {
        if (cameraVisible) {
            // Caméra plein écran : le centre de l'aperçu (fenêtre de visée) reste le centre de l'image analysée.
            CameraPreview(torchEnabled = torchOn, onBarcodeDetected = viewModel::onCodeDetected)
        } else if (!cameraPermission.status.isGranted && !manualMode) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 64.dp, bottom = sheetHeight)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Autorisation caméra requise pour scanner les codes-barres.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                    Text("Autoriser la caméra")
                }
            }
        } else {
            // Saisie manuelle : champ toujours en haut, résultats en cartes produit de la liste.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 64.dp)
            ) {
                ManualSearchField(
                    value = manualQuery,
                    onValueChange = viewModel::setManualQuery,
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 4.dp)
                )
                val hintModifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp)
                when {
                    manualQuery.isBlank() -> Text(
                        "Tapez un code ou un titre pour retrouver un produit.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = hintModifier
                    )
                    manualResults.isEmpty() -> Text(
                        "Aucun produit trouvé.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = hintModifier
                    )
                    else -> {
                        Text(
                            buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = BamNavy)) {
                                    append("${manualResults.size}")
                                }
                                append(if (manualResults.size > 1) " produits trouvés" else " produit trouvé")
                            },
                            fontSize = 12.sp,
                            color = TextSecondary,
                            style = LocalTextStyle.current.copy(fontFeatureSettings = TabularNums),
                            modifier = hintModifier
                        )
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(
                                start = 14.dp,
                                end = 14.dp,
                                top = 8.dp,
                                bottom = sheetHeight + 8.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(manualResults, key = { it.id }) { article ->
                                ArticleRow(
                                    item = article,
                                    locked = false,
                                    onEdit = {},
                                    onShowDetail = {},
                                    onSelect = { viewModel.selectManualArticle(article) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Barre du haut transparente, posée sur la caméra.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OverlayIconButton(
                onClick = {
                    tapHaptic()
                    onBack()
                },
                onCamera = cameraVisible
            ) { tint ->
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = tint)
            }
            Text(
                "Scan inventaire",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (cameraVisible) Color.White else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            if (cameraVisible) {
                OverlayIconButton(onClick = { torchOn = !torchOn }, onCamera = true) { tint ->
                    Icon(
                        imageVector = if (torchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        contentDescription = if (torchOn) "Désactiver le flash" else "Activer le flash",
                        tint = tint
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(44.dp))
            }
        }

        // Barre du bas : progression, écarts et actions (la même en scan et en saisie manuelle).
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onSizeChanged { sheetHeight = with(density) { it.height.toDp() } }
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    ambientColor = TextPrimary.copy(alpha = 0.3f),
                    spotColor = TextPrimary.copy(alpha = 0.3f)
                )
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(CardSurface)
                .navigationBarsPadding()
                .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    buildAnnotatedString {
                        append("Scannés ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = BamNavy)) {
                            append("${counters.scanned} / ${counters.total}")
                        }
                    },
                    fontSize = 12.5.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    softWrap = false,
                    style = LocalTextStyle.current.copy(fontFeatureSettings = TabularNums)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(ProgressTrack)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .background(BamPrimary)
                    )
                }
                if (counters.pendingEcarts > 0) {
                    Text(
                        "${counters.pendingEcarts} écart${if (counters.pendingEcarts > 1) "s" else ""} à valider",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusEcart,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier
                            .clip(PillShape)
                            .background(StatusEcartBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { manualMode = !manualMode },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ButtonOutline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BamNavy),
                    contentPadding = CompactButtonPadding,
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(
                        imageVector = if (manualMode) Icons.Outlined.QrCodeScanner else Icons.Filled.Keyboard,
                        contentDescription = null,
                        tint = BamNavy,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        if (manualMode) "Scanner" else "Saisie",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                Button(
                    onClick = { showFinalizeConfirm = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BamNavyFill, contentColor = OnBrand),
                    contentPadding = CompactButtonPadding,
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Text("Terminer", fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }

    when (val dialog = uiState.dialog) {
        is ScanDialogState.QuantityEntry -> QuantityEntryDialog(
            article = dialog.article,
            onDismiss = viewModel::dismissDialog,
            onConfirm = { quantite -> viewModel.confirmQuantity(dialog.article, quantite) }
        )
        is ScanDialogState.EcartConfirm -> EcartConfirmDialog(
            article = dialog.article,
            quantiteReelle = dialog.quantiteReelle,
            ecart = dialog.ecart,
            onDismiss = viewModel::dismissDialog,
            onValidate = { viewModel.validateEcart(dialog.article, dialog.quantiteReelle) },
            onSaveCorrection = { quantite, remarque ->
                viewModel.saveCorrection(dialog.article, quantite, remarque)
            }
        )
        is ScanDialogState.UnknownArticle -> UnknownArticleDialog(
            code = dialog.code,
            onDismiss = viewModel::dismissDialog
        )
        ScanDialogState.None -> Unit
    }

    if (showFinalizeConfirm) {
        val counters = uiState.counters
        ConfirmDialog(
            icon = Icons.Outlined.CheckCircle,
            tone = ConfirmTone.NAVY,
            title = "Terminer cet inventaire ?",
            message = boldNavy(
                "", "${counters.scanned} / ${counters.total}",
                " articles scannés. " +
                    if (counters.pendingEcarts > 0)
                        "${counters.pendingEcarts} écart(s) restent à valider."
                    else "L'inventaire sera enregistré dans l'historique."
            ),
            progress = if (counters.total > 0) counters.scanned.toFloat() / counters.total else 0f,
            confirmLabel = "Oui, terminer",
            dismissLabel = "Continuer",
            onConfirm = {
                showFinalizeConfirm = false
                viewModel.finalizeInventory()
            },
            onDismiss = { showFinalizeConfirm = false }
        )
    }
}

/**
 * Champ de recherche de la saisie manuelle : 46dp, coins 12dp, bordure marine 2dp au focus.
 * Le clavier s'ouvre dès l'entrée en saisie manuelle.
 */
@Composable
private fun ManualSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
        cursorBrush = SolidColor(BamPrimary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .focusRequester(focusRequester),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardSurface)
                    .border(
                        width = if (focused) 2.dp else 1.dp,
                        color = if (focused) BamNavy else FieldBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp)
            ) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = if (focused) BamNavy else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            "Code-barres, PID ou titre",
                            fontSize = 14.sp,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                    innerTextField()
                }
                if (value.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable { onValueChange("") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Effacer",
                            tint = TextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    )
}

/** Bouton carré arrondi de la barre du haut : fond blanc translucide sur la caméra. */
@Composable
private fun OverlayIconButton(
    onClick: () -> Unit,
    onCamera: Boolean,
    icon: @Composable (tint: Color) -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (onCamera) Color.White.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        icon(if (onCamera) Color.White else MaterialTheme.colorScheme.onSurface)
    }
}
