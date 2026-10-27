package ma.bam.inventaire.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/** Aperçu Android Studio en mode clair et en mode sombre. */
@Preview(name = "Clair", showBackground = true)
@Preview(
    name = "Sombre",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
annotation class LightDarkPreviews

/** Thème Invento posé sur le fond de l'écran, pour les aperçus. */
@Composable
fun PreviewSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    InventaireTheme {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
            content()
        }
    }
}
