package br.com.plantaouti.domain

import br.com.plantaouti.domain.calculator.InfusionTimeCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class InfusionTimeCalculatorTest {

    private val baseDate = LocalDate.of(2026, 11, 19)
    private val zone = ZoneId.of("America/Sao_Paulo")

    @Test
    fun `calcula termino no mesmo dia sem virar meia-noite`() {
        val result = InfusionTimeCalculator.calculatePredictedEnd(
            startHour = 8,
            startMinute = 30,
            durationHours = 4,
            durationMinutes = 15,
            referenceDate = baseDate,
            zoneId = zone
        )
        assertEquals("12:45", result.endTimeFormatted)
        assertFalse(result.crossedMidnight)
    }

    @Test
    fun `calcula termino corretamente na virada de meia-noite no plantao noturno`() {
        val result = InfusionTimeCalculator.calculatePredictedEnd(
            startHour = 22,
            startMinute = 40,
            durationHours = 3,
            durationMinutes = 35,
            referenceDate = baseDate,
            zoneId = zone
        )
        assertEquals("02:15", result.endTimeFormatted)
        assertTrue(result.crossedMidnight)
    }

    @Test
    fun `calcula termino exatamente as 00h00 virando o dia`() {
        val result = InfusionTimeCalculator.calculatePredictedEnd(
            startHour = 21,
            startMinute = 0,
            durationHours = 3,
            durationMinutes = 0,
            referenceDate = baseDate,
            zoneId = zone
        )
        assertEquals("00:00", result.endTimeFormatted)
        assertTrue(result.crossedMidnight)
    }
}
