package ma.bam.inventaire.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.data.repository.ExcelImportExportRepository
import ma.bam.inventaire.di.IoDispatcher

/**
 * Écrit l'export dans le dossier `exports/` des fichiers externes de l'app (déclaré dans
 * res/xml/file_paths.xml) et le partage via le FileProvider de l'app.
 */
class XlsxSessionExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val excelRepository: ExcelImportExportRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SessionExporter {

    override suspend fun export(session: InventorySessionEntity, articles: List<StockArticleEntity>): Uri {
        val file = withContext(ioDispatcher) {
            val exportsDir = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
            File(exportsDir, "${session.numero}.xlsx").also { file ->
                FileOutputStream(file).use { out -> excelRepository.exportSessions(out, listOf(session to articles)) }
            }
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
