package br.com.plantaouti.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "plantao_uti_settings")

enum class AppThemeMode(val label: String) {
    SYSTEM("Seguir sistema"),
    LIGHT("Claro"),
    DARK("Escuro (Plantão Noturno)")
}

data class VitalThresholds(
    val pamMin: Int = 65,
    val pamMax: Int = 110,
    val fcMin: Int = 55,
    val fcMax: Int = 110,
    val frMin: Int = 12,
    val frMax: Int = 24,
    val spo2Min: Int = 92,
    val tempMin: Double = 35.5,
    val tempMax: Double = 37.8,
    val hgtMin: Int = 70,
    val hgtMax: Int = 180
)

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val defaultTechnicianName: String = "",
    val alarmMinutesBeforeFirst: Int = 30,
    val alarmMinutesBeforeSecond: Int = 10,
    val flagSecureEnabled: Boolean = true,
    val appLockEnabled: Boolean = false,
    val inactivityTimeoutMinutes: Int = 5,
    val pinHash: String = "",
    val vitalThresholds: VitalThresholds = VitalThresholds()
)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DEFAULT_TECH_NAME = stringPreferencesKey("default_tech_name")
        val ALARM_FIRST_MIN = intPreferencesKey("alarm_first_min")
        val ALARM_SECOND_MIN = intPreferencesKey("alarm_second_min")
        val FLAG_SECURE = booleanPreferencesKey("flag_secure")
        val APP_LOCK = booleanPreferencesKey("app_lock")
        val INACTIVITY_MIN = intPreferencesKey("inactivity_min")
        val PIN_HASH = stringPreferencesKey("pin_hash")

        val PAM_MIN = intPreferencesKey("pam_min")
        val PAM_MAX = intPreferencesKey("pam_max")
        val FC_MIN = intPreferencesKey("fc_min")
        val FC_MAX = intPreferencesKey("fc_max")
        val FR_MIN = intPreferencesKey("fr_min")
        val FR_MAX = intPreferencesKey("fr_max")
        val SPO2_MIN = intPreferencesKey("spo2_min")
        val TEMP_MIN = doublePreferencesKey("temp_min")
        val TEMP_MAX = doublePreferencesKey("temp_max")
        val HGT_MIN = intPreferencesKey("hgt_min")
        val HGT_MAX = intPreferencesKey("hgt_max")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val themeName = prefs[Keys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
        val theme = runCatching { AppThemeMode.valueOf(themeName) }.getOrDefault(AppThemeMode.SYSTEM)
        AppSettings(
            themeMode = theme,
            defaultTechnicianName = prefs[Keys.DEFAULT_TECH_NAME] ?: "",
            alarmMinutesBeforeFirst = prefs[Keys.ALARM_FIRST_MIN] ?: 30,
            alarmMinutesBeforeSecond = prefs[Keys.ALARM_SECOND_MIN] ?: 10,
            flagSecureEnabled = prefs[Keys.FLAG_SECURE] ?: true,
            appLockEnabled = prefs[Keys.APP_LOCK] ?: false,
            inactivityTimeoutMinutes = prefs[Keys.INACTIVITY_MIN] ?: 5,
            pinHash = prefs[Keys.PIN_HASH] ?: "",
            vitalThresholds = VitalThresholds(
                pamMin = prefs[Keys.PAM_MIN] ?: 65,
                pamMax = prefs[Keys.PAM_MAX] ?: 110,
                fcMin = prefs[Keys.FC_MIN] ?: 55,
                fcMax = prefs[Keys.FC_MAX] ?: 110,
                frMin = prefs[Keys.FR_MIN] ?: 12,
                frMax = prefs[Keys.FR_MAX] ?: 24,
                spo2Min = prefs[Keys.SPO2_MIN] ?: 92,
                tempMin = prefs[Keys.TEMP_MIN] ?: 35.5,
                tempMax = prefs[Keys.TEMP_MAX] ?: 37.8,
                hgtMin = prefs[Keys.HGT_MIN] ?: 70,
                hgtMax = prefs[Keys.HGT_MAX] ?: 180
            )
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setDefaultTechnicianName(name: String) {
        context.dataStore.edit { it[Keys.DEFAULT_TECH_NAME] = name }
    }

    suspend fun setAlarmMinutes(first: Int, second: Int) {
        context.dataStore.edit {
            it[Keys.ALARM_FIRST_MIN] = first.coerceIn(5, 180)
            it[Keys.ALARM_SECOND_MIN] = second.coerceIn(1, 60)
        }
    }

    suspend fun setFlagSecure(enabled: Boolean) {
        context.dataStore.edit { it[Keys.FLAG_SECURE] = enabled }
    }

    suspend fun setAppLock(enabled: Boolean) {
        context.dataStore.edit { it[Keys.APP_LOCK] = enabled }
    }

    suspend fun setInactivityTimeout(minutes: Int) {
        context.dataStore.edit { it[Keys.INACTIVITY_MIN] = minutes.coerceIn(1, 60) }
    }

    suspend fun setPinHash(hash: String) {
        context.dataStore.edit { it[Keys.PIN_HASH] = hash }
    }

    suspend fun updateVitalThresholds(t: VitalThresholds) {
        context.dataStore.edit {
            it[Keys.PAM_MIN] = t.pamMin
            it[Keys.PAM_MAX] = t.pamMax
            it[Keys.FC_MIN] = t.fcMin
            it[Keys.FC_MAX] = t.fcMax
            it[Keys.FR_MIN] = t.frMin
            it[Keys.FR_MAX] = t.frMax
            it[Keys.SPO2_MIN] = t.spo2Min
            it[Keys.TEMP_MIN] = t.tempMin
            it[Keys.TEMP_MAX] = t.tempMax
            it[Keys.HGT_MIN] = t.hgtMin
            it[Keys.HGT_MAX] = t.hgtMax
        }
    }
}
