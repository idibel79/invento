package ma.bam.inventaire.data.local

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import ma.bam.inventaire.data.local.entity.InventoryStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Migrations sur une vraie base SQLite créée depuis les schémas exportés (app/schemas, ajoutés aux assets
 * androidTest). À lancer sur appareil/émulateur : connectedDebugAndroidTest.
 *
 * Le schéma 5.json n'a jamais été exporté : la migration 5 -> 6 ne peut pas être testée ici. À partir de la v6,
 * chaque nouvelle version ajoute un cas sur le modèle de [v6_data_survives_opening_with_all_migrations] :
 * créer la base dans l'ancienne version, y insérer des données, migrer, vérifier que rien n'est perdu.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun v6_data_survives_opening_with_all_migrations() = runTest {
        helper.createDatabase(TEST_DB, FIRST_EXPORTED_VERSION).use { db ->
            db.execSQL(
                """
                INSERT INTO inventory_session (id, numero, dateCreation, dateFinalisation, statut, nomFichierSource, verrouille)
                VALUES ('s1', 'INV-001', 1000, NULL, 'EN_COURS', 'stock.xlsx', 1)
                """.trimIndent()
            )
            insertArticle(db, code = "A", quantiteReelle = "7.0", ecart = "2.0", remarque = "'casse'", corrige = 1)
            insertArticle(db, code = "B", quantiteReelle = "NULL", ecart = "NULL", remarque = "NULL", corrige = 0)
        }

        // Applique toutes les migrations jusqu'à la version courante et valide le schéma obtenu.
        helper.runMigrationsAndValidate(TEST_DB, AppDatabase.VERSION, true, *AppDatabase.ALL_MIGRATIONS.toTypedArray())
            .close()

        // Ouvre la base comme l'application (même liste de migrations, sans repli destructif).
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB)
            .addMigrations(*AppDatabase.ALL_MIGRATIONS.toTypedArray())
            .allowMainThreadQueries()
            .build()
        try {
            val session = database.inventorySessionDao().getById("s1")
            assertNotNull(session)
            assertEquals("INV-001", session!!.numero)
            assertEquals(InventoryStatus.EN_COURS, session.statut)
            assertTrue(session.verrouille)

            val articles = database.stockArticleDao().observeBySession("s1").first()
            assertEquals(listOf("A", "B"), articles.map { it.codeArticle })
            val a = articles[0]
            assertEquals(7.0, a.quantiteReelle!!, 0.0)
            assertEquals(2.0, a.ecart!!, 0.0)
            assertEquals("casse", a.remarque)
            assertTrue(a.corrige)
            assertNull(articles[1].quantiteReelle)
        } finally {
            database.close()
        }
    }

    private fun insertArticle(
        db: SupportSQLiteDatabase,
        code: String,
        quantiteReelle: String,
        ecart: String,
        remarque: String,
        corrige: Int
    ) {
        db.execSQL(
            """
            INSERT INTO stock_article (sessionId, codeArticle, designation, categorie, tva, emplacement,
                quantiteTheorique, datePeremption, stockMin, stockMax, codeBarre1, codeBarre2, prixUnitaire, pph,
                dateImport, quantiteReelle, ecart, ecartValide, dateScan, remarque, corrige)
            VALUES ('s1', '$code', 'Produit $code', '', '', 'Zone 1', 5.0, '', 0.0, 0.0, 'CB$code', '', 10.0, 8.0,
                1000, $quantiteReelle, $ecart, 0, NULL, $remarque, $corrige)
            """.trimIndent()
        )
    }

    private companion object {
        const val TEST_DB = "migration-test.db"

        /** Premier schéma exporté (app/schemas/.../6.json). */
        const val FIRST_EXPORTED_VERSION = 6
    }
}
