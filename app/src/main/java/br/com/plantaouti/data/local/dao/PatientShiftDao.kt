package br.com.plantaouti.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.plantaouti.data.local.entity.PatientShiftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientShiftDao {
    @Query("SELECT * FROM patient_shifts ORDER BY isEnded ASC, bedNumber ASC, createdAtMillis DESC")
    fun observeAll(): Flow<List<PatientShiftEntity>>

    @Query("SELECT * FROM patient_shifts WHERE id = :shiftId")
    fun observeById(shiftId: Long): Flow<PatientShiftEntity?>

    @Query("SELECT * FROM patient_shifts WHERE id = :shiftId")
    suspend fun getById(shiftId: Long): PatientShiftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shift: PatientShiftEntity): Long

    @Update
    suspend fun update(shift: PatientShiftEntity)

    @Delete
    suspend fun delete(shift: PatientShiftEntity)

    @Query("DELETE FROM patient_shifts")
    suspend fun deleteAll()
}
