package ma.bam.inventaire.testutil

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.data.repository.ImportedArticle
import ma.bam.inventaire.data.repository.InventoryRepository
import ma.bam.inventaire.data.repository.SessionCounters

/** Faux dépôt en mémoire : articles fournis par le test, appels d'écriture enregistrés. */
class FakeInventoryRepository(articles: List<StockArticleEntity> = emptyList()) : InventoryRepository {

    data class ScanCall(val articleId: Long, val quantiteReelle: Double, val ecartValide: Boolean)

    val articles = MutableStateFlow(articles)
    val recordedScans = mutableListOf<ScanCall>()
    val finalizedSessions = mutableListOf<String>()
    var counters = SessionCounters(0, 0, 0, 0, 0, 0)

    override fun observeSessions(): Flow<List<InventorySessionEntity>> = flowOf(emptyList())

    override fun observeSession(sessionId: String): Flow<InventorySessionEntity?> = flowOf(null)

    override fun observeArticles(sessionId: String): Flow<List<StockArticleEntity>> =
        articles.map { list -> list.filter { it.sessionId == sessionId } }

    override suspend fun createSession(fileName: String, articles: List<ImportedArticle>): InventorySessionEntity =
        throw UnsupportedOperationException()

    override suspend fun addArticle(sessionId: String, article: ImportedArticle) = throw UnsupportedOperationException()

    override suspend fun findArticleByCode(sessionId: String, code: String): StockArticleEntity? = articles.value.firstOrNull {
        it.sessionId == sessionId && (it.codeBarre1 == code || it.codeBarre2 == code || it.codeArticle == code)
    }

    override suspend fun recordScan(articleId: Long, quantiteReelle: Double, ecartValide: Boolean) {
        recordedScans += ScanCall(articleId, quantiteReelle, ecartValide)
    }

    override suspend fun recordCorrection(articleId: Long, quantiteReelle: Double, quantiteSysteme: Double, remarque: String) = Unit

    override suspend fun updateQuantity(articleId: Long, quantiteReelle: Double, ecartValide: Boolean) = Unit

    override suspend fun finalizeSession(sessionId: String) {
        finalizedSessions += sessionId
    }

    override suspend fun closeSession(sessionId: String) = Unit

    override suspend fun reopenSession(sessionId: String) = Unit

    override suspend fun deleteSession(sessionId: String) = Unit

    override suspend fun getSessionCounters(sessionId: String): SessionCounters = counters
}
