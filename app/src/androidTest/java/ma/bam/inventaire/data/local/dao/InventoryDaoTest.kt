package ma.bam.inventaire.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import ma.bam.inventaire.data.local.AppDatabase
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.InventoryStatus
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.data.repository.InventoryRepositoryImpl
import ma.bam.inventaire.data.repository.SessionCounters
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Tests DAO sur une base Room en mémoire (à lancer sur appareil/émulateur : connectedDebugAndroidTest). */
@RunWith(AndroidJUnit4::class)
class InventoryDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var sessionDao: InventorySessionDao
    private lateinit var articleDao: StockArticleDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sessionDao = db.inventorySessionDao()
        articleDao = db.stockArticleDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun session(id: String) = InventorySessionEntity(
        id = id,
        numero = "INV-$id",
        dateCreation = 0L,
        dateFinalisation = null,
        statut = InventoryStatus.EN_COURS,
        nomFichierSource = "test.xlsx"
    )

    private fun article(sessionId: String, code: String, quantiteTheorique: Double = 1.0, ecart: Double? = null) = StockArticleEntity(
        sessionId = sessionId,
        codeArticle = code,
        designation = code,
        categorie = "",
        tva = "",
        emplacement = "",
        quantiteTheorique = quantiteTheorique,
        datePeremption = "",
        stockMin = 0.0,
        stockMax = 0.0,
        codeBarre1 = "CB$code",
        codeBarre2 = "",
        prixUnitaire = 0.0,
        pph = 0.0,
        dateImport = 0L,
        quantiteReelle = ecart?.let { quantiteTheorique + it },
        ecart = ecart
    )

    @Test
    fun insertWithArticles_inserts_session_and_articles() = runTest {
        sessionDao.insertWithArticles(session("s1"), listOf(article("s1", "A"), article("s1", "B")))

        assertEquals(1, sessionDao.count())
        assertEquals(listOf("A", "B"), articleDao.observeBySession("s1").first().map { it.codeArticle })
    }

    @Test
    fun insertWithArticles_rolls_back_session_when_articles_fail() = runTest {
        // Article rattaché à une session inexistante : violation de clé étrangère au milieu de la transaction.
        try {
            sessionDao.insertWithArticles(session("s1"), listOf(article("s1", "A"), article("absente", "B")))
            fail("L'insertion aurait dû échouer")
        } catch (expected: Exception) {
            // attendu
        }

        assertNull(sessionDao.getById("s1"))
        assertEquals(0, sessionDao.count())
    }

    @Test
    fun findByCode_matches_barcode_or_article_code() = runTest {
        sessionDao.insertWithArticles(session("s1"), listOf(article("s1", "A")))

        assertEquals("A", articleDao.findByCode("s1", "CBA")?.codeArticle)
        assertEquals("A", articleDao.findByCode("s1", "A")?.codeArticle)
        assertNull(articleDao.findByCode("s1", "inconnu"))
    }

    @Test
    fun deleteById_cascades_to_the_session_articles_only() = runTest {
        sessionDao.insertWithArticles(session("s1"), listOf(article("s1", "A"), article("s1", "B")))
        sessionDao.insertWithArticles(session("s2"), listOf(article("s2", "C")))

        sessionDao.deleteById("s1")

        assertNull(sessionDao.getById("s1"))
        assertEquals(emptyList<StockArticleEntity>(), articleDao.observeBySession("s1").first())
        assertNull(articleDao.findByCode("s1", "A"))
        assertEquals(listOf("C"), articleDao.observeBySession("s2").first().map { it.codeArticle })
    }

    @Test
    fun observeCountersForSession_counts_each_category() = runTest {
        sessionDao.insertWithArticles(
            session("s1"),
            listOf(
                article("s1", "A", quantiteTheorique = 5.0),
                article("s1", "B", quantiteTheorique = 5.0, ecart = 0.0),
                article("s1", "C", quantiteTheorique = 5.0, ecart = 2.0),
                article("s1", "D", quantiteTheorique = -1.0),
                article("s1", "E", quantiteTheorique = 0.0)
            )
        )

        val counters = articleDao.observeCountersForSession("s1").first()

        assertEquals(5, counters.total)
        assertEquals(2, counters.scanned)
        assertEquals(1, counters.ecarts)
        assertEquals(1, counters.pendingEcarts)
        assertEquals(1, counters.negativeStock)
        assertEquals(1, counters.emptyStock)
    }

    @Test
    fun observeCountersBySession_groups_counters_by_session() = runTest {
        sessionDao.insertWithArticles(
            session("s1"),
            listOf(article("s1", "A", ecart = 2.0), article("s1", "B", quantiteTheorique = 0.0))
        )
        sessionDao.insertWithArticles(session("s2"), listOf(article("s2", "C", quantiteTheorique = -3.0)))
        sessionDao.insertWithArticles(session("vide"), emptyList())

        val counters = articleDao.observeCountersBySession().first().associate { it.sessionId to it.counters }

        assertEquals(setOf("s1", "s2"), counters.keys) // une session sans article n'a pas de ligne
        assertEquals(SessionCounters(2, 1, 1, 1, 0, 1), counters["s1"])
        assertEquals(SessionCounters(1, 0, 0, 0, 1, 0), counters["s2"])
    }

    @Test
    fun recordCorrection_sets_counted_to_system_and_keeps_original_ecart() = runTest {
        sessionDao.insertWithArticles(session("s1"), listOf(article("s1", "A", quantiteTheorique = 10.0)))
        val id = articleDao.findByCode("s1", "A")!!.id

        InventoryRepositoryImpl(sessionDao, articleDao).recordCorrection(id, 8.0, "  recompté  ")

        val corrected = articleDao.getById(id)!!
        assertEquals(10.0, corrected.quantiteTheorique, 0.0)
        assertEquals(10.0, corrected.quantiteReelle!!, 0.0)
        assertEquals(-2.0, corrected.ecart!!, 0.0)
        assertTrue(corrected.ecartValide)
        assertTrue(corrected.corrige)
        assertEquals("recompté", corrected.remarque)
    }
}
