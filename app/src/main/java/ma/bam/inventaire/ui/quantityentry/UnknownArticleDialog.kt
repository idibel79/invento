package ma.bam.inventaire.ui.quantityentry

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun UnknownArticleDialog(
    code: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Article non reconnu") },
        text = { Text("Le code \"$code\" ne correspond à aucun article de cet inventaire.") },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}
