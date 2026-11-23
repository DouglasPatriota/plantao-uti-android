package br.com.plantaouti.domain.calculator

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Calcula o término previsto de uma infusão contínua (hora de início + duração h:min),
 * tratando corretamente a virada de meia-noite.
 */
object InfusionTimeCalculator {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    data class PredictedEndResult(
        val endTimeFormatted: String,
        val crossedMidnight: Boolean,
        val endEpochMillis: Long
    )

    /**
     * @param startHour Hora de início (0..23)
     * @param startMinute Minuto de início (0..59)
     * @param durationHours Horas restantes (>= 0)
     * @param durationMinutes Minutos restantes (0..59)
     * @param referenceDate Data base do início da infusão
     * @param zoneId Fuso horário local
     */
    fun calculatePredictedEnd(
        startHour: Int,
        startMinute: Int,
        durationHours: Int,
        durationMinutes: Int,
        referenceDate: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): PredictedEndResult {
        require(startHour in 0..23) { "Hora de início inválida: $startHour" }
        require(startMinute in 0..59) { "Minuto de início inválido: $startMinute" }
        require(durationHours >= 0) { "Duração em horas não pode ser negativa" }
        require(durationMinutes in 0..59) { "Duração em minutos inválida: $durationMinutes" }

        val startDateTime = LocalDateTime.of(referenceDate, LocalTime.of(startHour, startMinute))
        val totalMinutes = durationHours.toLong() * 60L + durationMinutes.toLong()
        val endDateTime = startDateTime.plusMinutes(totalMinutes)

        val crossedMidnight = endDateTime.toLocalDate().isAfter(startDateTime.toLocalDate())
        val endEpochMillis = endDateTime.atZone(zoneId).toInstant().toEpochMilli()

        return PredictedEndResult(
            endTimeFormatted = endDateTime.toLocalTime().format(timeFormatter),
            crossedMidnight = crossedMidnight,
            endEpochMillis = endEpochMillis
        )
    }

    /**
     * Formata tempo restante em "Xh Ymin" ou "Encerrada".
     */
    fun formatRemainingMillis(remainingMillis: Long): String {
        if (remainingMillis <= 0L) return "Encerrada"
        val totalMinutes = (remainingMillis + 59_999L) / 60_000L
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 -> "${hours}h ${minutes.toString().padStart(2, '0')}min"
            else -> "${minutes}min"
        }
    }
}
