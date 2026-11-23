package br.com.plantaouti.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class InfusionAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(InfusionAlarmScheduler.EXTRA_NOTIFICATION_ID, 1001)
        val drugName = intent.getStringExtra(InfusionAlarmScheduler.EXTRA_DRUG_NAME) ?: "Infusão"
        val bed = intent.getStringExtra(InfusionAlarmScheduler.EXTRA_BED_NUMBER) ?: "--"
        val minutesBefore = intent.getIntExtra(InfusionAlarmScheduler.EXTRA_MINUTES_BEFORE, 10)
        val predictedEnd = intent.getStringExtra(InfusionAlarmScheduler.EXTRA_PREDICTED_END) ?: "--:--"

        NotificationHelper.showInfusionAlert(
            context = context,
            notificationId = id,
            drugName = drugName,
            bedNumber = bed,
            minutesBefore = minutesBefore,
            predictedEnd = predictedEnd
        )
    }
}
