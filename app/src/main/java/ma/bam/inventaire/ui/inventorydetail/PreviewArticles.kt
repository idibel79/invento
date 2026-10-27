package ma.bam.inventaire.ui.inventorydetail

import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.InventoryStatus
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.domain.ArticleStats

/** Données d'exemple des aperçus (@Preview) : une carte par état d'article. */
internal object PreviewArticles {
    private fun article(id: Long, designation: String, theorique: Double, reelle: Double? = null) = StockArticleEntity(
        id = id,
        sessionId = "preview",
        codeArticle = "PID-$id",
        designation = designation,
        categorie = "Médicament (33.930%)",
        tva = "Exonéré (0.00%)",
        emplacement = "Rayon A - Étagère 2",
        quantiteTheorique = theorique,
        datePeremption = "",
        stockMin = 0.0,
        stockMax = 0.0,
        codeBarre1 = "611100000000$id",
        codeBarre2 = "",
        prixUnitaire = 24.5,
        pph = 18.0,
        dateImport = 0L,
        quantiteReelle = reelle,
        ecart = reelle?.let { it - theorique }
    )

    val nonCompte = article(id = 1, designation = "Doliprane 1000 mg comprimés", theorique = 12.0)
    val conforme = article(id = 2, designation = "Smecta 3 g poudre orale", theorique = 8.0, reelle = 8.0)
    val ecartFort = article(id = 3, designation = "Spasfon lyoc 80 mg", theorique = 20.0, reelle = 11.0)
    val ecartFaible = article(id = 4, designation = "Gaviscon suspension buvable", theorique = 6.0, reelle = 4.0)
    val corrige = article(id = 5, designation = "Vitamine C 500 mg", theorique = 10.0, reelle = 7.0)
        .copy(corrige = true, remarque = "Boîtes abîmées retirées du rayon.")

    val all = listOf(nonCompte, conforme, ecartFort, ecartFaible, corrige)

    val session = InventorySessionEntity(
        id = "preview",
        numero = "INV-2026-0042",
        dateCreation = 0L,
        dateFinalisation = null,
        statut = InventoryStatus.EN_COURS,
        nomFichierSource = "stock.xlsx"
    )

    val uiState = InventoryDetailUiState(session = session, articles = all, stats = ArticleStats.of(all))
}
