package br.com.plantaouti.domain.validator

import br.com.plantaouti.data.datastore.VitalThresholds
import br.com.plantaouti.data.local.entity.VitalSignEntity
import br.com.plantaouti.domain.calculator.VitalSignCalculator

enum class AlertLevel { NORMAL, WARNING, CRITICAL }

/**
 * Avalia cada parâmetro de sinal vital contra as faixas configuráveis pelo usuário.
 * Serve exclusivamente como destaque visual discreto — nunca bloqueia o registro.
 */
object VitalAlertEvaluator {

    fun evaluatePam(pam: Int?, t: VitalThresholds): AlertLevel {
        if (pam == null) return AlertLevel.NORMAL
        return when {
            pam < t.pamMin - 5 || pam > t.pamMax + 15 -> AlertLevel.CRITICAL
            pam < t.pamMin || pam > t.pamMax -> AlertLevel.WARNING
            else -> AlertLevel.NORMAL
        }
    }

    fun evaluateHeartRate(fc: Int?, t: VitalThresholds): AlertLevel {
        if (fc == null) return AlertLevel.NORMAL
        return when {
            fc < t.fcMin - 10 || fc > t.fcMax + 20 -> AlertLevel.CRITICAL
            fc < t.fcMin || fc > t.fcMax -> AlertLevel.WARNING
            else -> AlertLevel.NORMAL
        }
    }

    fun evaluateRespRate(fr: Int?, t: VitalThresholds): AlertLevel {
        if (fr == null) return AlertLevel.NORMAL
        return when {
            fr < t.frMin - 3 || fr > t.frMax + 8 -> AlertLevel.CRITICAL
            fr < t.frMin || fr > t.frMax -> AlertLevel.WARNING
            else -> AlertLevel.NORMAL
        }
    }

    fun evaluateSpo2(spo2: Int?, t: VitalThresholds): AlertLevel {
        if (spo2 == null) return AlertLevel.NORMAL
        return when {
            spo2 < t.spo2Min - 4 -> AlertLevel.CRITICAL
            spo2 < t.spo2Min -> AlertLevel.WARNING
            else -> AlertLevel.NORMAL
        }
    }

    fun evaluateTemp(temp: Double?, t: VitalThresholds): AlertLevel {
        if (temp == null) return AlertLevel.NORMAL
        return when {
            temp >= 39.0 || temp < 35.0 -> AlertLevel.CRITICAL
            temp > t.tempMax || temp < t.tempMin -> AlertLevel.WARNING
            else -> AlertLevel.NORMAL
        }
    }

    fun evaluateHgt(hgt: Int?, t: VitalThresholds): AlertLevel {
        if (hgt == null) return AlertLevel.NORMAL
        return when {
            hgt < 60 || hgt > 300 -> AlertLevel.CRITICAL
            hgt < t.hgtMin || hgt > t.hgtMax -> AlertLevel.WARNING
            else -> AlertLevel.NORMAL
        }
    }

    fun evaluatePain(dor: Int?): AlertLevel {
        if (dor == null) return AlertLevel.NORMAL
        return when {
            dor >= 7 -> AlertLevel.CRITICAL
            dor >= 4 -> AlertLevel.WARNING
            else -> AlertLevel.NORMAL
        }
    }

    fun hasAnyAlert(vital: VitalSignEntity, t: VitalThresholds): Boolean {
        val pam = vital.pam ?: VitalSignCalculator.calculatePamFromString(vital.bloodPressure)
        return listOf(
            evaluatePam(pam, t),
            evaluateHeartRate(vital.heartRate, t),
            evaluateRespRate(vital.respRate, t),
            evaluateSpo2(vital.spo2, t),
            evaluateTemp(vital.temperature, t),
            evaluateHgt(vital.hgt, t),
            evaluatePain(vital.painScore)
        ).any { it != AlertLevel.NORMAL }
    }
}
