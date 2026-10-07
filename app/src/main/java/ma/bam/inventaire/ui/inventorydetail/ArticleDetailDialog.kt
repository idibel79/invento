package ma.bam.inventaire.ui.inventorydetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.ui.theme.InProgressBlue
import ma.bam.inventaire.util.formatQuantity

/**
 * Aperçu détaillé d'un article (ouvert par appui long sur sa carte) : tous les champs issus
 * de l'import Excel qui ne sont pas déjà visibles sur la carte, notamment PPV, PPH et TVA.
 */
@Composable
fun ArticleDetailDialog(
    article: StockArticleEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                article.designation.ifBlank { article.codeArticle },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = InProgressBlue
            )
        },
        text = {
            Column {
                DetailRow("PID", article.codeArticle)
                DetailRow("Catégorie", article.categorie)
                DetailRow("TVA", article.tva)
                Spacer(modifier = Modifier.height(4.dp))
                Divider()
                Spacer(modifier = Modifier.height(4.dp))
                DetailRow("PPV", "${formatQuantity(article.prixUnitaire)} DH")
                DetailRow("PPH", "${formatQuantity(article.pph)} DH")
                Spacer(modifier = Modifier.height(4.dp))
                Divider()
                Spacer(modifier = Modifier.height(4.dp))
                DetailRow("Zone", article.emplacement)
                DetailRow("Stock min", formatQuantity(article.stockMin))
                DetailRow("Stock max", formatQuantity(article.stockMax))
                if (article.datePeremption.isNotBlank()) {
                    DetailRow("Péremption", article.datePeremption)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fermer") }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value.ifBlank { "-" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
