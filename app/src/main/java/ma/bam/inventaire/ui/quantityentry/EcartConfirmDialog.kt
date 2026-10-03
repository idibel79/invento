package ma.bam.inventaire.ui.quantityentry

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import ma.bam.inventaire.data.local.entity.StockArticleEntity

@Composable
fun EcartConfirmDialog(
    article: StockArticleEntity,
    quantiteReelle: Double,
    ecart: Double,
    onDismiss: () -> Unit,
    onValidate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Écart détecté") },
        text = {
            Column {
                Text(article.designation.ifBlank { article.codeArticle })
                Text("Quantité théorique : ${article.quantiteTheorique} ${article.unite}")
                Text("Quantité réelle : $quantiteReelle ${article.unite}")
                Text(
                    text = "Écart : ${if (ecart > 0) "+" else ""}$ecart ${article.unite}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onValidate) { Text("Valider l'écart") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Corriger la quantité") }
        }
    )
}
