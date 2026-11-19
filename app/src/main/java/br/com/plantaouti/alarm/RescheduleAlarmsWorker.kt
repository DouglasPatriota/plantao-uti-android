package br.com.plantaouti.alarm

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.plantaouti.data.datastore.SettingsDataStore
import br.com.plantaouti.data.repository.PlantaoRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Reagenda todos os alarmes de infusões ativas após reinicialização do aparelho.
 */
@HiltWorker
class RescheduleAlarmsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: PlantaoRepository,
    private val settingsDataStore: SettingsDataStore,
    private val scheduler: InfusionAlarmScheduler
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val settings = settingsDataStore.settingsFlow.first()
        val futureInfusions = repository.getActiveFutureInfusions(System.currentTimeMillis())
        for (infusion in futureInfusions) {
            val shift = repository.getShift(infusion.shiftId) ?: continue
            if (!shift.isEnded) {
                scheduler.scheduleInfusionAlerts(
                    infusion = infusion,
                    bedNumber = shift.bedNumber,
                    firstWarningMinutes = settings.alarmMinutesBeforeFirst,
                    secondWarningMinutes = settings.alarmMinutesBeforeSecond
                )
            }
        }
        return Result.success()
    }
}
