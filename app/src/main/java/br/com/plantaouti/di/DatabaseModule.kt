package br.com.plantaouti.di

import android.content.Context
import androidx.room.Room
import br.com.plantaouti.data.local.PlantaoUtiDatabase
import br.com.plantaouti.data.local.dao.DrugCatalogDao
import br.com.plantaouti.data.local.dao.FluidBalanceDao
import br.com.plantaouti.data.local.dao.InfusionDao
import br.com.plantaouti.data.local.dao.MaterialDao
import br.com.plantaouti.data.local.dao.PatientShiftDao
import br.com.plantaouti.data.local.dao.VitalSignDao
import br.com.plantaouti.security.DatabasePassphraseProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        passphraseProvider: DatabasePassphraseProvider
    ): PlantaoUtiDatabase {
        // Carrega biblioteca nativa SQLCipher antes de abrir o banco criptografado
        System.loadLibrary("sqlcipher")
        val passphrase = passphraseProvider.getOrCreatePassphrase()
        val factory = SupportOpenHelperFactory(passphrase)

        return Room.databaseBuilder(
            context,
            PlantaoUtiDatabase::class.java,
            "plantao_uti_encrypted.db"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun providePatientShiftDao(db: PlantaoUtiDatabase): PatientShiftDao = db.patientShiftDao()
    @Provides fun provideVitalSignDao(db: PlantaoUtiDatabase): VitalSignDao = db.vitalSignDao()
    @Provides fun provideDrugCatalogDao(db: PlantaoUtiDatabase): DrugCatalogDao = db.drugCatalogDao()
    @Provides fun provideInfusionDao(db: PlantaoUtiDatabase): InfusionDao = db.infusionDao()
    @Provides fun provideMaterialDao(db: PlantaoUtiDatabase): MaterialDao = db.materialDao()
    @Provides fun provideFluidBalanceDao(db: PlantaoUtiDatabase): FluidBalanceDao = db.fluidBalanceDao()
}
