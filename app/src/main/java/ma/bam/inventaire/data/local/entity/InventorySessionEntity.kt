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
    val nomFichierSource: String
)
