package ma.bam.inventaire.domain

object EcartCalculator {

    /** Écart = quantité réelle constatée - quantité théorique. */
    fun compute(quantiteTheorique: Double, quantiteReelle: Double): Double =
        quantiteReelle - quantiteTheorique

    fun hasEcart(ecart: Double?): Boolean =
        ecart != null && kotlin.math.abs(ecart) > 1e-9
}
