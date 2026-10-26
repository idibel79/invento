package ma.bam.inventaire.domain

import ma.bam.inventaire.data.local.entity.StockArticleEntity

/**
 * Agrégats de la page produits sur la session entière (indépendants des filtres et de la
 * recherche) : compteurs des filtres rapides, listes et compteurs de zones / catégories,
 * TVA disponibles et bornes de prix (PPV) pour le slider.
 */
data class ArticleStats(
    val filterCounts: Map<ArticleFilter, Int> = emptyMap(),
    /** Zones non vides, triées. */
    val positions: List<String> = emptyList(),
    val positionCounts: Map<String, Int> = emptyMap(),
    /** Catégories non vides, triées. */
    val categories: List<String> = emptyList(),
    val categoryCounts: Map<String, Int> = emptyMap(),
    /** TVA non vides, triées. */
    val tvas: List<String> = emptyList(),
    val priceBounds: ClosedFloatingPointRange<Float> = 0f..0f
) {
    companion object {
        /** Calcule tous les agrégats en une seule passe sur [articles]. */
        fun of(articles: List<StockArticleEntity>): ArticleStats {
            val filterCounts = IntArray(ArticleFilter.entries.size)
            val positionCounts = HashMap<String, Int>()
            val categoryCounts = HashMap<String, Int>()
            val tvas = HashSet<String>()
            var minPrice = Float.POSITIVE_INFINITY
            var maxPrice = Float.NEGATIVE_INFINITY
            for (article in articles) {
                ArticleFilter.entries.forEach { filter ->
                    if (ArticleFilters.matches(filter, article)) filterCounts[filter.ordinal]++
                }
                if (article.emplacement.isNotBlank()) positionCounts.merge(article.emplacement, 1, Int::plus)
                if (article.categorie.isNotBlank()) categoryCounts.merge(article.categorie, 1, Int::plus)
                if (article.tva.isNotBlank()) tvas += article.tva
                val price = article.prixUnitaire.toFloat()
                if (price < minPrice) minPrice = price
                if (price > maxPrice) maxPrice = price
            }
            return ArticleStats(
                filterCounts = ArticleFilter.entries.associateWith { filterCounts[it.ordinal] },
                positions = positionCounts.keys.sorted(),
                positionCounts = positionCounts,
                categories = categoryCounts.keys.sorted(),
                categoryCounts = categoryCounts,
                tvas = tvas.sorted(),
                priceBounds = if (articles.isEmpty()) 0f..0f else minPrice..maxPrice
            )
        }
    }
}
