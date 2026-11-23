package br.com.plantaouti.domain.calculator

import kotlin.math.roundToInt

/**
 * Utilitários puros para interpretação de PA (formato 120x80) e cálculo de PAM.
 */
object VitalSignCalculator {

    data class BloodPressure(val systolic: Int, val diastolic: Int)

    /**
     * Faz o parsing de pressão arterial em formatos comuns de UTI: "120x80", "120/80", "120 X 80".
     */
    fun parseBloodPressure(raw: String?): BloodPressure? {
        if (raw.isNullOrBlank()) return null
        val normalized = raw.trim().replace("/", "x").replace("X", "x")
        val parts = normalized.split("x").map { it.trim() }
        if (parts.size != 2) return null
        val pas = parts[0].toIntOrNull() ?: return null
        val pad = parts[1].toIntOrNull() ?: return null
        if (pas <= 0 || pad <= 0 || pas < pad) return null
        return BloodPressure(systolic = pas, diastolic = pad)
    }

    /**
     * Calcula a Pressão Arterial Média (PAM) pela fórmula padrão:
     * PAM = (PAS + 2 * PAD) / 3, arredondada para inteiro.
     */
    fun calculatePam(systolic: Int, diastolic: Int): Int {
        return ((systolic + 2.0 * diastolic) / 3.0).roundToInt()
    }

    fun calculatePamFromString(rawPa: String?): Int? {
        val bp = parseBloodPressure(rawPa) ?: return null
        return calculatePam(bp.systolic, bp.diastolic)
    }
}
