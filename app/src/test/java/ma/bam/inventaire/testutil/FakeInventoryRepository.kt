package ma.bam.inventaire.testutil

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.data.repository.ImportedArticle
import ma.bam.inventaire.data.repository.InventoryRepository
import ma.bam.inventaire.data.repository.SessionCounters
import ma.bam.inventaire.data.repository.SessionCountersRow
import ma.bam.inventaire.domain.EcartCalculator

/** Faux dépôt en mémoire : articles fournis par le test, appels d'écriture enregistrés. */
class FakeInventoryRepository(articles: List<StockArticleEntity> = emptyList()) : InventoryRepository {

    data class QuantityCall(val articleId: Long, val quantiteReelle: Double, val ecartValide: Boolean)

    val articles = MutableStateFlow(articles)
    val recordedQuantities = mutableListOf<QuantityCall>()
    val finalizedSessions = mutableListOf<String>()
    val sessions = MutableStateFlow<List<InventorySessionEntity>>(emptyList())
    val session = MutableStateFlow<InventorySessionEntity?>(null)

    /** Sessions créées par [createSession] : nom du fichier source et nombre d'articles. */
    val createdSessions = mutableListOf<Pair<String, Int>>()

    override fun observeSessions(): Flow<List<InventorySessionEntity>> = sessions

    override fun observeSession(sessionId: String): Flow<InventorySessionEntity?> {
        return session.map { it?.takeIf { s -> s.id == sessionId } }
    }

    override fun observeArticles(sessionId: String): Flow<List<StockArticleEntity>> =
        articles.map { list -> list.filter { it.sessionId == sessionId } }

    override suspend fun createSession(fileName: String, articles: List<ImportedArticle>): InventorySessionEntity {
        createdSessions += fileName to articles.size
        return testSession(id = "session-${createdSessions.size}", nomFichierSource = fileName)
    }

    override suspend fun addArticle(sessionId: String, article: ImportedArticle) = throw UnsupportedOperationException()

    override suspend fun findArticleByCode(sessionId: String, code: String): StockArticleEntity? = articles.value.firstOrNull {
        it.sessionId == sessionId && (it.codeBarre1 == code || it.codeBarre2 == code || it.codeArticle == code)
    }

    override suspend fun updateQuantity(articleId: Long, quantiteReelle: Double, ecartValide: Boolean) {
        recordedQuantities += QuantityCall(articleId, quantiteReelle, ecartValide)
        articles.value = articles.value.map { article ->
            if (article.id != articleId) return@map article
            val ecart = EcartCalculator.compute(article.quantiteTheorique, quantiteReelle)
            article.copy(
                quantiteReelle = quantiteReelle,
                ecart = ecart,
                ecartValide = ecartValide || !EcartCalculator.hasEcart(ecart)
            )
        }
    }

    override suspend fun recordCorrection(articleId: Long, quantiteReelle: Double, quantiteSysteme: Double, remarque: String) = Unit

    override suspend fun finalizeSession(sessionId: String) {
        finalizedSessions += sessionId
    }

    override suspend fun closeSession(sessionId: String) = Unit

    override suspend fun reopenSession(sessionId: String) = Unit

    override suspend fun deleteSession(sessionId: String) = Unit

    // Compteurs recalculés depuis [articles] (mêmes règles que la requête SQL), réémis à chaque modification.
    override fun observeSessionCounters(sessionId: String): Flow<SessionCounters> =
        articles.map { list -> countersOf(list.filter { it.sessionId == sessionId }) }

    override fun observeCountersBySession(): Flow<List<SessionCountersRow>> {
        return articles.map { list ->
            list.groupBy { it.sessionId }.map { (sessionId, group) -> SessionCountersRow(sessionId, countersOf(group)) }
        }
    }

    private fun countersOf(articles: List<StockArticleEntity>) = SessionCounters(
        total = articles.size,
        scanned = articles.count { it.quantiteReelle != null },
        ecarts = articles.count { EcartCalculator.isEcartNonCorrige(it) },
        pendingEcarts = articles.count { EcartCalculator.hasEcart(it.ecart) && !it.ecartValide },
        negativeStock = articles.count { it.quantiteTheorique < 0 },
        emptyStock = articles.count { it.quantiteTheorique == 0.0 }
    )
}
