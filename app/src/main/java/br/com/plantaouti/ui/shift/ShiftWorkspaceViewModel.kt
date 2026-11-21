package br.com.plantaouti.ui.shift

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.plantaouti.alarm.InfusionAlarmScheduler
import br.com.plantaouti.data.datastore.AppSettings
import br.com.plantaouti.data.datastore.AppThemeMode
import br.com.plantaouti.data.datastore.SettingsDataStore
import br.com.plantaouti.data.local.entity.DrugCatalogEntity
import br.com.plantaouti.data.local.entity.FluidBalanceEntity
import br.com.plantaouti.data.local.entity.InfusionEntity
import br.com.plantaouti.data.local.entity.InfusionHistoryEntity
import br.com.plantaouti.data.local.entity.MaterialCatalogEntity
import br.com.plantaouti.data.local.entity.MaterialUsageEntity
import br.com.plantaouti.data.local.entity.PatientShiftEntity
import br.com.plantaouti.data.local.entity.VitalSignEntity
import br.com.plantaouti.data.repository.PlantaoRepository
import br.com.plantaouti.domain.calculator.InfusionTimeCalculator
import br.com.plantaouti.domain.calculator.VitalSignCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShiftWorkspaceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PlantaoRepository,
    private val settingsDataStore: SettingsDataStore,
    private val alarmScheduler: InfusionAlarmScheduler
) : ViewModel() {

    val shiftId: Long = checkNotNull(savedStateHandle.get<Long>("shiftId"))

    val shift: StateFlow<PatientShiftEntity?> = repository.observeShift(shiftId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val vitals: StateFlow<List<VitalSignEntity>> = repository.observeVitals(shiftId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val infusions: StateFlow<List<InfusionEntity>> = repository.observeInfusions(shiftId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val drugCatalog: StateFlow<List<DrugCatalogEntity>> = repository.observeDrugCatalog()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val materials: StateFlow<List<MaterialUsageEntity>> = repository.observeMaterials(shiftId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val materialCatalog: StateFlow<List<MaterialCatalogEntity>> = repository.observeMaterialCatalog()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val fluidBalance: StateFlow<List<FluidBalanceEntity>> = repository.observeFluidBalance(shiftId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val settings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            settingsDataStore.setThemeMode(mode)
        }
    }

    private val _feedback = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val feedback = _feedback.asSharedFlow()

    // --- Sinais Vitais (salva a cada alteração) ---
    fun updateVital(updated: VitalSignEntity) {
        viewModelScope.launch {
            val computedPam = VitalSignCalculator.calculatePamFromString(updated.bloodPressure)
            repository.updateVital(updated.copy(pam = computedPam))
        }
    }

    // --- Drogas Vasoativas / Infusões ---
    fun observeHistory(infusionId: Long): Flow<List<InfusionHistoryEntity>> =
        repository.observeInfusionHistory(infusionId)

    fun addInfusion(
        drugName: String,
        unit: String,
        referenceRangeLabel: String,
        minDose: Double?,
        maxDose: Double?,
        dose: String,
        rateMlH: String,
        startHour: Int,
        startMinute: Int,
        durationHours: Int,
        durationMinutes: Int,
        alarmEnabled: Boolean
    ) {
        viewModelScope.launch {
            val calc = InfusionTimeCalculator.calculatePredictedEnd(
                startHour = startHour,
                startMinute = startMinute,
                durationHours = durationHours,
                durationMinutes = durationMinutes
            )
            val entity = InfusionEntity(
                shiftId = shiftId,
                drugName = drugName.trim(),
                unit = unit.trim(),
                referenceRangeLabel = referenceRangeLabel.trim(),
                minDose = minDose,
                maxDose = maxDose,
                currentDose = dose.trim(),
                currentRateMlH = rateMlH.trim(),
                startHour = startHour,
                startMinute = startMinute,
                durationHours = durationHours,
                durationMinutes = durationMinutes,
                predictedEndLabel = calc.endTimeFormatted,
                crossedMidnight = calc.crossedMidnight,
                endEpochMillis = calc.endEpochMillis,
                isActive = true,
                alarmEnabled = alarmEnabled
            )
            val id = repository.addInfusion(entity)
            scheduleAlarmsFor(entity.copy(id = id))
            _feedback.tryEmit("Infusão de $drugName salva")
        }
    }

    fun updateInfusionDoseOrRate(
        existing: InfusionEntity,
        newDose: String,
        newRateMlH: String,
        startHour: Int,
        startMinute: Int,
        durationHours: Int,
        durationMinutes: Int,
        alarmEnabled: Boolean,
        note: String
    ) {
        viewModelScope.launch {
            val calc = InfusionTimeCalculator.calculatePredictedEnd(
                startHour = startHour,
                startMinute = startMinute,
                durationHours = durationHours,
                durationMinutes = durationMinutes
            )
            val updated = existing.copy(
                currentDose = newDose.trim(),
                currentRateMlH = newRateMlH.trim(),
                startHour = startHour,
                startMinute = startMinute,
                durationHours = durationHours,
                durationMinutes = durationMinutes,
                predictedEndLabel = calc.endTimeFormatted,
                crossedMidnight = calc.crossedMidnight,
                endEpochMillis = calc.endEpochMillis,
                alarmEnabled = alarmEnabled
            )
            repository.updateInfusionWithHistory(
                updated,
                note.ifBlank { "Alteração de dose/taxa" }
            )
            scheduleAlarmsFor(updated)
            _feedback.tryEmit("Alteração registrada no histórico")
        }
    }

    fun toggleInfusionActive(infusion: InfusionEntity, active: Boolean) {
        viewModelScope.launch {
            repository.toggleInfusionActive(infusion, active)
            if (active) {
                scheduleAlarmsFor(infusion.copy(isActive = true))
            } else {
                alarmScheduler.cancelInfusionAlerts(infusion.id)
            }
            _feedback.tryEmit(if (active) "Infusão retomada" else "Infusão encerrada")
        }
    }

    fun deleteInfusion(infusion: InfusionEntity) {
        viewModelScope.launch {
            alarmScheduler.cancelInfusionAlerts(infusion.id)
            repository.deleteInfusion(infusion)
        }
    }

    private suspend fun scheduleAlarmsFor(infusion: InfusionEntity) {
        val currentSettings = settingsDataStore.settingsFlow.first()
        val bed = shift.value?.bedNumber ?: "--"
        alarmScheduler.scheduleInfusionAlerts(
            infusion = infusion,
            bedNumber = bed,
            firstWarningMinutes = currentSettings.alarmMinutesBeforeFirst,
            secondWarningMinutes = currentSettings.alarmMinutesBeforeSecond
        )
    }

    // --- Materiais ---
    fun addMaterial(name: String, quantity: Int, notes: String) {
        viewModelScope.launch {
            repository.addOrIncrementMaterial(shiftId, name, quantity, notes)
            _feedback.tryEmit("Material adicionado")
        }
    }

    fun incrementMaterial(usage: MaterialUsageEntity, delta: Int) {
        viewModelScope.launch {
            val newQty = usage.quantity + delta
            if (newQty <= 0) {
                repository.deleteMaterialUsage(usage)
            } else {
                repository.updateMaterialUsage(usage.copy(quantity = newQty))
            }
        }
    }

    fun deleteMaterial(usage: MaterialUsageEntity) {
        viewModelScope.launch {
            repository.deleteMaterialUsage(usage)
        }
    }

    // --- Balanço Hídrico ---
    fun updateFluidBalance(item: FluidBalanceEntity) {
        viewModelScope.launch {
            repository.updateFluidBalance(item)
        }
    }

    // --- Encerrar Plantão ---
    fun endCurrentShift() {
        viewModelScope.launch {
            val current = shift.value ?: return@launch
            infusions.value.forEach { alarmScheduler.cancelInfusionAlerts(it.id) }
            repository.endShift(current)
            _feedback.tryEmit("Plantão encerrado")
        }
    }
}
