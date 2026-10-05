package ma.bam.inventaire.ui.quantityentry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ma.bam.inventaire.R
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.ui.components.SingleLineAutoSizeText
import ma.bam.inventaire.util.formatQuantity

@Composable
fun QuantityEntryDialog(
    article: StockArticleEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var text by remember {
        mutableStateOf(article.quantiteReelle?.let { formatQuantity(it) }.orEmpty())
    }
    val quantite = text.replace(",", ".").toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
        title = {
            SingleLineAutoSizeText(
                text = article.designation.ifBlank { article.codeArticle },
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                InfoRow(painter = painterResource(id = R.drawable.ic_pid_cross), text = article.codeArticle)
                InfoRow(icon = Icons.Filled.Place, text = article.emplacement)
                Text(
                    "Quantité théorique : ${formatQuantity(article.quantiteTheorique)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Quantité réelle constatée") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = quantite != null,
                onClick = { quantite?.let(onConfirm) }
            ) {
                Text("Valider", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    InfoRow(text = text) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(painter: Painter, text: String) {
    InfoRow(text = text) {
        Icon(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(text: String, icon: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        icon()
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
