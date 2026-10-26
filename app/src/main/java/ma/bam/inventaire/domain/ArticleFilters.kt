package ma.bam.inventaire.domain

import ma.bam.inventaire.data.local.entity.StockArticleEntity

enum class ArticleFilter { TOUS, SCANNES, ECARTS, CORRIGES, NON_SCANNES, NEGATIFS, VIDE }

enum class ArticleSort { DEFAUT, TITRE_AZ, TITRE_ZA, PRIX_ASC, PRIX_DESC, QUANTITE_ASC, QUANTITE_DESC }

/** Filtres de champ de la feuille de filtre ; null = pas de restriction sur ce champ. */
data class FieldFilters(
    val position: String? = null,
    val category: String? = null,
    val priceRange: ClosedFloatingPointRange<Float>? = null
)

/** Fonctions pures de filtrage et de tri de la page produits (testables hors Android). */
object ArticleFilters {

    /** Les corrigés gardent leur écart d'origine mais sont rangés dans « Corrigés », pas « Écarts ». */
    fun isEcartNonCorrige(article: StockArticleEntity): Boolean = !article.corrige && EcartCalculator.hasEcart(article.ecart)

    /** Nombre d'articles correspondant à chaque filtre rapide, sur la liste entière. */
    fun counts(articles: List<StockArticleEntity>): Map<ArticleFilter, Int> = mapOf(
        ArticleFilter.TOUS to articles.size,
        ArticleFilter.SCANNES to articles.count { it.quantiteReelle != null },
        ArticleFilter.ECARTS to articles.count { isEcartNonCorrige(it) },
        ArticleFilter.CORRIGES to articles.count { it.corrige },
        ArticleFilter.NON_SCANNES to articles.count { it.quantiteReelle == null },
        ArticleFilter.NEGATIFS to articles.count { it.quantiteTheorique < 0 },
        ArticleFilter.VIDE to articles.count { it.quantiteTheorique == 0.0 }
    )

    // Logique reprise telle quelle de InventoryDetailViewModel (complexité inchangée).
    @Suppress("CyclomaticComplexMethod")
    fun applyFilters(
        articles: List<StockArticleEntity>,
        filter: ArticleFilter,
        query: String,
        fields: FieldFilters
    ): List<StockArticleEntity> {
        var filtered = when (filter) {
            ArticleFilter.TOUS -> articles
            ArticleFilter.SCANNES -> articles.filter { it.quantiteReelle != null }
            ArticleFilter.ECARTS -> articles.filter { isEcartNonCorrige(it) }
            ArticleFilter.CORRIGES -> articles.filter { it.corrige }
            ArticleFilter.NON_SCANNES -> articles.filter { it.quantiteReelle == null }
            ArticleFilter.NEGATIFS -> articles.filter { it.quantiteTheorique < 0 }
            ArticleFilter.VIDE -> articles.filter { it.quantiteTheorique == 0.0 }
        }
        if (fields.position != null) {
            filtered = filtered.filter { it.emplacement == fields.position }
        }
        if (fields.category != null) {
            filtered = filtered.filter { it.categorie == fields.category }
        }
        if (fields.priceRange != null) {
            filtered = filtered.filter { it.prixUnitaire.toFloat() in fields.priceRange }
        }
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.codeBarre1.contains(query, ignoreCase = true) ||
                    it.codeBarre2.contains(query, ignoreCase = true) ||
                    it.codeArticle.contains(query, ignoreCase = true) ||
                    it.designation.contains(query, ignoreCase = true)
            }
        }
        return filtered
    }

    fun sort(articles: List<StockArticleEntity>, sort: ArticleSort): List<StockArticleEntity> = when (sort) {
        ArticleSort.DEFAUT -> articles
        ArticleSort.TITRE_AZ -> articles.sortedWith(
            compareBy(String.CASE_INSENSITIVE_ORDER) { it.designation.ifBlank { it.codeArticle } }
        )
        ArticleSort.TITRE_ZA -> articles.sortedWith(
            compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.designation.ifBlank { it.codeArticle } }
        )
        ArticleSort.PRIX_ASC -> articles.sortedBy { it.prixUnitaire }
        ArticleSort.PRIX_DESC -> articles.sortedByDescending { it.prixUnitaire }
        ArticleSort.QUANTITE_ASC -> articles.sortedBy { it.quantiteTheorique }
        ArticleSort.QUANTITE_DESC -> articles.sortedByDescending { it.quantiteTheorique }
    }
}
