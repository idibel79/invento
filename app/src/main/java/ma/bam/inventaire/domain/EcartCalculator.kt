package ma.bam.inventaire.domain

object EcartCalculator {

    /** Écart = quantité réelle constatée - quantité théorique. */
    fun compute(quantiteTheorique: Double, quantiteReelle: Double): Double =
        quantiteReelle - quantiteTheorique

    fun hasEcart(ecart: Double?): Boolean =
        ecart != null && kotlin.math.abs(ecart) > 1e-9

    /** À partir de cet écart (en valeur absolue, -5 ou +5), l'écart s'affiche en rouge ; en dessous, en orange. */
    const val SEUIL_ECART_FORT = 5.0

    /** Écart d'au moins ±[SEUIL_ECART_FORT] : rouge. Un écart non nul plus petit est orange. */
    fun isEcartFort(ecart: Double?): Boolean =
        ecart != null && kotlin.math.abs(ecart) >= SEUIL_ECART_FORT
}
