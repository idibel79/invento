package ma.bam.inventaire.ui.quantityentry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import ma.bam.inventaire.ui.theme.CardSurface

/**
 * Cadre commun des popups de saisie (quantité, écart détecté) : voile #0F1A33 à 42 % qui ferme la
 * popup au toucher, carte blanche centrée (coins 24dp, élévation 10dp, marges 16dp), défilante et
 * remontée au-dessus du clavier, blocs espacés de 10dp.
 */
@Composable
internal fun EntryPopup(onDismiss: () -> Unit, contentPadding: PaddingValues, content: @Composable ColumnScope.() -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        // Le voile est dessiné ici (#0F1A33 à 42 %) : on coupe l'assombrissement par défaut de la fenêtre.
        (LocalView.current.parent as? DialogWindowProvider)?.window?.setDimAmount(0f)
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Scrim)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                EntryPopupCard(contentPadding = contentPadding, content = content)
            }
        }
    }
}

/** Carte de la popup, sans la fenêtre : utilisée telle quelle par les aperçus. */
@Composable
internal fun EntryPopupCard(contentPadding: PaddingValues, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CardSurface,
        shadowElevation = 10.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}
