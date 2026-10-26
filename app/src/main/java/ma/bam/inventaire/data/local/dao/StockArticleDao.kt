package ma.bam.inventaire.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.data.repository.SessionCounters
import ma.bam.inventaire.domain.EcartCalculator

@Dao
interface StockArticleDao {

    @Insert
    suspend fun insert(article: StockArticleEntity): Long

    @Insert
    suspend fun insertAll(articles: List<StockArticleEntity>): List<Long>

    @Update
    suspend fun update(article: StockArticleEntity)

    @Query("SELECT * FROM stock_article WHERE id = :id")
    suspend fun getById(id: Long): StockArticleEntity?

    @Query("SELECT * FROM stock_article WHERE sessionId = :sessionId ORDER BY codeArticle COLLATE NOCASE ASC")
    fun observeBySession(sessionId: String): Flow<List<StockArticleEntity>>

    /** Recherche par l'un des deux codes-barres scannés ou par code_article saisi manuellement. */
    @Query(
        """
        SELECT * FROM stock_article
        WHERE sessionId = :sessionId
          AND (codeBarre1 = :code OR codeBarre2 = :code OR codeArticle = :code)
        LIMIT 1
        """
    )
    suspend fun findByCode(sessionId: String, code: String): StockArticleEntity?

    /** Les compteurs d'une session en une seule lecture de la table (au lieu de 6 requêtes). */
    // Seuil interpolé depuis EcartCalculator.SEUIL_ECART_NUL ; `ecarts` suit EcartCalculator.isEcartNonCorrige
    // (les corrigés gardent leur écart d'origine mais ne comptent pas comme écarts), pour que tous les
    // compteurs d'écarts concordent avec la page produits.
    @Query(
        """
        SELECT COUNT(*) AS total,
            COALESCE(SUM(quantiteReelle IS NOT NULL), 0) AS scanned,
            COALESCE(SUM(ecart IS NOT NULL AND ABS(ecart) > ${EcartCalculator.SEUIL_ECART_NUL} AND corrige = 0), 0) AS ecarts,
            COALESCE(SUM(ecart IS NOT NULL AND ABS(ecart) > ${EcartCalculator.SEUIL_ECART_NUL} AND ecartValide = 0), 0)
                AS pendingEcarts,
            COALESCE(SUM(quantiteTheorique < 0), 0) AS negativeStock,
            COALESCE(SUM(quantiteTheorique = 0), 0) AS emptyStock
        FROM stock_article WHERE sessionId = :sessionId
        """
    )
    suspend fun countersForSession(sessionId: String): SessionCounters
}
