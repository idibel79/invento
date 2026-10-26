package ma.bam.inventaire.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.StockArticleEntity

@Dao
interface InventorySessionDao {

    @Insert
    suspend fun insert(session: InventorySessionEntity)

    @Insert
    suspend fun insertArticles(articles: List<StockArticleEntity>)

    /** Crée la session et ses articles en une seule transaction : tout ou rien. */
    @Transaction
    suspend fun insertWithArticles(session: InventorySessionEntity, articles: List<StockArticleEntity>) {
        insert(session)
        insertArticles(articles)
    }

    @Update
    suspend fun update(session: InventorySessionEntity)

    @Query("SELECT * FROM inventory_session ORDER BY dateCreation DESC")
    fun observeAll(): Flow<List<InventorySessionEntity>>

    @Query("SELECT * FROM inventory_session WHERE id = :sessionId")
    suspend fun getById(sessionId: String): InventorySessionEntity?

    @Query("SELECT * FROM inventory_session WHERE id = :sessionId")
    fun observeById(sessionId: String): Flow<InventorySessionEntity?>

    @Query("SELECT COUNT(*) FROM inventory_session")
    suspend fun count(): Int

    @Query("DELETE FROM inventory_session WHERE id = :sessionId")
    suspend fun deleteById(sessionId: String)
}
