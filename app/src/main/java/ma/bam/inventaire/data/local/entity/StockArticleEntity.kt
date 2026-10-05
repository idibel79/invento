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
    indices = [
        Index("sessionId"),
        Index("codeArticle"),
        Index("codeBarre1"),
        Index("codeBarre2")
    ]
)
data class StockArticleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    /** PID dans le fichier Sobrus. */
    val codeArticle: String,
    /** Produit. */
    val designation: String,
    /** Catégorie (texte libre incluant le taux de marge, ex: "Médicament (33.930%)"). */
    val categorie: String,
    /** TVA (texte libre, ex: "Exonéré (0.00%)" ou "TVA (20.00%)"). */
    val tva: String,
    /** Zone. */
    val emplacement: String,
    /** Stock. */
    val quantiteTheorique: Double,
    /** Date de péremption, telle quelle (texte brut, peut être vide). */
    val datePeremption: String,
    val stockMin: Double,
    val stockMax: Double,
    /** Code barre 1. */
    val codeBarre1: String,
    /** Code barre 2 (optionnel, peut être vide). */
    val codeBarre2: String,
    /** PPV (prix public de vente), utilisé comme prix unitaire dans l'app. */
    val prixUnitaire: Double,
    val dateImport: Long,
    val quantiteReelle: Double? = null,
    val ecart: Double? = null,
    val ecartValide: Boolean = false,
    val dateScan: Long? = null
)
