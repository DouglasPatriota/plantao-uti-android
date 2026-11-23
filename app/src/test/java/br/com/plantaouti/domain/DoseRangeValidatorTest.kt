package br.com.plantaouti.domain

import br.com.plantaouti.domain.validator.DoseRangeValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DoseRangeValidatorTest {

    @Test
    fun `valida dose dentro da faixa com virgula brasileira`() {
        val result = DoseRangeValidator.validate(
            rawDose = "0,02",
            minDose = 0.01,
            maxDose = 0.03,
            unit = "UI/min",
            referenceLabel = "0,01 – 0,03"
        )
        assertEquals(DoseRangeValidator.Status.WITHIN_RANGE, result.status)
        assertFalse(result.isOutOfRange)
    }

    @Test
    fun `alerta dose acima da faixa de referencia`() {
        val result = DoseRangeValidator.validate(
            rawDose = "12,5",
            minDose = 0.01,
            maxDose = 10.0,
            unit = "mcg/kg/min",
            referenceLabel = "0,01 – 10"
        )
        assertEquals(DoseRangeValidator.Status.ABOVE_MAX, result.status)
        assertTrue(result.isOutOfRange)
    }

    @Test
    fun `alerta dose abaixo da faixa de referencia`() {
        val result = DoseRangeValidator.validate(
            rawDose = "0,1",
            minDose = 0.2,
            maxDose = 0.8,
            unit = "mg/kg/h",
            referenceLabel = "0,2 – 0,8"
        )
        assertEquals(DoseRangeValidator.Status.BELOW_MIN, result.status)
        assertTrue(result.isOutOfRange)
    }

    @Test
    fun `permite droga manual sem faixa de referencia cadastrada`() {
        val result = DoseRangeValidator.validate(
            rawDose = "5",
            minDose = null,
            maxDose = null,
            unit = "mL/h",
            referenceLabel = ""
        )
        assertEquals(DoseRangeValidator.Status.NO_REFERENCE_RANGE, result.status)
        assertFalse(result.isOutOfRange)
    }
}
