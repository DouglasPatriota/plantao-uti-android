package br.com.plantaouti.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.com.plantaouti.data.local.dao.DrugCatalogDao
import br.com.plantaouti.data.local.dao.FluidBalanceDao
import br.com.plantaouti.data.local.dao.InfusionDao
import br.com.plantaouti.data.local.dao.MaterialDao
import br.com.plantaouti.data.local.dao.PatientShiftDao
import br.com.plantaouti.data.local.dao.VitalSignDao
import br.com.plantaouti.data.local.entity.DrugCatalogEntity
import br.com.plantaouti.data.local.entity.FluidBalanceEntity
import br.com.plantaouti.data.local.entity.InfusionEntity
import br.com.plantaouti.data.local.entity.InfusionHistoryEntity
import br.com.plantaouti.data.local.entity.MaterialCatalogEntity
import br.com.plantaouti.data.local.entity.MaterialUsageEntity
import br.com.plantaouti.data.local.entity.PatientShiftEntity
import br.com.plantaouti.data.local.entity.VitalSignEntity

@Database(
    entities = [
        PatientShiftEntity::class,
        VitalSignEntity::class,
        DrugCatalogEntity::class,
        InfusionEntity::class,
        InfusionHistoryEntity::class,
        MaterialCatalogEntity::class,
        MaterialUsageEntity::class,
        FluidBalanceEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PlantaoUtiDatabase : RoomDatabase() {
    abstract fun patientShiftDao(): PatientShiftDao
    abstract fun vitalSignDao(): VitalSignDao
    abstract fun drugCatalogDao(): DrugCatalogDao
    abstract fun infusionDao(): InfusionDao
    abstract fun materialDao(): MaterialDao
    abstract fun fluidBalanceDao(): FluidBalanceDao
}
