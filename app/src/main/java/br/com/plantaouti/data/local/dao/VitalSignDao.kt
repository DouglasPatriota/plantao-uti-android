package br.com.plantaouti.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.plantaouti.data.local.entity.VitalSignEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VitalSignDao {
    @Query("SELECT * FROM vital_signs WHERE shiftId = :shiftId ORDER BY hourOrder ASC")
    fun observeByShift(shiftId: Long): Flow<List<VitalSignEntity>>

    @Query("SELECT * FROM vital_signs WHERE shiftId = :shiftId ORDER BY hourOrder ASC")
    suspend fun getByShift(shiftId: Long): List<VitalSignEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<VitalSignEntity>)

    @Update
    suspend fun update(item: VitalSignEntity)
}
