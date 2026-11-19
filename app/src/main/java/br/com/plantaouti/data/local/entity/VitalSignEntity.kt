package br.com.plantaouti.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vital_signs",
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
data class VitalSignEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val shiftId: Long,
    val hourOrder: Int,
    val hourLabel: String, // ex.: "07:00"
    val bloodPressure: String = "", // ex.: "120x80"
    val pam: Int? = null,
    val heartRate: Int? = null,
    val respRate: Int? = null,
    val spo2: Int? = null,
    val temperature: Double? = null,
    val hgt: Int? = null,
    val painScore: Int? = null,
    val notes: String = "",
    val updatedAtMillis: Long = System.currentTimeMillis()
)
