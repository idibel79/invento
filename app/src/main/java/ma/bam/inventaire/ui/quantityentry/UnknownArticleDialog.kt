package ma.bam.inventaire.ui.quantityentry

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import ma.bam.inventaire.R
import ma.bam.inventaire.ui.components.PrimaryActionButton
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
        title = { SingleLineAutoSizeText(stringResource(R.string.unknown_article_title), style = MaterialTheme.typography.titleMedium) },
        text = {
            Text(stringResource(R.string.unknown_article_message, code))
        },
        confirmButton = {
            PrimaryActionButton(stringResource(R.string.common_ok), onDismiss, Modifier.fillMaxWidth())
        }
    )
}
