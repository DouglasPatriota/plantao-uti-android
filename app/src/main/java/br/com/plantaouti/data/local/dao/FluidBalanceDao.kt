package br.com.plantaouti.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.plantaouti.data.local.entity.FluidBalanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FluidBalanceDao {
    @Query("SELECT * FROM fluid_balance WHERE shiftId = :shiftId ORDER BY hourOrder ASC")
    fun observeByShift(shiftId: Long): Flow<List<FluidBalanceEntity>>

    @Query("SELECT * FROM fluid_balance WHERE shiftId = :shiftId ORDER BY hourOrder ASC")
    suspend fun getByShift(shiftId: Long): List<FluidBalanceEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<FluidBalanceEntity>)

    @Update
    suspend fun update(item: FluidBalanceEntity)
}
