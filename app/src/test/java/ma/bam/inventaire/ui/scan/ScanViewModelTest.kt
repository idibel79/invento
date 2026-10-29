package ma.bam.inventaire.ui.scan

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ma.bam.inventaire.data.repository.SessionCounters
import ma.bam.inventaire.testutil.FakeInventoryRepository
import ma.bam.inventaire.testutil.testArticle
import ma.bam.inventaire.util.ScanFeedback
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScanViewModelTest {

    private class FakeFeedback : ScanFeedback {
        var successes = 0
        var errors = 0

        override fun onScanSuccess() {
            successes++
        }

        override fun onScanError() {
            errors++
        }
    }

    private val article =
        testArticle(id = 7, codeArticle = "PID7", codeBarre1 = "6111000000007", quantiteTheorique = 10.0)
    private lateinit var repository: FakeInventoryRepository
    private lateinit var feedback: FakeFeedback
    private lateinit var viewModel: ScanViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakeInventoryRepository(listOf(article))
        feedback = FakeFeedback()
        viewModel = ScanViewModel(
            SavedStateHandle(mapOf("sessionId" to "s1")),
            repository,
            feedback,
            UnconfinedTestDispatcher()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `scan d'un code inconnu ouvre UnknownArticle avec bip d'erreur`() = runTest {
        viewModel.onCodeDetected("0000")

        assertEquals(ScanDialogState.UnknownArticle("0000"), viewModel.uiState.value.dialog)
        assertEquals(1, feedback.errors)
        assertEquals(0, feedback.successes)
    }

    @Test
    fun `scan d'un code connu ouvre QuantityEntry avec bip de succes`() = runTest {
        viewModel.onCodeDetected("6111000000007")

        assertEquals(ScanDialogState.QuantityEntry(article), viewModel.uiState.value.dialog)
        assertEquals(1, feedback.successes)
    }

    @Test
    fun `scan ignore tant qu'un dialog est ouvert`() = runTest {
        viewModel.onCodeDetected("0000")
        viewModel.onCodeDetected("6111000000007")

        assertEquals(ScanDialogState.UnknownArticle("0000"), viewModel.uiState.value.dialog)
        assertEquals(0, feedback.successes)
    }

    @Test
    fun `quantite sans ecart enregistree comme validee et dialog ferme`() = runTest {
        viewModel.onCodeDetected("6111000000007")

        viewModel.confirmQuantity(article, 10.0)

        assertEquals(
            listOf(FakeInventoryRepository.QuantityCall(7, 10.0, ecartValide = true)),
            repository.recordedQuantities
        )
        assertEquals(ScanDialogState.None, viewModel.uiState.value.dialog)
    }

    @Test
    fun `compteurs issus du flux du depot, mis a jour apres chaque enregistrement`() = runTest {
        assertEquals(SessionCounters(1, 0, 0, 0, 0, 0), viewModel.uiState.value.counters)

        viewModel.confirmQuantity(article, 10.0)
        assertEquals(SessionCounters(1, 1, 0, 0, 0, 0), viewModel.uiState.value.counters)

        viewModel.confirmQuantity(article, 12.0)
        viewModel.validateEcart(article, 12.0)
        assertEquals(SessionCounters(1, 1, 1, 0, 0, 0), viewModel.uiState.value.counters)
    }

    @Test
    fun `l'etat du dialog est conserve quand les compteurs changent`() = runTest {
        viewModel.onCodeDetected("6111000000007")

        repository.articles.value = listOf(article, testArticle(id = 8, quantiteTheorique = 0.0))

        assertEquals(ScanDialogState.QuantityEntry(article), viewModel.uiState.value.dialog)
        assertEquals(SessionCounters(2, 0, 0, 0, 0, 1), viewModel.uiState.value.counters)
    }

    @Test
    fun `quantite avec ecart ouvre EcartConfirm sans enregistrer`() = runTest {
        viewModel.onCodeDetected("6111000000007")

        viewModel.confirmQuantity(article, 12.0)

        assertEquals(ScanDialogState.EcartConfirm(article, 12.0, 2.0), viewModel.uiState.value.dialog)
        assertTrue(repository.recordedQuantities.isEmpty())
    }

    @Test
    fun `validation de l'ecart enregistre la quantite`() = runTest {
        viewModel.confirmQuantity(article, 12.0)

        viewModel.validateEcart(article, 12.0)

        assertEquals(
            listOf(FakeInventoryRepository.QuantityCall(7, 12.0, ecartValide = true)),
            repository.recordedQuantities
        )
        assertEquals(ScanDialogState.None, viewModel.uiState.value.dialog)
    }

    @Test
    fun `retour depuis l'ecart rouvre la saisie avec la quantite saisie sans enregistrer`() = runTest {
        viewModel.confirmQuantity(article, 12.0)

        viewModel.backToQuantityEntry(article, 12.0)

        assertEquals(ScanDialogState.QuantityEntry(article, 12.0), viewModel.uiState.value.dialog)
        assertEquals(emptyList<FakeInventoryRepository.QuantityCall>(), repository.recordedQuantities)
    }

    @Test
    fun `correction enregistre la quantite saisie et la remarque`() = runTest {
        viewModel.confirmQuantity(article, 12.0)

        viewModel.saveCorrection(article, 9.0, "recompté")

        assertEquals(
            listOf(FakeInventoryRepository.CorrectionCall(7, 9.0, "recompté")),
            repository.recordedCorrections
        )
        assertEquals(ScanDialogState.None, viewModel.uiState.value.dialog)
    }

    @Test
    fun `saisie manuelle filtre par designation, PID ou code-barres`() = runTest {
        repository.articles.value = listOf(
            article,
            testArticle(id = 8, codeArticle = "PID8", designation = "Doliprane"),
            testArticle(id = 9, codeArticle = "PID9", designation = "Aspirine", codeBarre2 = "999")
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.manualResults.collect {} }

        viewModel.setManualQuery("doli")
        assertEquals(listOf(8L), viewModel.manualResults.value.map { it.id })
        viewModel.setManualQuery("999")
        assertEquals(listOf(9L), viewModel.manualResults.value.map { it.id })
        viewModel.setManualQuery("  ")
        assertTrue(viewModel.manualResults.value.isEmpty())
    }
}
