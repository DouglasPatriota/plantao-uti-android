package br.com.plantaouti.domain.validator

/**
 * Valida se uma dose informada pelo profissional está dentro da faixa de referência
 * cadastrada para a droga. O aplicativo NÃO calcula nem sugere doses — apenas alerta.
 */
object DoseRangeValidator {

    enum class Status {
        WITHIN_RANGE,
        BELOW_MIN,
        ABOVE_MAX,
        NO_REFERENCE_RANGE,
        INVALID_NUMBER
    }

    data class ValidationResult(
        val status: Status,
        val parsedDose: Double? = null,
        val message: String? = null
    ) {
        val isOutOfRange: Boolean
            get() = status == Status.BELOW_MIN || status == Status.ABOVE_MAX
    }

    /**
     * Aceita vírgula ou ponto decimal (ex.: "0,05" ou "0.05").
     */
    fun parseDose(raw: String): Double? {
        val cleaned = raw.trim().replace(",", ".")
        return cleaned.toDoubleOrNull()?.takeIf { it >= 0.0 }
    }

    fun validate(
        rawDose: String,
        minDose: Double?,
        maxDose: Double?,
        unit: String,
        referenceLabel: String
    ): ValidationResult {
        val dose = parseDose(rawDose)
            ?: return ValidationResult(Status.INVALID_NUMBER, null, "Informe um valor numérico válido.")

        if (minDose == null && maxDose == null) {
            return ValidationResult(Status.NO_REFERENCE_RANGE, dose, null)
        }

        if (minDose != null && dose < minDose) {
            return ValidationResult(
                status = Status.BELOW_MIN,
                parsedDose = dose,
                message = "Dose informada ($rawDose $unit) está ABAIXO da faixa de referência ($referenceLabel)."
            )
        }

        if (maxDose != null && dose > maxDose) {
            return ValidationResult(
                status = Status.ABOVE_MAX,
                parsedDose = dose,
                message = "Dose informada ($rawDose $unit) está ACIMA da faixa de referência ($referenceLabel)."
            )
        }

        return ValidationResult(Status.WITHIN_RANGE, dose, null)
    }
}
