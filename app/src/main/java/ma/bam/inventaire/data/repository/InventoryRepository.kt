package ma.bam.inventaire.data.repository

import kotlinx.coroutines.flow.Flow
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.StockArticleEntity

/**
 * Source de vérité pour les sessions d'inventaire et leurs articles.
 * Aujourd'hui adossée à Room (SQLite local) ; l'implémentation pourra être
 * remplacée/complétée par un accès réseau au SI existant sans changer l'UI.
 */
interface InventoryRepository {

    fun observeSessions(): Flow<List<InventorySessionEntity>>

    fun observeSession(sessionId: String): Flow<InventorySessionEntity?>

    fun observeArticles(sessionId: String): Flow<List<StockArticleEntity>>

    suspend fun createSession(fileName: String, articles: List<ImportedArticle>): InventorySessionEntity

    /** Recherche un article par code-barres scanné ou par code_article saisi manuellement. */
    suspend fun findArticleByCode(sessionId: String, code: String): StockArticleEntity?

    suspend fun recordScan(articleId: Long, quantiteReelle: Double, ecartValide: Boolean)

    suspend fun updateQuantity(articleId: Long, quantiteReelle: Double, ecartValide: Boolean)

    /** Marque la session comme finalisée (fin du comptage) — indépendant du verrouillage. */
    suspend fun finalizeSession(sessionId: String)

    /** Verrouille la session : plus aucune modification des quantités n'est permise. */
    suspend fun closeSession(sessionId: String)

    /** Lève le verrouillage posé par [closeSession], pour autoriser à nouveau les modifications. */
    suspend fun reopenSession(sessionId: String)

    suspend fun deleteSession(sessionId: String)

    suspend fun getSessionCounters(sessionId: String): SessionCounters
}

data class SessionCounters(
    val total: Int,
    val scanned: Int,
    val pendingEcarts: Int
)

/** Article tel que décodé depuis le fichier Excel importé, avant persistance. */
data class ImportedArticle(
    val codeArticle: String,
    val codeBarre: String,
    val reference: String,
    val designation: String,
    val description: String,
    val categorie: String,
    val emplacement: String,
    val quantiteTheorique: Double,
    val unite: String,
    val prixUnitaire: Double,
    val dateImport: Long
)
