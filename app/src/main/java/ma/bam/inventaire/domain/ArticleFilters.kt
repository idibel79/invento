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

    /** Vrai si [article] fait partie du filtre rapide [filter]. */
    fun matches(filter: ArticleFilter, article: StockArticleEntity): Boolean = when (filter) {
        ArticleFilter.TOUS -> true
        ArticleFilter.SCANNES -> article.quantiteReelle != null
        ArticleFilter.ECARTS -> EcartCalculator.isEcartNonCorrige(article)
        ArticleFilter.CORRIGES -> article.corrige
        ArticleFilter.NON_SCANNES -> article.quantiteReelle == null
        ArticleFilter.NEGATIFS -> article.quantiteTheorique < 0
        ArticleFilter.VIDE -> article.quantiteTheorique == 0.0
    }

    /** Nombre d'articles correspondant à chaque filtre rapide, sur la liste entière. */
    fun counts(articles: List<StockArticleEntity>): Map<ArticleFilter, Int> =
        ArticleFilter.entries.associateWith { filter -> articles.count { matches(filter, it) } }

    fun applyFilters(
        articles: List<StockArticleEntity>,
        filter: ArticleFilter,
        query: String,
        fields: FieldFilters
    ): List<StockArticleEntity> = articles.filter { article ->
        matches(filter, article) &&
            (fields.position == null || article.emplacement == fields.position) &&
            (fields.category == null || article.categorie == fields.category) &&
            (fields.priceRange == null || article.prixUnitaire.toFloat() in fields.priceRange) &&
            article.matchesQuery(query)
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
