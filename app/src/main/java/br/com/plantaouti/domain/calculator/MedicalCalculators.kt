package br.com.plantaouti.domain.calculator

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

object MedicalCalculators {

    /**
     * Formata automaticamente os dígitos digitados no formato dd/MM/aaaa.
     * Exemplo: "15041975" -> "15/04/1975"
     */
    fun formatDateMask(input: String): String {
        val digits = input.filter { it.isDigit() }.take(8)
        val sb = StringBuilder()
        for (i in digits.indices) {
            if (i == 2 || i == 4) {
                sb.append('/')
            }
            sb.append(digits[i])
        }
        return sb.toString()
    }

    /**
     * Calcula a idade em anos a partir de uma data de nascimento no formato dd/MM/aaaa.
     */
    fun calculateAge(birthDateStr: String): Int? {
        return try {
            val parts = birthDateStr.split("/")
            if (parts.size == 3 && parts[2].length == 4) {
                val day = parts[0].toInt()
                val month = parts[1].toInt()
                val year = parts[2].toInt()
                val birthDate = LocalDate.of(year, month, day)
                val today = LocalDate.now()
                val age = Period.between(birthDate, today).years
                if (age in 0..130) age else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Cálculo de Diluição:
     * Volume (mL) = (Dose Prescrita * Volume Ampola) / Apresentação
     */
    fun calculateDilution(dosePrescribed: Double, presentationMg: Double, ampouleVolumeMl: Double): Double? {
        if (presentationMg <= 0.0 || ampouleVolumeMl <= 0.0 || dosePrescribed < 0.0) return null
        return (dosePrescribed * ampouleVolumeMl) / presentationMg
    }

    /**
     * Regra de Três:
     * X = (Quantidade Prescrita * Volume Disponível) / Quantidade Disponível
     */
    fun calculateRuleOfThree(availableAmount: Double, availableVolumeMl: Double, prescribedAmount: Double): Double? {
        if (availableAmount <= 0.0 || availableVolumeMl < 0.0 || prescribedAmount < 0.0) return null
        return (prescribedAmount * availableVolumeMl) / availableAmount
    }

    /**
     * Cálculo de Gotejamento:
     * Gotas/min = Volume Total (mL) / (Tempo em Horas * 3)
     * Microgotas/min = Volume Total (mL) / Tempo em Horas
     */
    data class DripResult(
        val dropsPerMin: Double,
        val microdropsPerMin: Double
    )

    fun calculateDripRate(volumeMl: Double, timeHours: Double): DripResult? {
        if (volumeMl <= 0.0 || timeHours <= 0.0) return null
        val drops = volumeMl / (timeHours * 3.0)
        val microdrops = volumeMl / timeHours
        return DripResult(dropsPerMin = drops, microdropsPerMin = microdrops)
    }

    /**
     * Cálculo de Infusão Contínua (mL/h):
     * Taxa (mL/h) = (Dose mcg/kg/min * Peso kg * 60) / Concentração mcg/mL
     */
    fun calculateContinuousInfusion(
        weightKg: Double,
        doseMcgKgMin: Double,
        concentrationMcgMl: Double
    ): Double? {
        if (weightKg <= 0.0 || doseMcgKgMin < 0.0 || concentrationMcgMl <= 0.0) return null
        return (doseMcgKgMin * weightKg * 60.0) / concentrationMcgMl
    }
}
