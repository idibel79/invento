package ma.bam.inventaire.ui.quantityentry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.util.formatQuantity

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
        title = { Text("Écart détecté", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
        text = {
            Column {
                Text(
                    article.designation.ifBlank { article.codeArticle },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Quantité théorique : ${formatQuantity(article.quantiteTheorique)} ${article.unite}")
                Text("Quantité réelle saisie : ${formatQuantity(quantiteReelle)} ${article.unite}")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Écart : ${formatQuantity(ecart, showSign = true)} ${article.unite}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onValidate) {
                Text(
                    "Valider l'écart",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Corriger la quantité") }
        }
    )
}
