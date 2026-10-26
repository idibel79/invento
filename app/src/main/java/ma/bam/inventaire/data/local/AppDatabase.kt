package ma.bam.inventaire.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ma.bam.inventaire.data.local.dao.InventorySessionDao
import ma.bam.inventaire.data.local.dao.StockArticleDao
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.StockArticleEntity

@Database(
    entities = [
        InventorySessionEntity::class,
        StockArticleEntity::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventorySessionDao(): InventorySessionDao
    abstract fun stockArticleDao(): StockArticleDao

    companion object {
        const val DATABASE_NAME = "inventaire-bam.db"

        /** v6 : remarque facultative et marqueur « corrigé » (données existantes conservées). */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stock_article ADD COLUMN remarque TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE stock_article ADD COLUMN corrige INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
