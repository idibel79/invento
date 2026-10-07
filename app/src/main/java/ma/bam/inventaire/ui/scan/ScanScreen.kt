package ma.bam.inventaire.ui.scan

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.ui.components.SingleLineAutoSizeText
import ma.bam.inventaire.ui.quantityentry.EcartConfirmDialog
import ma.bam.inventaire.ui.quantityentry.QuantityEntryDialog
import ma.bam.inventaire.ui.quantityentry.UnknownArticleDialog
import ma.bam.inventaire.ui.theme.BamGreen

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onFinished: (sessionId: String) -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan inventaire") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (!manualMode && cameraPermission.status.isGranted) {
                        IconButton(onClick = { torchOn = !torchOn }) {
                            Icon(
                                imageVector = if (torchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                                contentDescription = if (torchOn) "Désactiver le flash" else "Activer le flash"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (!manualMode && cameraPermission.status.isGranted) {
                    CameraPreview(torchEnabled = torchOn, onBarcodeDetected = viewModel::onCodeDetected)
                } else if (!cameraPermission.status.isGranted && !manualMode) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
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
                    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                        OutlinedTextField(
                            value = manualQuery,
                            onValueChange = viewModel::setManualQuery,
                            label = { Text("Titre, code article ou code-barres") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            trailingIcon = {
                                if (manualQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setManualQuery("") }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Effacer")
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        when {
                            manualQuery.isBlank() -> Text(
                                "Tapez un titre, un code article ou un code-barres pour retrouver un produit.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            manualResults.isEmpty() -> Text(
                                "Aucun produit trouvé.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            else -> LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(manualResults, key = { it.id }) { article ->
                                    ManualSearchResultRow(
                                        article = article,
                                        onClick = { viewModel.selectManualArticle(article) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Scannés : ${uiState.counters.scanned} / ${uiState.counters.total}")
                    if (uiState.counters.pendingEcarts > 0) {
                        Text(
                            "Écarts à valider : ${uiState.counters.pendingEcarts}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { manualMode = !manualMode },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (manualMode) "Scanner" else "Saisie manuelle")
                    }
                    Button(
                        onClick = { showFinalizeConfirm = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Terminer")
                    }
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
            onValidate = { viewModel.validateEcart(dialog.article, dialog.quantiteReelle) }
        )
        is ScanDialogState.UnknownArticle -> UnknownArticleDialog(
            code = dialog.code,
            onDismiss = viewModel::dismissDialog
        )
        ScanDialogState.None -> Unit
    }

    if (showFinalizeConfirm) {
        AlertDialog(
            onDismissRequest = { showFinalizeConfirm = false },
            icon = {
                Icon(
                    Icons.Filled.TaskAlt,
                    contentDescription = null,
                    tint = BamGreen
                )
            },
            title = { SingleLineAutoSizeText("Terminer cet inventaire ?", style = MaterialTheme.typography.titleMedium) },
            text = {
                Text(
                    "${uiState.counters.scanned} / ${uiState.counters.total} articles scannés. " +
                        if (uiState.counters.pendingEcarts > 0)
                            "${uiState.counters.pendingEcarts} écart(s) restent à valider."
                        else "L'inventaire sera enregistré dans l'historique."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showFinalizeConfirm = false
                    viewModel.finalizeInventory()
                }) { Text("Oui, terminer", color = BamGreen, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showFinalizeConfirm = false }) { Text("Continuer") }
            }
        )
    }
}

@Composable
private fun ManualSearchResultRow(
    article: StockArticleEntity,
    onClick: () -> Unit
) {
    val codesBarres = listOf(article.codeBarre1, article.codeBarre2).filter { it.isNotBlank() }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                article.designation.ifBlank { article.codeArticle },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                listOf(article.codeArticle, codesBarres.joinToString(" / "))
                    .filter { it.isNotBlank() }
                    .joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
