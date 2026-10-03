package ma.bam.inventaire.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "article_barcode",
    foreignKeys = [
        ForeignKey(
            entity = StockArticleEntity::class,
            parentColumns = ["id"],
            childColumns = ["stockArticleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("stockArticleId"), Index("codeBarre")]
)
data class ArticleBarcodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val stockArticleId: Long,
    val codeBarre: String
)
