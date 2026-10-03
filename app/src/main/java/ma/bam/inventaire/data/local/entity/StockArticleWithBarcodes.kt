package ma.bam.inventaire.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class StockArticleWithBarcodes(
    @Embedded
    val article: StockArticleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "stockArticleId"
    )
    val barcodes: List<ArticleBarcodeEntity>
)
