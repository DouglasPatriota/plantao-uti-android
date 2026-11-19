package br.com.plantaouti.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Gastos com materiais do paciente no plantão — SEM valores monetários.
 */
@Entity(
    tableName = "material_usage",
    foreignKeys = [
        ForeignKey(
            entity = PatientShiftEntity::class,
            parentColumns = ["id"],
            childColumns = ["shiftId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("shiftId")]
)
data class MaterialUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val shiftId: Long,
    val materialName: String,
    val quantity: Int = 1,
    val notes: String = "",
    val updatedAtMillis: Long = System.currentTimeMillis()
)
