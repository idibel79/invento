package ma.bam.inventaire.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_session")
data class InventorySessionEntity(
    @PrimaryKey
    val id: String,
    val numero: String,
    val dateCreation: Long,
    val dateFinalisation: Long?,
    val statut: InventoryStatus,
    val nomFichierSource: String,
    /** Verrouillage manuel (swipe + confirmation), indépendant de [statut] : empêche toute
     * modification des quantités tant qu'il n'est pas levé via "Reprendre les modifications". */
    val verrouille: Boolean = false
)
