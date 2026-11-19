package br.com.plantaouti.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.plantaouti.data.local.entity.DrugCatalogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DrugCatalogDao {
    @Query("SELECT * FROM drug_catalog ORDER BY name ASC")
    fun observeAll(): Flow<List<DrugCatalogEntity>>

    @Query("SELECT COUNT(*) FROM drug_catalog")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(drug: DrugCatalogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(drugs: List<DrugCatalogEntity>)

    @Update
    suspend fun update(drug: DrugCatalogEntity)

    @Delete
    suspend fun delete(drug: DrugCatalogEntity)

    @Query("DELETE FROM drug_catalog")
    suspend fun deleteAll()
}
