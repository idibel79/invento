package ma.bam.inventaire.data.remote

/**
 * Point d'extension pour une future synchronisation avec le système de gestion existant (BAM).
 * Non utilisé aujourd'hui : l'app fonctionne entièrement en local (SQLite via [ma.bam.inventaire.data.repository.InventoryRepository]).
 * Quand une API sera disponible, une implémentation réseau de InventoryRepository pourra
 * s'appuyer sur cette interface (Retrofit ou autre client HTTP) sans impacter l'UI.
 */
interface ApiService {

    suspend fun fetchTheoreticalStock(depotId: String): List<RemoteStockArticle>

    suspend fun pushInventoryResult(session: RemoteInventorySession)
}

data class RemoteStockArticle(
    val codeArticle: String,
    val designation: String,
    val categorie: String,
    val tva: String,
    val emplacement: String,
    val quantiteTheorique: Double,
    val datePeremption: String,
    val stockMin: Double,
    val stockMax: Double,
    val codeBarre1: String,
    val codeBarre2: String,
    val prixUnitaire: Double
)

data class RemoteInventorySession(
    val numero: String,
    val dateCreation: Long,
    val dateFinalisation: Long?,
    val lignes: List<RemoteInventoryLine>
)

data class RemoteInventoryLine(
    val codeArticle: String,
    val quantiteTheorique: Double,
    val quantiteReelle: Double?,
    val ecart: Double?
)
