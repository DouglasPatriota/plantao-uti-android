package br.com.plantaouti.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Balanço hídrico / eliminações por hora — registra vias de diurese (SVD, Fralda, Vaso, Papagaio)
 * e evacuações (quantidade + aspecto), SEM volume em mL.
 */
@Entity(
    tableName = "fluid_balance",
    foreignKeys = [
        ForeignKey(
            entity = PatientShiftEntity::class,
            parentColumns = ["id"],
            childColumns = ["shiftId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["shiftId", "hourOrder"], unique = true)]
)
data class FluidBalanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val shiftId: Long,
    val hourOrder: Int,
    val hourLabel: String,
    val diuresisSvd: Boolean = false,
    val diuresisFralda: Boolean = false,
    val diuresisVaso: Boolean = false,
    val diuresisPapagaio: Boolean = false,
    val stoolCount: Int = 0,
    val stoolAspect: String = "",
    val updatedAtMillis: Long = System.currentTimeMillis()
)
