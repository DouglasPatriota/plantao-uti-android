package br.com.plantaouti.ui.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.plantaouti.alarm.InfusionAlarmScheduler
import br.com.plantaouti.data.datastore.AppSettings
import br.com.plantaouti.data.datastore.AppThemeMode
import br.com.plantaouti.data.datastore.SettingsDataStore
import br.com.plantaouti.data.local.entity.PatientShiftEntity
import br.com.plantaouti.data.repository.PlantaoRepository
import br.com.plantaouti.domain.model.ShiftType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientsViewModel @Inject constructor(
    private val repository: PlantaoRepository,
    private val settingsDataStore: SettingsDataStore,
    private val alarmScheduler: InfusionAlarmScheduler
) : ViewModel() {

    val shifts: StateFlow<List<PatientShiftEntity>> = repository.observeShifts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val settings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            settingsDataStore.setThemeMode(mode)
        }
    }

    fun createShift(
        patientName: String,
        bedNumber: String,
        weightKg: Double,
        shiftDate: String,
        birthDate: String,
        shiftType: ShiftType,
        technicianName: String,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            if (technicianName.isNotBlank()) {
                settingsDataStore.setDefaultTechnicianName(technicianName.trim())
            }
            val id = repository.createShiftWithHourlyRows(
                PatientShiftEntity(
                    patientName = patientName.trim(),
                    bedNumber = bedNumber.trim(),
                    weightKg = weightKg,
                    shiftDate = shiftDate.trim(),
                    birthDate = birthDate.trim(),
                    shiftType = shiftType,
                    technicianName = technicianName.trim()
                )
            )
            onCreated(id)
        }
    }

    fun updateShift(shift: PatientShiftEntity) {
        viewModelScope.launch {
            repository.updateShift(shift)
        }
    }

    fun endShift(shift: PatientShiftEntity) {
        viewModelScope.launch {
            val infusions = repository.getInfusions(shift.id)
            infusions.forEach { alarmScheduler.cancelInfusionAlerts(it.id) }
            repository.endShift(shift)
        }
    }

    fun reopenShift(shift: PatientShiftEntity) {
        viewModelScope.launch {
            repository.reopenShift(shift)
        }
    }

    fun deleteShift(shift: PatientShiftEntity) {
        viewModelScope.launch {
            val infusions = repository.getInfusions(shift.id)
            infusions.forEach { alarmScheduler.cancelInfusionAlerts(it.id) }
            repository.deleteShift(shift)
        }
    }
}
