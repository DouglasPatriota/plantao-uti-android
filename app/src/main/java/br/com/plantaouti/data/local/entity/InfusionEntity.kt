package br.com.plantaouti.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "infusions",
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
data class InfusionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val shiftId: Long,
    val drugName: String,
    val unit: String,
    val referenceRangeLabel: String,
    val minDose: Double?,
    val maxDose: Double?,
    val currentDose: String,
    val currentRateMlH: String,
    val startHour: Int,
    val startMinute: Int,
    val durationHours: Int,
    val durationMinutes: Int,
    val predictedEndLabel: String,
    val crossedMidnight: Boolean,
    val endEpochMillis: Long,
    val isActive: Boolean = true,
    val alarmEnabled: Boolean = true,
    val createdAtMillis: Long = System.currentTimeMillis()
)
