package ma.bam.inventaire.ui.home

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ma.bam.inventaire.R
import ma.bam.inventaire.ui.inventorylist.SessionListItem
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.StatusEcart
import ma.bam.inventaire.ui.theme.StatusSuccess
import ma.bam.inventaire.ui.theme.TextSecondary

/** Filtres de la liste, un par tuile, dans l'ordre d'affichage. */
internal enum class HomeFilter(@StringRes val labelRes: Int) {
    ONGOING(R.string.status_ongoing),
    PENDING(R.string.status_pending),
    ECARTS(R.string.filter_ecarts),
    FINALIZED(R.string.home_finalized);

    val color: Color
        @Composable get() = when (this) {
            ONGOING -> BamPrimary
            PENDING -> TextSecondary
            ECARTS -> StatusEcart
            FINALIZED -> StatusSuccess
        }

    fun matches(item: SessionListItem): Boolean = when (this) {
        ONGOING -> item.isOngoing()
        PENDING -> item.isPending()
        ECARTS -> item.hasEcarts()
        FINALIZED -> item.isFinalized()
    }
}

/** Finalisé = tous les articles comptés (carte verte) ou clôturé (cadenas fermé). */
private fun SessionListItem.isFinalized(): Boolean = session.verrouille || (counters.total > 0 && counters.scanned >= counters.total)

/** Non finalisé, au moins un article compté. */
private fun SessionListItem.isOngoing(): Boolean = !isFinalized() && counters.scanned > 0

/** Non finalisé, aucun article compté. */
private fun SessionListItem.isPending(): Boolean = !isFinalized() && counters.scanned == 0

/** Au moins un écart (validé ou non), même définition que le filtre « Écarts » de la page produits. */
private fun SessionListItem.hasEcarts(): Boolean = counters.ecarts > 0
