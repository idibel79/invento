package ma.bam.inventaire.data.importfile

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStream
import javax.inject.Inject

/** Lit le fichier choisi via le ContentResolver (Uri `content://` du sélecteur de documents). */
class ContentResolverImportFileSource @Inject constructor(
    @ApplicationContext private val context: Context
) : ImportFileSource {

    // Métadonnées indisponibles (erreur ou curseur vide) : nom par défaut, taille inconnue.
    override fun describe(fileUri: String): ImportFileInfo =
        runCatching { queryMetadata(Uri.parse(fileUri)) }.getOrNull() ?: ImportFileInfo(DEFAULT_NAME, null)

    private fun queryMetadata(uri: Uri): ImportFileInfo? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            it.takeIf { c -> c.moveToFirst() }?.let { c ->
                val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = c.getColumnIndex(OpenableColumns.SIZE)
                ImportFileInfo(
                    name = (if (nameIndex >= 0) c.getString(nameIndex) else null) ?: DEFAULT_NAME,
                    sizeBytes = sizeIndex.takeIf { index -> index >= 0 && !c.isNull(index) }?.let(c::getLong)
                )
            }
        }
    }

    override fun open(fileUri: String): InputStream? = context.contentResolver.openInputStream(Uri.parse(fileUri))

    private companion object {
        const val DEFAULT_NAME = "inventaire.xlsx"
    }
}
