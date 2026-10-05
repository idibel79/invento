package ma.bam.inventaire.ui.quantityentry

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import ma.bam.inventaire.ui.components.SingleLineAutoSizeText

@Composable
fun UnknownArticleDialog(
    code: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Filled.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        title = { SingleLineAutoSizeText("Article non reconnu", style = MaterialTheme.typography.titleMedium) },
        text = {
            Text("Le code \"$code\" ne correspond à aucun article de cet inventaire. Vérifiez qu'il s'agit du bon article ou saisissez-le manuellement.")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK", fontWeight = FontWeight.SemiBold) }
        }
    )
}
