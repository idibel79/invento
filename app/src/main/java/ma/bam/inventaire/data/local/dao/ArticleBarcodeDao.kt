package ma.bam.inventaire.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import ma.bam.inventaire.data.local.entity.ArticleBarcodeEntity

@Dao
interface ArticleBarcodeDao {

    @Insert
    suspend fun insert(barcode: ArticleBarcodeEntity)

    @Insert
    suspend fun insertAll(barcodes: List<ArticleBarcodeEntity>)
}
