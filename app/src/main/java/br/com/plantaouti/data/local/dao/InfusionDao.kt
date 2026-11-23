package br.com.plantaouti.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.plantaouti.data.local.entity.InfusionEntity
import br.com.plantaouti.data.local.entity.InfusionHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InfusionDao {
    @Query("SELECT * FROM infusions WHERE shiftId = :shiftId ORDER BY isActive DESC, endEpochMillis ASC")
    fun observeByShift(shiftId: Long): Flow<List<InfusionEntity>>

    @Query("SELECT * FROM infusions WHERE shiftId = :shiftId ORDER BY isActive DESC, endEpochMillis ASC")
    suspend fun getByShift(shiftId: Long): List<InfusionEntity>

    @Query("SELECT * FROM infusions WHERE id = :infusionId")
    suspend fun getById(infusionId: Long): InfusionEntity?

    @Query("SELECT * FROM infusions WHERE isActive = 1 AND alarmEnabled = 1 AND endEpochMillis > :nowMillis")
    suspend fun getActiveFutureInfusions(nowMillis: Long): List<InfusionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInfusion(infusion: InfusionEntity): Long

    @Update
    suspend fun updateInfusion(infusion: InfusionEntity)

    @Delete
    suspend fun deleteInfusion(infusion: InfusionEntity)

    @Query("SELECT * FROM infusion_history WHERE infusionId = :infusionId ORDER BY recordedAtMillis DESC")
    fun observeHistory(infusionId: Long): Flow<List<InfusionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertHistory(entry: InfusionHistoryEntity): Long
}
