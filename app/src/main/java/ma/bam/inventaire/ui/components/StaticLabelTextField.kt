package ma.bam.inventaire.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Champ texte dont l'indice reste toujours affiché, en petit, posé sur le trait du cadre
 * (comme un libellé Material "docké"), au lieu du comportement par défaut de
 * [OutlinedTextField] qui l'affiche en grand à l'intérieur du champ puis l'anime vers le
 * bord uniquement au focus. [containerColor] doit correspondre à la couleur de fond derrière
 * le champ (écran ou AlertDialog) pour que l'indice se détache correctement du trait.
 */
@Composable
fun StaticLabelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    isError: Boolean = false,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            isError = isError,
            readOnly = readOnly,
            keyboardOptions = keyboardOptions,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier
                .padding(start = 12.dp)
                .offset(y = (-8).dp)
                .background(containerColor)
                .padding(horizontal = 4.dp)
        )
    }
}
