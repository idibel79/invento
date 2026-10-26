package ma.bam.inventaire.util

/** Retour (son + vibration) d'un scan ; interface pour pouvoir le simuler dans les tests. */
interface ScanFeedback {
    fun onScanSuccess()

    fun onScanError()
}
