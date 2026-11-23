package br.com.plantaouti.domain

import br.com.plantaouti.domain.calculator.MedicalCalculators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MedicalCalculatorsTest {

    @Test
    fun testDateMaskFormatting() {
        assertEquals("15/04/1975", MedicalCalculators.formatDateMask("15041975"))
        assertEquals("01/01/2000", MedicalCalculators.formatDateMask("01012000"))
        assertEquals("12/34/5", MedicalCalculators.formatDateMask("12345"))
    }

    @Test
    fun testAgeCalculation() {
        val age = MedicalCalculators.calculateAge("15/04/1975")
        assertNotNull(age)
        assertNull(MedicalCalculators.calculateAge("data_invalida"))
    }

    @Test
    fun testDilutionCalculator() {
        // Exemplo: 500 mg prescritos, ampola de 250 mg em 10 mL -> 20 mL
        val vol = MedicalCalculators.calculateDilution(500.0, 250.0, 10.0)
        assertEquals(20.0, vol!!, 0.001)
    }

    @Test
    fun testRuleOfThreeCalculator() {
        // Exemplo: 100 mg disponível em 2 mL, 50 mg prescrito -> 1.0 mL
        val result = MedicalCalculators.calculateRuleOfThree(100.0, 2.0, 50.0)
        assertEquals(1.0, result!!, 0.001)
    }

    @Test
    fun testDripRateCalculator() {
        // Exemplo: 500 mL em 8 horas -> Gotas/min = 500 / (8*3) = 20.83, Microgotas/min = 500 / 8 = 62.5
        val drip = MedicalCalculators.calculateDripRate(500.0, 8.0)
        assertNotNull(drip)
        assertEquals(20.833, drip!!.dropsPerMin, 0.01)
        assertEquals(62.5, drip.microdropsPerMin, 0.01)
    }

    @Test
    fun testContinuousInfusionCalculator() {
        // Exemplo: Peso 70kg, Dose 0.1 mcg/kg/min, Concentração 16 mcg/mL
        // (0.1 * 70 * 60) / 16 = 420 / 16 = 26.25 mL/h
        val rate = MedicalCalculators.calculateContinuousInfusion(70.0, 0.1, 16.0)
        assertNotNull(rate)
        assertEquals(26.25, rate!!, 0.01)
    }
}
