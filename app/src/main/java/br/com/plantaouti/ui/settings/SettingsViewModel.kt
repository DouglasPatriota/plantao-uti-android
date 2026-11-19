package br.com.plantaouti.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.plantaouti.alarm.InfusionAlarmScheduler
import br.com.plantaouti.data.datastore.AppSettings
import br.com.plantaouti.data.datastore.AppThemeMode
import br.com.plantaouti.data.datastore.SettingsDataStore
import br.com.plantaouti.data.datastore.VitalThresholds
import br.com.plantaouti.data.local.entity.DrugCatalogEntity
import br.com.plantaouti.data.repository.PlantaoRepository
import br.com.plantaouti.security.PinHasher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val repository: PlantaoRepository,
    val alarmScheduler: InfusionAlarmScheduler
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val drugCatalog: StateFlow<List<DrugCatalogEntity>> = repository.observeDrugCatalog()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setThemeMode(mode: AppThemeMode) = viewModelScope.launch { settingsDataStore.setThemeMode(mode) }
    fun setTechnicianName(name: String) = viewModelScope.launch { settingsDataStore.setDefaultTechnicianName(name) }
    fun setAlarmMinutes(first: Int, second: Int) = viewModelScope.launch { settingsDataStore.setAlarmMinutes(first, second) }
    fun setFlagSecure(enabled: Boolean) = viewModelScope.launch { settingsDataStore.setFlagSecure(enabled) }
    fun setAppLock(enabled: Boolean) = viewModelScope.launch { settingsDataStore.setAppLock(enabled) }
    fun setInactivityTimeout(minutes: Int) = viewModelScope.launch { settingsDataStore.setInactivityTimeout(minutes) }
    fun setPin(pin: String) = viewModelScope.launch {
        settingsDataStore.setPinHash(if (pin.isBlank()) "" else PinHasher.hash(pin))
    }

    fun updateVitalThresholds(thresholds: VitalThresholds) = viewModelScope.launch {
        settingsDataStore.updateVitalThresholds(thresholds)
    }

    fun saveDrug(drug: DrugCatalogEntity) = viewModelScope.launch { repository.upsertDrug(drug) }
    fun deleteDrug(drug: DrugCatalogEntity) = viewModelScope.launch { repository.deleteDrug(drug) }
    fun restoreDefaultDrugs() = viewModelScope.launch { repository.restoreDefaultDrugCatalog() }

    fun eraseAllShiftData(onDone: () -> Unit) = viewModelScope.launch {
        repository.deleteAllShiftData()
        onDone()
    }
}
