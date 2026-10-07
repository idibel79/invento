package ma.bam.inventaire.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ma.bam.inventaire.data.local.entity.StockArticleEntity

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

    @Query("SELECT COUNT(*) FROM stock_article WHERE sessionId = :sessionId")
    suspend fun countForSession(sessionId: String): Int

    @Query("SELECT COUNT(*) FROM stock_article WHERE sessionId = :sessionId AND quantiteReelle IS NOT NULL")
    suspend fun countScannedForSession(sessionId: String): Int

    @Query("SELECT COUNT(*) FROM stock_article WHERE sessionId = :sessionId AND ecart IS NOT NULL AND ecart != 0 AND ecartValide = 0")
    suspend fun countPendingEcartsForSession(sessionId: String): Int

    @Query("SELECT COUNT(*) FROM stock_article WHERE sessionId = :sessionId AND quantiteTheorique < 0")
    suspend fun countNegativeStockForSession(sessionId: String): Int

    @Query("SELECT COUNT(*) FROM stock_article WHERE sessionId = :sessionId AND quantiteTheorique = 0")
    suspend fun countEmptyStockForSession(sessionId: String): Int
}
