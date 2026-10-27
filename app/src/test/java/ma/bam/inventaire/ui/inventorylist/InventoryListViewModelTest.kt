package ma.bam.inventaire.ui.inventorylist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ma.bam.inventaire.data.repository.SessionCounters
import ma.bam.inventaire.testutil.FakeInventoryRepository
import ma.bam.inventaire.testutil.testArticle
import ma.bam.inventaire.testutil.testSession
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryListViewModelTest {

    private lateinit var repository: FakeInventoryRepository
    private lateinit var viewModel: InventoryListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakeInventoryRepository(
            listOf(
                testArticle(id = 1, quantiteTheorique = 5.0, quantiteReelle = 5.0, ecart = 0.0, ecartValide = true),
                testArticle(id = 2, quantiteTheorique = 5.0, quantiteReelle = 7.0, ecart = 2.0),
                testArticle(id = 3, quantiteTheorique = -1.0),
                testArticle(id = 4, quantiteTheorique = 0.0).copy(sessionId = "s2")
            )
        )
        // Ordre du dépôt (dateCreation DESC) : s3 la plus récente, sans aucun article.
        repository.sessions.value = listOf(testSession("s3"), testSession("s2"), testSession("s1"))
        viewModel = InventoryListViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.collectSessions() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.sessions.collect {} }
    }

    private fun countersOf(sessionId: String) = viewModel.sessions.value.single { it.session.id == sessionId }.counters

    @Test
    fun `compteurs calcules par session, dans l'ordre du depot`() = runTest {
        collectSessions()

        assertEquals(listOf("s3", "s2", "s1"), viewModel.sessions.value.map { it.session.id })
        assertEquals(SessionCounters(3, 2, 1, 1, 1, 0), countersOf("s1"))
        assertEquals(SessionCounters(1, 0, 0, 0, 0, 1), countersOf("s2"))
    }

    @Test
    fun `session sans article affichee avec des compteurs a zero`() = runTest {
        collectSessions()

        assertEquals(SessionCounters.ZERO, countersOf("s3"))
    }

    @Test
    fun `compteurs mis a jour quand le depot emet`() = runTest {
        collectSessions()

        repository.updateQuantity(articleId = 3, quantiteReelle = -1.0, ecartValide = false)
        assertEquals(SessionCounters(3, 3, 1, 1, 1, 0), countersOf("s1"))

        repository.articles.value += testArticle(id = 5).copy(sessionId = "s3")
        assertEquals(SessionCounters(1, 0, 0, 0, 0, 0), countersOf("s3"))

        repository.sessions.value = listOf(testSession("s4")) + repository.sessions.value
        assertEquals(listOf("s4", "s3", "s2", "s1"), viewModel.sessions.value.map { it.session.id })
        assertEquals(SessionCounters.ZERO, countersOf("s4"))
    }
}
