package br.com.plantaouti.alarm

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class InfusionAlertWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getInt(InfusionAlarmScheduler.EXTRA_NOTIFICATION_ID, 1001)
        val drugName = inputData.getString(InfusionAlarmScheduler.EXTRA_DRUG_NAME) ?: "Infusão"
        val bed = inputData.getString(InfusionAlarmScheduler.EXTRA_BED_NUMBER) ?: "--"
        val minutesBefore = inputData.getInt(InfusionAlarmScheduler.EXTRA_MINUTES_BEFORE, 10)
        val predictedEnd = inputData.getString(InfusionAlarmScheduler.EXTRA_PREDICTED_END) ?: "--:--"

        NotificationHelper.showInfusionAlert(
            context = applicationContext,
            notificationId = id,
            drugName = drugName,
            bedNumber = bed,
            minutesBefore = minutesBefore,
            predictedEnd = predictedEnd
        )
        return Result.success()
    }
}
