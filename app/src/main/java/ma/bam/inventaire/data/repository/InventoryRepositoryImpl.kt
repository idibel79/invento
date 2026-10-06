package ma.bam.inventaire.data.repository

import kotlinx.coroutines.flow.Flow
import ma.bam.inventaire.data.local.dao.InventorySessionDao
import ma.bam.inventaire.data.local.dao.StockArticleDao
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.InventoryStatus
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.domain.EcartCalculator
import ma.bam.inventaire.util.SessionNumberGenerator
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepositoryImpl @Inject constructor(
    private val sessionDao: InventorySessionDao,
    private val articleDao: StockArticleDao
) : InventoryRepository {

    override fun observeSessions(): Flow<List<InventorySessionEntity>> = sessionDao.observeAll()

    override fun observeSession(sessionId: String): Flow<InventorySessionEntity?> =
        sessionDao.observeById(sessionId)

    override fun observeArticles(sessionId: String): Flow<List<StockArticleEntity>> =
        articleDao.observeBySession(sessionId)

    override suspend fun createSession(
        fileName: String,
        articles: List<ImportedArticle>
    ): InventorySessionEntity {
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
        sessionDao.insert(session)

        articleDao.insertAll(
            articles.map { imported ->
                StockArticleEntity(
                    sessionId = session.id,
                    codeArticle = imported.codeArticle,
                    designation = imported.designation,
                    categorie = imported.categorie,
                    tva = imported.tva,
                    emplacement = imported.emplacement,
                    quantiteTheorique = imported.quantiteTheorique,
                    datePeremption = imported.datePeremption,
                    stockMin = imported.stockMin,
                    stockMax = imported.stockMax,
                    codeBarre1 = imported.codeBarre1,
                    codeBarre2 = imported.codeBarre2,
                    prixUnitaire = imported.prixUnitaire,
                    pph = imported.pph,
                    dateImport = imported.dateImport
                )
            }
        )
        return session
    }

    override suspend fun findArticleByCode(
        sessionId: String,
        code: String
    ): StockArticleEntity? = articleDao.findByCode(sessionId, code)

    override suspend fun recordScan(articleId: Long, quantiteReelle: Double, ecartValide: Boolean) {
        updateQuantity(articleId, quantiteReelle, ecartValide)
    }

    override suspend fun updateQuantity(articleId: Long, quantiteReelle: Double, ecartValide: Boolean) {
        val article = articleDao.getById(articleId) ?: return
        val ecart = EcartCalculator.compute(article.quantiteTheorique, quantiteReelle)
        articleDao.update(
            article.copy(
                quantiteReelle = quantiteReelle,
                ecart = ecart,
                ecartValide = if (EcartCalculator.hasEcart(ecart)) ecartValide else true,
                dateScan = System.currentTimeMillis()
            )
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

    override suspend fun getSessionCounters(sessionId: String): SessionCounters {
        val total = articleDao.countForSession(sessionId)
        val scanned = articleDao.countScannedForSession(sessionId)
        val pending = articleDao.countPendingEcartsForSession(sessionId)
        return SessionCounters(total = total, scanned = scanned, pendingEcarts = pending)
    }
}
