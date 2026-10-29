package ma.bam.inventaire.ui.home

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ma.bam.inventaire.R
import ma.bam.inventaire.ui.inventorylist.SessionListItem
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.StatusEcart
import ma.bam.inventaire.ui.theme.StatusSuccess
import ma.bam.inventaire.ui.theme.TextSecondary

/** Filtres de la liste, un par tuile, dans l'ordre d'affichage. */
internal enum class HomeFilter(@StringRes val labelRes: Int) {
    ONGOING(R.string.status_ongoing),
    PENDING(R.string.status_pending),
    ECARTS(R.string.filter_ecarts),
    FINALIZED(R.string.home_finalized),
    CLOSED(R.string.home_closed);

    val color: Color
        @Composable get() = when (this) {
            ONGOING -> BamPrimary
            PENDING -> TextSecondary
            ECARTS -> StatusEcart
            FINALIZED -> StatusSuccess
            CLOSED -> BamNavy
        }

    fun matches(item: SessionListItem): Boolean = when (this) {
        ONGOING -> item.isOngoing()
        PENDING -> item.isPending()
        ECARTS -> item.hasEcarts()
        FINALIZED -> item.isFinalized()
        CLOSED -> item.session.verrouille
    }
}

/** Finalisé = tous les articles comptés (carte verte), non clôturé : un clôturé va dans « Clôturés ». */
private fun SessionListItem.isFinalized(): Boolean = !session.verrouille && counters.total > 0 && counters.scanned >= counters.total

/** Ni finalisé ni clôturé, au moins un article compté. */
private fun SessionListItem.isOngoing(): Boolean = !session.verrouille && !isFinalized() && counters.scanned > 0

/** Ni finalisé ni clôturé, aucun article compté. */
private fun SessionListItem.isPending(): Boolean = !session.verrouille && !isFinalized() && counters.scanned == 0

/** Au moins un écart (validé ou non), même définition que le filtre « Écarts » de la page produits. */
private fun SessionListItem.hasEcarts(): Boolean = counters.ecarts > 0
