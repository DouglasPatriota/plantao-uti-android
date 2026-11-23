package br.com.plantaouti.data.local

import androidx.room.TypeConverter
import br.com.plantaouti.domain.model.ShiftType

class Converters {
    @TypeConverter
    fun fromShiftType(value: ShiftType): String = value.name

    @TypeConverter
    fun toShiftType(value: String): ShiftType = ShiftType.valueOf(value)
}
