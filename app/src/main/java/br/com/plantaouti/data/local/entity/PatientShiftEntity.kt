package br.com.plantaouti.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.com.plantaouti.domain.model.ShiftType

@Entity(tableName = "patient_shifts")
data class PatientShiftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val patientName: String,
    val bedNumber: String,
    val weightKg: Double,
    val shiftDate: String, // formato dd/MM/yyyy
    val shiftType: ShiftType,
    val technicianName: String,
    val isEnded: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val endedAtMillis: Long? = null
)
