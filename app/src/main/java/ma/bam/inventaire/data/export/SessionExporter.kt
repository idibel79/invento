package ma.bam.inventaire.data.export

import android.net.Uri
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.StockArticleEntity

/** Export d'une session d'inventaire vers un fichier partageable. */
interface SessionExporter {

    /** Écrit le classeur .xlsx de [session] et de ses [articles], et renvoie un Uri partageable. */
    suspend fun export(session: InventorySessionEntity, articles: List<StockArticleEntity>): Uri
}
