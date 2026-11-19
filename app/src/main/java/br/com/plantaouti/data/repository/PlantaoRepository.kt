package br.com.plantaouti.data.repository

import br.com.plantaouti.data.local.DefaultCatalogData
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
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlantaoRepository @Inject constructor(
    private val patientShiftDao: PatientShiftDao,
    private val vitalSignDao: VitalSignDao,
    private val drugCatalogDao: DrugCatalogDao,
    private val infusionDao: InfusionDao,
    private val materialDao: MaterialDao,
    private val fluidBalanceDao: FluidBalanceDao
) {
    /**
     * Garante o carregamento inicial do catálogo de 17 drogas e materiais frequentes.
     */
    suspend fun ensureDefaultCatalogsSeeded() {
        if (drugCatalogDao.count() == 0) {
            drugCatalogDao.insertAll(DefaultCatalogData.defaultDrugs)
        }
        if (materialDao.catalogCount() == 0) {
            materialDao.insertCatalogAll(DefaultCatalogData.defaultMaterials)
        }
    }

    // --- Pacientes / Plantões ---
    fun observeShifts(): Flow<List<PatientShiftEntity>> = patientShiftDao.observeAll()
    fun observeShift(shiftId: Long): Flow<PatientShiftEntity?> = patientShiftDao.observeById(shiftId)
    suspend fun getShift(shiftId: Long): PatientShiftEntity? = patientShiftDao.getById(shiftId)

    /**
     * Cria o plantão do paciente e pré-popula automaticamente todas as linhas horárias
     * de Sinais Vitais e Balanço Hídrico conforme o turno (Diurno 07h–19h / Noturno 19h–07h).
     */
    suspend fun createShiftWithHourlyRows(shift: PatientShiftEntity): Long {
        val shiftId = patientShiftDao.insert(shift)
        val hours = shift.shiftType.hours
        val vitals = hours.mapIndexed { idx, label ->
            VitalSignEntity(shiftId = shiftId, hourOrder = idx, hourLabel = label)
        }
        val fluids = hours.mapIndexed { idx, label ->
            FluidBalanceEntity(shiftId = shiftId, hourOrder = idx, hourLabel = label)
        }
        vitalSignDao.insertAll(vitals)
        fluidBalanceDao.insertAll(fluids)
        return shiftId
    }

    suspend fun updateShift(shift: PatientShiftEntity) = patientShiftDao.update(shift)

    suspend fun endShift(shift: PatientShiftEntity) {
        patientShiftDao.update(
            shift.copy(isEnded = true, endedAtMillis = System.currentTimeMillis())
        )
    }

    suspend fun reopenShift(shift: PatientShiftEntity) {
        patientShiftDao.update(shift.copy(isEnded = false, endedAtMillis = null))
    }

    suspend fun deleteShift(shift: PatientShiftEntity) = patientShiftDao.delete(shift)
    suspend fun deleteAllShiftData() = patientShiftDao.deleteAll()

    // --- Sinais Vitais ---
    fun observeVitals(shiftId: Long): Flow<List<VitalSignEntity>> = vitalSignDao.observeByShift(shiftId)
    suspend fun getVitals(shiftId: Long): List<VitalSignEntity> = vitalSignDao.getByShift(shiftId)
    suspend fun updateVital(vital: VitalSignEntity) {
        vitalSignDao.update(vital.copy(updatedAtMillis = System.currentTimeMillis()))
    }

    // --- Catálogo de Drogas ---
    fun observeDrugCatalog(): Flow<List<DrugCatalogEntity>> = drugCatalogDao.observeAll()
    suspend fun upsertDrug(drug: DrugCatalogEntity) = drugCatalogDao.insert(drug)
    suspend fun deleteDrug(drug: DrugCatalogEntity) = drugCatalogDao.delete(drug)
    suspend fun restoreDefaultDrugCatalog() {
        drugCatalogDao.deleteAll()
        drugCatalogDao.insertAll(DefaultCatalogData.defaultDrugs)
    }

    // --- Infusões Contínuas ---
    fun observeInfusions(shiftId: Long): Flow<List<InfusionEntity>> = infusionDao.observeByShift(shiftId)
    suspend fun getInfusions(shiftId: Long): List<InfusionEntity> = infusionDao.getByShift(shiftId)
    suspend fun getInfusion(infusionId: Long): InfusionEntity? = infusionDao.getById(infusionId)
    suspend fun getActiveFutureInfusions(nowMillis: Long): List<InfusionEntity> =
        infusionDao.getActiveFutureInfusions(nowMillis)
    fun observeInfusionHistory(infusionId: Long): Flow<List<InfusionHistoryEntity>> =
        infusionDao.observeHistory(infusionId)

    /**
     * Insere nova infusão e registra automaticamente o ponto inicial no histórico imutável.
     */
    suspend fun addInfusion(infusion: InfusionEntity, note: String = "Início da infusão"): Long {
        val id = infusionDao.insertInfusion(infusion)
        infusionDao.insertHistory(
            InfusionHistoryEntity(
                infusionId = id,
                dose = infusion.currentDose,
                rateMlH = infusion.currentRateMlH,
                unit = infusion.unit,
                note = note
            )
        )
        return id
    }

    /**
     * Atualiza infusão e SEMPRE acrescenta novo registro no histórico (nunca sobrescreve).
     */
    suspend fun updateInfusionWithHistory(infusion: InfusionEntity, note: String = "Alteração de dose/taxa") {
        infusionDao.updateInfusion(infusion)
        infusionDao.insertHistory(
            InfusionHistoryEntity(
                infusionId = infusion.id,
                dose = infusion.currentDose,
                rateMlH = infusion.currentRateMlH,
                unit = infusion.unit,
                note = note
            )
        )
    }

    suspend fun toggleInfusionActive(infusion: InfusionEntity, active: Boolean) {
        infusionDao.updateInfusion(infusion.copy(isActive = active))
        infusionDao.insertHistory(
            InfusionHistoryEntity(
                infusionId = infusion.id,
                dose = infusion.currentDose,
                rateMlH = infusion.currentRateMlH,
                unit = infusion.unit,
                note = if (active) "Infusão retomada" else "Infusão pausada/encerrada"
            )
        )
    }

    suspend fun deleteInfusion(infusion: InfusionEntity) = infusionDao.deleteInfusion(infusion)

    // --- Materiais ---
    fun observeMaterials(shiftId: Long): Flow<List<MaterialUsageEntity>> = materialDao.observeUsageByShift(shiftId)
    suspend fun getMaterials(shiftId: Long): List<MaterialUsageEntity> = materialDao.getUsageByShift(shiftId)
    fun observeMaterialCatalog(): Flow<List<MaterialCatalogEntity>> = materialDao.observeCatalog()

    suspend fun addOrIncrementMaterial(shiftId: Long, materialName: String, quantity: Int, notes: String) {
        val trimmed = materialName.trim()
        if (trimmed.isEmpty()) return
        materialDao.insertCatalogItem(MaterialCatalogEntity(name = trimmed, isDefault = false))
        materialDao.insertUsage(
            MaterialUsageEntity(
                shiftId = shiftId,
                materialName = trimmed,
                quantity = quantity.coerceAtLeast(1),
                notes = notes.trim()
            )
        )
    }

    suspend fun updateMaterialUsage(usage: MaterialUsageEntity) {
        materialDao.updateUsage(usage.copy(updatedAtMillis = System.currentTimeMillis()))
    }

    suspend fun deleteMaterialUsage(usage: MaterialUsageEntity) = materialDao.deleteUsage(usage)

    // --- Balanço Hídrico / Eliminações ---
    fun observeFluidBalance(shiftId: Long): Flow<List<FluidBalanceEntity>> = fluidBalanceDao.observeByShift(shiftId)
    suspend fun getFluidBalance(shiftId: Long): List<FluidBalanceEntity> = fluidBalanceDao.getByShift(shiftId)
    suspend fun updateFluidBalance(item: FluidBalanceEntity) {
        fluidBalanceDao.update(item.copy(updatedAtMillis = System.currentTimeMillis()))
    }
}
