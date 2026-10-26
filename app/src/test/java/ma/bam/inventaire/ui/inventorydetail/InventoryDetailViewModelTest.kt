package ma.bam.inventaire.ui.inventorydetail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ma.bam.inventaire.data.export.SessionExporter
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.domain.ArticleFilter
import ma.bam.inventaire.domain.ArticleSort
import ma.bam.inventaire.testutil.FakeInventoryRepository
import ma.bam.inventaire.testutil.testArticle
import ma.bam.inventaire.testutil.testSession
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryDetailViewModelTest {

    /** Exporteur qui échoue toujours (un Uri ne peut pas être construit hors Android). */
    private class FailingExporter : SessionExporter {
        val exported = mutableListOf<String>()

        override suspend fun export(session: InventorySessionEntity, articles: List<StockArticleEntity>): Uri {
            exported += session.id
            throw IOException("disque plein")
        }
    }

    private val articles = listOf(
        testArticle(id = 1, designation = "Banane", emplacement = "A", categorie = "Fruit", prixUnitaire = 5.0),
        testArticle(
            id = 2,
            designation = "Abricot",
            emplacement = "B",
            categorie = "Fruit",
            prixUnitaire = 9.0,
            quantiteReelle = 12.0,
            ecart = 2.0
        ),
        testArticle(
            id = 3,
            designation = "Carotte",
            emplacement = "A",
            categorie = "Legume",
            prixUnitaire = 1.0,
            quantiteReelle = 10.0,
            ecart = 0.0,
            ecartValide = true
        ),
        testArticle(
            id = 4,
            designation = "Avocat",
            emplacement = "A",
            categorie = "Fruit",
            prixUnitaire = 3.0,
            quantiteReelle = 7.0,
            ecart = -3.0
        )
    )

    private lateinit var repository: FakeInventoryRepository
    private lateinit var exporter: FailingExporter
    private lateinit var viewModel: InventoryDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakeInventoryRepository(articles)
        repository.session.value = testSession("s1")
        exporter = FailingExporter()
        viewModel = InventoryDetailViewModel(
            SavedStateHandle(mapOf("sessionId" to "s1")),
            repository,
            exporter,
            UnconfinedTestDispatcher()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.collectUiState() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    private fun visibleIds() = viewModel.uiState.value.articles.map { it.id }

    @Test
    fun `etat initial avec session, tous les articles et agregats`() = runTest {
        collectUiState()

        val state = viewModel.uiState.value
        assertEquals("s1", state.session?.id)
        assertEquals(listOf(1L, 2L, 3L, 4L), visibleIds())
        assertEquals(4, state.stats.filterCounts[ArticleFilter.TOUS])
        assertEquals(3, state.stats.filterCounts[ArticleFilter.SCANNES])
        assertEquals(2, state.stats.filterCounts[ArticleFilter.ECARTS])
        assertEquals(listOf("A", "B"), state.stats.positions)
        assertEquals(1f..9f, state.stats.priceBounds)
    }

    @Test
    fun `filtre, recherche et tri se refletent dans l'uiState`() = runTest {
        collectUiState()

        viewModel.setFilter(ArticleFilter.ECARTS)
        assertEquals(ArticleFilter.ECARTS, viewModel.uiState.value.filter)
        assertEquals(listOf(2L, 4L), visibleIds())

        viewModel.setSort(ArticleSort.TITRE_AZ)
        assertEquals(ArticleSort.TITRE_AZ, viewModel.uiState.value.sort)
        assertEquals(listOf(2L, 4L), visibleIds())
        viewModel.setSort(ArticleSort.PRIX_ASC)
        assertEquals(listOf(4L, 2L), visibleIds())

        viewModel.setSearchQuery("avo")
        assertEquals("avo", viewModel.uiState.value.searchQuery)
        assertEquals(listOf(4L), visibleIds())

        // Les compteurs restent calculés sur la session entière.
        assertEquals(4, viewModel.uiState.value.stats.filterCounts[ArticleFilter.TOUS])
    }

    @Test
    fun `filtres de zone, categorie et prix se cumulent`() = runTest {
        collectUiState()

        viewModel.setPositionFilter("A")
        assertEquals(listOf(1L, 3L, 4L), visibleIds())
        viewModel.setCategoryFilter("Fruit")
        assertEquals(listOf(1L, 4L), visibleIds())
        viewModel.setPriceRangeFilter(4f..9f)
        assertEquals(listOf(1L), visibleIds())

        val state = viewModel.uiState.value
        assertEquals("A", state.positionFilter)
        assertEquals("Fruit", state.categoryFilter)
        assertEquals(4f..9f, state.priceRangeFilter)
    }

    @Test
    fun `la liste suit les changements du depot`() = runTest {
        collectUiState()
        viewModel.setFilter(ArticleFilter.NON_SCANNES)
        assertEquals(listOf(1L), visibleIds())

        repository.articles.value = articles + testArticle(id = 5, designation = "Datte")

        assertEquals(listOf(1L, 5L), visibleIds())
        assertEquals(5, viewModel.uiState.value.stats.filterCounts[ArticleFilter.TOUS])
    }

    @Test
    fun `previewCount compte sans appliquer les filtres`() = runTest {
        collectUiState()
        viewModel.setSearchQuery("a")

        assertEquals(2, viewModel.previewCount(ArticleFilter.ECARTS, null, null, null))
        assertEquals(1, viewModel.previewCount(ArticleFilter.TOUS, "B", null, null))
        assertEquals(ArticleFilter.TOUS, viewModel.uiState.value.filter)
    }

    @Test
    fun `export en erreur emet un evenement Error`() = runTest {
        collectUiState()

        viewModel.exportCurrentSession()

        val event = viewModel.exportEvents.first()
        assertTrue(event is ExportEvent.Error)
        assertTrue((event as ExportEvent.Error).message.startsWith("Échec de l'export"))
        assertTrue(event.message.contains("disque plein"))
        assertEquals(listOf("s1"), exporter.exported)
    }

    @Test
    fun `export sans session emet un evenement Error`() = runTest {
        repository.session.value = null
        collectUiState()

        viewModel.exportCurrentSession()

        assertTrue(viewModel.exportEvents.first() is ExportEvent.Error)
        assertTrue(exporter.exported.isEmpty())
    }
}
