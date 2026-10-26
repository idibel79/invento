package ma.bam.inventaire.ui.scan

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        viewModel = ScanViewModel(SavedStateHandle(mapOf("sessionId" to "s1")), repository, feedback)
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
        repository.counters = SessionCounters(1, 1, 0, 0, 0, 0)
        viewModel.onCodeDetected("6111000000007")

        viewModel.confirmQuantity(article, 10.0)

        assertEquals(listOf(FakeInventoryRepository.ScanCall(7, 10.0, ecartValide = true)), repository.recordedScans)
        assertEquals(ScanDialogState.None, viewModel.uiState.value.dialog)
        assertEquals(repository.counters, viewModel.uiState.value.counters)
    }

    @Test
    fun `quantite avec ecart ouvre EcartConfirm sans enregistrer`() = runTest {
        viewModel.onCodeDetected("6111000000007")

        viewModel.confirmQuantity(article, 12.0)

        assertEquals(ScanDialogState.EcartConfirm(article, 12.0, 2.0), viewModel.uiState.value.dialog)
        assertTrue(repository.recordedScans.isEmpty())
    }

    @Test
    fun `validation de l'ecart enregistre la quantite`() = runTest {
        viewModel.confirmQuantity(article, 12.0)

        viewModel.validateEcart(article, 12.0)

        assertEquals(listOf(FakeInventoryRepository.ScanCall(7, 12.0, ecartValide = true)), repository.recordedScans)
        assertEquals(ScanDialogState.None, viewModel.uiState.value.dialog)
    }
}
