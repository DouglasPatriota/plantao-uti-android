package br.com.plantaouti.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import br.com.plantaouti.data.local.entity.InfusionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Agenda avisos de término de infusão (padrão 30 min e 10 min antes, configurável).
 * Usa AlarmManager.setExactAndAllowWhileIdle quando a permissão de alarme exato (Android 12+)
 * está concedida, com fallback automático para WorkManager para garantir entrega offline/bloqueado.
 */
@Singleton
class InfusionAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun scheduleInfusionAlerts(
        infusion: InfusionEntity,
        bedNumber: String,
        firstWarningMinutes: Int,
        secondWarningMinutes: Int
    ) {
        cancelInfusionAlerts(infusion.id)
        if (!infusion.isActive || !infusion.alarmEnabled) return

        val now = System.currentTimeMillis()
        val offsets = listOf(firstWarningMinutes, secondWarningMinutes, 0).distinct()

        offsets.forEachIndexed { idx, minutesBefore ->
            val triggerAt = infusion.endEpochMillis - minutesBefore * 60_000L
            if (triggerAt <= now) return@forEachIndexed

            val requestCode = buildRequestCode(infusion.id, idx)
            if (canScheduleExactAlarms()) {
                val intent = Intent(context, InfusionAlarmReceiver::class.java).apply {
                    putExtra(EXTRA_NOTIFICATION_ID, requestCode)
                    putExtra(EXTRA_DRUG_NAME, infusion.drugName)
                    putExtra(EXTRA_BED_NUMBER, bedNumber)
                    putExtra(EXTRA_MINUTES_BEFORE, minutesBefore)
                    putExtra(EXTRA_PREDICTED_END, infusion.predictedEndLabel)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            } else {
                // Fallback seguro via WorkManager quando o usuário ainda não liberou alarmes exatos
                val delayMs = (triggerAt - now).coerceAtLeast(1_000L)
                val data = Data.Builder()
                    .putInt(EXTRA_NOTIFICATION_ID, requestCode)
                    .putString(EXTRA_DRUG_NAME, infusion.drugName)
                    .putString(EXTRA_BED_NUMBER, bedNumber)
                    .putInt(EXTRA_MINUTES_BEFORE, minutesBefore)
                    .putString(EXTRA_PREDICTED_END, infusion.predictedEndLabel)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<InfusionAlertWorker>()
                    .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .addTag(workTag(infusion.id))
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "infusion_${infusion.id}_$idx",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            }
        }
    }

    fun cancelInfusionAlerts(infusionId: Long) {
        for (idx in 0..2) {
            val requestCode = buildRequestCode(infusionId, idx)
            val intent = Intent(context, InfusionAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
        WorkManager.getInstance(context).cancelAllWorkByTag(workTag(infusionId))
    }

    private fun buildRequestCode(infusionId: Long, slotIndex: Int): Int =
        ((infusionId % 100_000L).toInt() * 10) + slotIndex

    private fun workTag(infusionId: Long): String = "infusion_alert_$infusionId"

    companion object {
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_DRUG_NAME = "extra_drug_name"
        const val EXTRA_BED_NUMBER = "extra_bed_number"
        const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"
        const val EXTRA_PREDICTED_END = "extra_predicted_end"
    }
}
