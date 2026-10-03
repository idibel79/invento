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

    @Query("SELECT * FROM stock_article WHERE sessionId = :sessionId AND codeBarre = :codeBarre LIMIT 1")
    suspend fun findByBarcode(sessionId: String, codeBarre: String): StockArticleEntity?

    @Query("SELECT COUNT(*) FROM stock_article WHERE sessionId = :sessionId")
    suspend fun countForSession(sessionId: String): Int

    @Query("SELECT COUNT(*) FROM stock_article WHERE sessionId = :sessionId AND quantiteReelle IS NOT NULL")
    suspend fun countScannedForSession(sessionId: String): Int

    @Query("SELECT COUNT(*) FROM stock_article WHERE sessionId = :sessionId AND ecart IS NOT NULL AND ecart != 0 AND ecartValide = 0")
    suspend fun countPendingEcartsForSession(sessionId: String): Int
}
