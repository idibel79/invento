package ma.bam.inventaire.ui.importinventory

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ma.bam.inventaire.data.importfile.ImportFileInfo
import ma.bam.inventaire.data.importfile.ImportFileSource
import ma.bam.inventaire.data.repository.ExcelImportExportRepository
import ma.bam.inventaire.testutil.FakeInventoryRepository
import ma.bam.inventaire.testutil.SampleInventoryFixture
import ma.bam.inventaire.util.xlsx.XlsxWriter
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImportViewModelTest {

    /** Faux fichier : [content] renvoie le contenu à ouvrir (null = illisible) ou lève une exception. */
    private class FakeFileSource(
        private val info: ImportFileInfo = ImportFileInfo("stock.xlsx", 1234L),
        private val content: () -> ByteArray?
    ) : ImportFileSource {
        override fun describe(fileUri: String): ImportFileInfo = info

        override fun open(fileUri: String): InputStream? = content()?.let { ByteArrayInputStream(it) }
    }

    private lateinit var repository: FakeInventoryRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakeInventoryRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(source: ImportFileSource) =
        ImportViewModel(source, ExcelImportExportRepository(), repository, UnconfinedTestDispatcher())

    @Test
    fun `import reussi cree la session et renvoie Success`() = runTest {
        val vm = viewModel(FakeFileSource { SampleInventoryFixture.buildSampleWorkbookBytes() })

        vm.importFile("content://test/stock.xlsx")

        val state = vm.state.value
        assertTrue("état : $state", state is ImportUiState.Success)
        state as ImportUiState.Success
        assertEquals(4, state.nbArticles)
        assertEquals(1, state.erreurs.size)
        assertEquals("session-1", state.sessionId)
        assertEquals(listOf("stock.xlsx" to 4), repository.createdSessions)
        assertEquals(ImportFileInfo("stock.xlsx", 1234L), vm.file.value)
    }

    @Test
    fun `fichier sans article valide donne Error`() = runTest {
        val headerOnly = ByteArrayOutputStream().also {
            XlsxWriter.write(it, "in", listOf(listOf("PID", "Produit", "Stock")))
        }.toByteArray()
        val vm = viewModel(FakeFileSource { headerOnly })

        vm.importFile("content://test/vide.xlsx")

        val state = vm.state.value
        assertTrue("état : $state", state is ImportUiState.Error)
        assertTrue((state as ImportUiState.Error).message.startsWith("Aucun article valide"))
        assertTrue(repository.createdSessions.isEmpty())
    }

    @Test
    fun `fichier illisible donne Error`() = runTest {
        val vm = viewModel(FakeFileSource { null })

        vm.importFile("content://test/absent.xlsx")

        assertTrue(vm.state.value is ImportUiState.Error)
    }

    @Test
    fun `exception de lecture donne Error avec le message`() = runTest {
        val vm = viewModel(FakeFileSource { throw IOException("accès refusé") })

        vm.importFile("content://test/protege.xlsx")

        val state = vm.state.value
        assertTrue("état : $state", state is ImportUiState.Error)
        assertEquals("Échec de l'import : accès refusé", (state as ImportUiState.Error).message)
        assertTrue(repository.createdSessions.isEmpty())
    }

    @Test
    fun `reset revient a Idle sans fichier`() = runTest {
        val vm = viewModel(FakeFileSource { null })
        vm.importFile("content://test/absent.xlsx")

        vm.reset()

        assertEquals(ImportUiState.Idle, vm.state.value)
        assertEquals(null, vm.file.value)
    }
}
