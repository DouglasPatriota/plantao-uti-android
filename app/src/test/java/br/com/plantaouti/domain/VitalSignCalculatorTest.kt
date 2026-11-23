package br.com.plantaouti.domain

import br.com.plantaouti.domain.calculator.VitalSignCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VitalSignCalculatorTest {

    @Test
    fun `calcula PAM corretamente para PA 120x80`() {
        assertEquals(93, VitalSignCalculator.calculatePam(120, 80))
        assertEquals(93, VitalSignCalculator.calculatePamFromString("120x80"))
    }

    @Test
    fun `aceita separador barra e espacos na PA`() {
        assertEquals(70, VitalSignCalculator.calculatePamFromString("90 / 60"))
        assertEquals(107, VitalSignCalculator.calculatePamFromString("140X90"))
    }

    @Test
    fun `retorna nulo para PA invalida ou invertida`() {
        assertNull(VitalSignCalculator.calculatePamFromString(""))
        assertNull(VitalSignCalculator.calculatePamFromString("80x120"))
        assertNull(VitalSignCalculator.calculatePamFromString("abc"))
    }
}
