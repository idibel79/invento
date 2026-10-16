package ma.bam.inventaire.util

import java.text.SimpleDateFormat
import java.util.Locale

object SessionNumberGenerator {

    /** Numéro unique lisible, ex: INV-20261002-014. [sequence] est le rang de la session du jour. */
    fun generate(timestampMillis: Long, sequence: Int): String {
        val datePart = SimpleDateFormat("yyyyMMdd", Locale.FRANCE).format(timestampMillis)
        return "INV-%s-%03d".format(datePart, sequence)
    }
}
