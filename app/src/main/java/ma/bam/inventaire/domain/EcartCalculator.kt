package ma.bam.inventaire.domain

import ma.bam.inventaire.data.local.entity.StockArticleEntity

object EcartCalculator {

    /**
     * En dessous de ce seuil (en valeur absolue), un écart est considéré nul (arrondis des Double).
     * Seule définition du seuil : la requête SQL des compteurs (StockArticleDao, COUNTER_COLUMNS)
     * l'interpole directement.
     */
    const val SEUIL_ECART_NUL = 1e-9

    /** Écart = quantité réelle constatée - quantité théorique. */
    fun compute(quantiteTheorique: Double, quantiteReelle: Double): Double = quantiteReelle - quantiteTheorique

    fun hasEcart(ecart: Double?): Boolean = ecart != null && kotlin.math.abs(ecart) > SEUIL_ECART_NUL

    /**
     * Écart restant à traiter : les corrigés gardent leur écart d'origine mais sont rangés dans
     * « Corrigés », pas « Écarts ». Même règle que la colonne `ecarts` de SessionCounters.
     */
    fun isEcartNonCorrige(article: StockArticleEntity): Boolean = !article.corrige && hasEcart(article.ecart)

    /** À partir de cet écart (en valeur absolue, -5 ou +5), l'écart s'affiche en rouge ; en dessous, en orange. */
    const val SEUIL_ECART_FORT = 5.0

    /** Écart d'au moins ±[SEUIL_ECART_FORT] : rouge. Un écart non nul plus petit est orange. */
    fun isEcartFort(ecart: Double?): Boolean = ecart != null && kotlin.math.abs(ecart) >= SEUIL_ECART_FORT
}
