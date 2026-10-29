package ma.bam.inventaire.data.repository

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import ma.bam.inventaire.data.local.dao.InventorySessionDao
import ma.bam.inventaire.data.local.dao.StockArticleDao
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.InventoryStatus
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.domain.EcartCalculator
import ma.bam.inventaire.util.SessionNumberGenerator

@Singleton
class InventoryRepositoryImpl @Inject constructor(
    private val sessionDao: InventorySessionDao,
    private val articleDao: StockArticleDao
) : InventoryRepository {

    override fun observeSessions(): Flow<List<InventorySessionEntity>> = sessionDao.observeAll()

    override fun observeSession(sessionId: String): Flow<InventorySessionEntity?> = sessionDao.observeById(sessionId)

    override fun observeArticles(sessionId: String): Flow<List<StockArticleEntity>> = articleDao.observeBySession(sessionId)

    override suspend fun createSession(fileName: String, articles: List<ImportedArticle>): InventorySessionEntity {
        val now = System.currentTimeMillis()
        val sequence = sessionDao.count() + 1
        val session = InventorySessionEntity(
            id = UUID.randomUUID().toString(),
            numero = SessionNumberGenerator.generate(now, sequence),
            dateCreation = now,
            dateFinalisation = null,
            statut = InventoryStatus.EN_COURS,
            nomFichierSource = fileName
        )
        sessionDao.insertWithArticles(session, articles.map { it.toEntity(session.id) })
        return session
    }

    override suspend fun addArticle(sessionId: String, article: ImportedArticle) {
        articleDao.insert(article.toEntity(sessionId))
    }

    private fun ImportedArticle.toEntity(sessionId: String): StockArticleEntity {
        val ecart = quantiteReelle?.let { EcartCalculator.compute(quantiteTheorique, it) }
        return StockArticleEntity(
            sessionId = sessionId,
            codeArticle = codeArticle,
            designation = designation,
            categorie = categorie,
            tva = tva,
            emplacement = emplacement,
            quantiteTheorique = quantiteTheorique,
            datePeremption = datePeremption,
            stockMin = stockMin,
            stockMax = stockMax,
            codeBarre1 = codeBarre1,
            codeBarre2 = codeBarre2,
            prixUnitaire = prixUnitaire,
            pph = pph,
            dateImport = dateImport,
            quantiteReelle = quantiteReelle,
            ecart = ecart,
            ecartValide = if (ecart != null) !EcartCalculator.hasEcart(ecart) else false,
            dateScan = if (quantiteReelle != null) dateImport else null
        )
    }

    override suspend fun findArticleByCode(sessionId: String, code: String): StockArticleEntity? = articleDao.findByCode(sessionId, code)

    override suspend fun recordCorrection(articleId: Long, quantiteSaisie: Double, remarque: String) {
        val article = articleDao.getById(articleId) ?: return
        // La quantité comptée prend la quantité système, qui reste celle du fichier importé.
        // L'écart d'origine (saisie − système) reste affiché sur la carte, rangée dans « Corrigés ».
        articleDao.update(
            article.copy(
                quantiteReelle = article.quantiteTheorique,
                ecart = EcartCalculator.compute(article.quantiteTheorique, quantiteSaisie),
                ecartValide = true,
                dateScan = System.currentTimeMillis(),
                corrige = true,
                remarque = remarque.trim().ifEmpty { null }
            )
        )
    }

    override suspend fun updateQuantity(articleId: Long, quantiteReelle: Double, ecartValide: Boolean) {
        val article = articleDao.getById(articleId) ?: return
        articleDao.update(withQuantity(article, quantiteReelle, ecartValide))
    }

    private fun withQuantity(article: StockArticleEntity, quantiteReelle: Double, ecartValide: Boolean): StockArticleEntity {
        val ecart = EcartCalculator.compute(article.quantiteTheorique, quantiteReelle)
        return article.copy(
            quantiteReelle = quantiteReelle,
            ecart = ecart,
            ecartValide = if (EcartCalculator.hasEcart(ecart)) ecartValide else true,
            dateScan = System.currentTimeMillis()
        )
    }

    override suspend fun finalizeSession(sessionId: String) {
        val session = sessionDao.getById(sessionId) ?: return
        sessionDao.update(
            session.copy(
                statut = InventoryStatus.FINALISE,
                dateFinalisation = System.currentTimeMillis()
            )
        )
    }

    override suspend fun closeSession(sessionId: String) {
        val session = sessionDao.getById(sessionId) ?: return
        sessionDao.update(session.copy(verrouille = true))
    }

    override suspend fun reopenSession(sessionId: String) {
        val session = sessionDao.getById(sessionId) ?: return
        sessionDao.update(session.copy(verrouille = false))
    }

    override suspend fun deleteSession(sessionId: String) {
        sessionDao.deleteById(sessionId)
    }

    override fun observeSessionCounters(sessionId: String): Flow<SessionCounters> {
        return articleDao.observeCountersForSession(sessionId)
    }

    override fun observeCountersBySession(): Flow<List<SessionCountersRow>> = articleDao.observeCountersBySession()
}
