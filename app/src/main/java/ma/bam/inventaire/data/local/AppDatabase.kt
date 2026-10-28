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

/**
 * Règle : les inventaires ne doivent jamais être effacés par une mise à jour (pas de migration destructive).
 * Toute modification d'entité = [VERSION] + 1, une [Migration] ajoutée à [ALL_MIGRATIONS], le schéma
 * exporté commité (app/schemas) et un cas dans MigrationTest. DatabaseMigrationsTest fait échouer la CI
 * s'il manque une migration ou un schéma.
 */
@Database(
    entities = [
        InventorySessionEntity::class,
        StockArticleEntity::class
    ],
    version = AppDatabase.VERSION,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventorySessionDao(): InventorySessionDao
    abstract fun stockArticleDao(): StockArticleDao

    companion object {
        const val DATABASE_NAME = "inventaire-bam.db"
        const val VERSION = 6

        /** v6 : remarque facultative et marqueur « corrigé » (données existantes conservées). */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stock_article ADD COLUMN remarque TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE stock_article ADD COLUMN corrige INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** Toutes les migrations, dans l'ordre : c'est cette liste que le builder Room enregistre. */
        val ALL_MIGRATIONS: List<Migration> = listOf(MIGRATION_5_6)
    }
}
