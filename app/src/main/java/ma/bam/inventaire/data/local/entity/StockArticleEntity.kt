package ma.bam.inventaire.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_article",
    foreignKeys = [
        ForeignKey(
            entity = InventorySessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("codeArticle")]
)
data class StockArticleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val codeArticle: String,
    val reference: String,
    val designation: String,
    val description: String,
    val categorie: String,
    val emplacement: String,
    val quantiteTheorique: Double,
    val unite: String,
    val prixUnitaire: Double,
    val dateImport: Long,
    val quantiteReelle: Double? = null,
    val ecart: Double? = null,
    val ecartValide: Boolean = false,
    val dateScan: Long? = null
)
