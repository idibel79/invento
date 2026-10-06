package ma.bam.inventaire.ui.importinventory

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onBack: () -> Unit,
    onSessionReady: (sessionId: String) -> Unit,
    viewModel: ImportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importFile(it) }
    }

    LaunchedEffect(state) {
        val current = state
        if (current is ImportUiState.Success) {
            onSessionReady(current.sessionId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Importer la liste du stock système", style = MaterialTheme.typography.titleLarge)

            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    when (val current = state) {
                        is ImportUiState.Idle -> {
                            Text("Sélectionnez le fichier Excel (.xlsx) exporté depuis votre système de gestion.")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    filePicker.launch(
                                        arrayOf(
                                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text("Choisir un fichier")
                            }
                        }
                        is ImportUiState.Loading -> {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Import en cours...")
                        }
                        is ImportUiState.Error -> {
                            Text(
                                text = current.message,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.reset() }) {
                                Text("Réessayer")
                            }
                        }
                        is ImportUiState.Success -> {
                            Text("Import réussi : ${current.nbArticles} articles.")
                        }
                    }
                }
            }
        }
    }
}
