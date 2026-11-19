package br.com.plantaouti.domain.model

enum class ShiftType(
    val label: String,
    val shortLabel: String,
    val startHour: Int,
    val hours: List<String>
) {
    DIURNO(
        label = "Diurno (07h–19h)",
        shortLabel = "Diurno",
        startHour = 7,
        hours = listOf(
            "07:00", "08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00"
        )
    ),
    NOTURNO(
        label = "Noturno (19h–07h)",
        shortLabel = "Noturno",
        startHour = 19,
        hours = listOf(
            "19:00", "20:00", "21:00", "22:00", "23:00", "00:00",
            "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00"
        )
    )
}
