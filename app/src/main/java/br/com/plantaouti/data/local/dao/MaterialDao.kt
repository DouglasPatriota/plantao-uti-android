package br.com.plantaouti.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.plantaouti.data.local.entity.MaterialCatalogEntity
import br.com.plantaouti.data.local.entity.MaterialUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    @Query("SELECT * FROM material_usage WHERE shiftId = :shiftId ORDER BY updatedAtMillis DESC")
    fun observeUsageByShift(shiftId: Long): Flow<List<MaterialUsageEntity>>

    @Query("SELECT * FROM material_usage WHERE shiftId = :shiftId ORDER BY materialName ASC")
    suspend fun getUsageByShift(shiftId: Long): List<MaterialUsageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsage(usage: MaterialUsageEntity): Long

    @Update
    suspend fun updateUsage(usage: MaterialUsageEntity)

    @Delete
    suspend fun deleteUsage(usage: MaterialUsageEntity)

    @Query("SELECT * FROM material_catalog ORDER BY name ASC")
    fun observeCatalog(): Flow<List<MaterialCatalogEntity>>

    @Query("SELECT COUNT(*) FROM material_catalog")
    suspend fun catalogCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCatalogItem(item: MaterialCatalogEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCatalogAll(items: List<MaterialCatalogEntity>)
}
