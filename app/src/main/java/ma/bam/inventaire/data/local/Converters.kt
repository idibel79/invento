package ma.bam.inventaire.data.local

import androidx.room.TypeConverter
import ma.bam.inventaire.data.local.entity.InventoryStatus

class Converters {

    @TypeConverter
    fun fromStatus(status: InventoryStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): InventoryStatus = InventoryStatus.valueOf(value)
}
