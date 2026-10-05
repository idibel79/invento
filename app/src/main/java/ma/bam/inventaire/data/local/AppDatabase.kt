package ma.bam.inventaire.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ma.bam.inventaire.data.local.dao.InventorySessionDao
import ma.bam.inventaire.data.local.dao.StockArticleDao
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.StockArticleEntity

@Database(
    entities = [
        InventorySessionEntity::class,
        StockArticleEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventorySessionDao(): InventorySessionDao
    abstract fun stockArticleDao(): StockArticleDao

    companion object {
        const val DATABASE_NAME = "inventaire-bam.db"
    }
}
