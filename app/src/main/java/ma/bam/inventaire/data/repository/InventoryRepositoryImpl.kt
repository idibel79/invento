package ma.bam.inventaire.data.repository

import kotlinx.coroutines.flow.Flow
import ma.bam.inventaire.data.local.dao.ArticleBarcodeDao
import ma.bam.inventaire.data.local.dao.InventorySessionDao
import ma.bam.inventaire.data.local.dao.StockArticleDao
import ma.bam.inventaire.data.local.entity.ArticleBarcodeEntity
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.InventoryStatus
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.data.local.entity.StockArticleWithBarcodes
import ma.bam.inventaire.domain.EcartCalculator
import ma.bam.inventaire.util.SessionNumberGenerator
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepositoryImpl @Inject constructor(
    private val sessionDao: InventorySessionDao,
    private val articleDao: StockArticleDao,
    private val barcodeDao: ArticleBarcodeDao
) : InventoryRepository {

    override fun observeSessions(): Flow<List<InventorySessionEntity>> = sessionDao.observeAll()

    override fun observeSession(sessionId: String): Flow<InventorySessionEntity?> =
        sessionDao.observeById(sessionId)

    override fun observeArticles(sessionId: String): Flow<List<StockArticleWithBarcodes>> =
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

        articles.forEach { imported ->
            val articleId = articleDao.insert(
                StockArticleEntity(
                    sessionId = session.id,
                    codeArticle = imported.codeArticle,
                    reference = imported.reference,
                    designation = imported.designation,
                    description = imported.description,
                    categorie = imported.categorie,
                    emplacement = imported.emplacement,
                    quantiteTheorique = imported.quantiteTheorique,
                    unite = imported.unite,
                    prixUnitaire = imported.prixUnitaire,
                    dateImport = imported.dateImport
                )
            )
            if (imported.codesBarres.isNotEmpty()) {
                barcodeDao.insertAll(
                    imported.codesBarres.map { codeBarre ->
                        ArticleBarcodeEntity(stockArticleId = articleId, codeBarre = codeBarre)
                    }
                )
            }
        }
        return session
    }

    override suspend fun findArticleByBarcode(
        sessionId: String,
        codeBarre: String
    ): StockArticleWithBarcodes? = articleDao.findByBarcode(sessionId, codeBarre)

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
