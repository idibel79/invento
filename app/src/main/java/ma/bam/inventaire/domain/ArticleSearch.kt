package ma.bam.inventaire.domain

import ma.bam.inventaire.data.local.entity.StockArticleEntity

/**
 * Recherche texte commune à la page produits et à la saisie manuelle du scan : désignation,
 * PID (code article) ou l'un des deux codes-barres contient [query], sans tenir compte de la casse.
 * Une recherche vide ou faite d'espaces correspond à tout article.
 */
fun StockArticleEntity.matchesQuery(query: String): Boolean = query.isBlank() ||
    designation.contains(query, ignoreCase = true) ||
    codeArticle.contains(query, ignoreCase = true) ||
    codeBarre1.contains(query, ignoreCase = true) ||
    codeBarre2.contains(query, ignoreCase = true)
