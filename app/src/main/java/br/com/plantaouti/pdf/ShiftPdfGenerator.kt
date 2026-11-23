package br.com.plantaouti.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import br.com.plantaouti.data.local.entity.FluidBalanceEntity
import br.com.plantaouti.data.local.entity.InfusionEntity
import br.com.plantaouti.data.local.entity.MaterialUsageEntity
import br.com.plantaouti.data.local.entity.PatientShiftEntity
import br.com.plantaouti.data.local.entity.VitalSignEntity
import br.com.plantaouti.domain.model.ShiftType
import java.io.File
import java.io.FileOutputStream

/**
 * Gera uma folha A4 vertical (595 x 842 pt) em página única no layout de registro de UTI:
 * - Cabeçalho (paciente, leito, data, turno, técnico, peso)
 * - Tabela de Sinais Vitais
 * - Tabela de Drogas Vasoativas / Infusões Contínuas
 * - Bloco inferior dividido: Gastos com Materiais (esquerda) e Balanço Hídrico / Eliminações (direita)
 * Suporta tanto o plantão preenchido quanto uma folha em branco para preenchimento manual.
 */
data class PdfPrintOptions(
    val includeMaterials: Boolean = true,
    val includeDrugs: Boolean = true,
    val includeEvolutions: Boolean = true,
    val includeVitals: Boolean = true,
    val includeProcedures: Boolean = false
)

object ShiftPdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 22f

    private val tealColor = Color.rgb(0, 96, 100)
    private val headerBgColor = Color.rgb(228, 241, 242)
    private val lineColor = Color.rgb(160, 175, 180)
    private val textDark = Color.rgb(25, 35, 40)
    private val textMuted = Color.rgb(90, 105, 110)

    fun generateCustomShiftPdf(
        context: Context,
        shift: PatientShiftEntity,
        vitals: List<VitalSignEntity>,
        infusions: List<InfusionEntity>,
        materials: List<MaterialUsageEntity>,
        fluids: List<FluidBalanceEntity>,
        options: PdfPrintOptions
    ): File {
        return generateFilledShiftPdf(
            context = context,
            shift = shift,
            vitals = if (options.includeVitals) vitals else emptyList(),
            infusions = if (options.includeDrugs) infusions else emptyList(),
            materials = if (options.includeMaterials) materials else emptyList(),
            fluids = if (options.includeEvolutions) fluids else emptyList()
        )
    }

    fun generateFilledShiftPdf(
        context: Context,
        shift: PatientShiftEntity,
        vitals: List<VitalSignEntity>,
        infusions: List<InfusionEntity>,
        materials: List<MaterialUsageEntity>,
        fluids: List<FluidBalanceEntity>
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        drawSheet(
            canvas = page.canvas,
            shift = shift,
            vitals = vitals,
            infusions = infusions,
            materials = materials,
            fluids = fluids,
            blankShiftType = null
        )
        document.finishPage(page)

        val dir = File(context.cacheDir, "pdfs").apply { mkdirs() }
        val safeBed = shift.bedNumber.replace(Regex("[^A-Za-z0-9_-]"), "_")
        val file = File(dir, "Plantao_UTI_Leito_${safeBed}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    fun generateBlankShiftPdf(context: Context, shiftType: ShiftType): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        drawSheet(
            canvas = page.canvas,
            shift = null,
            vitals = emptyList(),
            infusions = emptyList(),
            materials = emptyList(),
            fluids = emptyList(),
            blankShiftType = shiftType
        )
        document.finishPage(page)

        val dir = File(context.cacheDir, "pdfs").apply { mkdirs() }
        val file = File(dir, "Folha_Plantao_UTI_Em_Branco_${shiftType.shortLabel}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun drawSheet(
        canvas: Canvas,
        shift: PatientShiftEntity?,
        vitals: List<VitalSignEntity>,
        infusions: List<InfusionEntity>,
        materials: List<MaterialUsageEntity>,
        fluids: List<FluidBalanceEntity>,
        blankShiftType: ShiftType?
    ) {
        val contentWidth = PAGE_WIDTH - MARGIN * 2

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 11.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tealColor
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headerCellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textDark
            textSize = 6.8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textDark
            textSize = 6.6f
        }
        val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textMuted
            textSize = 6.2f
        }
        val fillTeal = Paint().apply { color = tealColor; style = Paint.Style.FILL }
        val fillHeader = Paint().apply { color = headerBgColor; style = Paint.Style.FILL }
        val borderPaint = Paint().apply {
            color = lineColor
            style = Paint.Style.STROKE
            strokeWidth = 0.6f
        }

        var y = MARGIN

        // 1. Faixa de Título + Cabeçalho do Paciente
        canvas.drawRect(MARGIN, y, MARGIN + contentWidth, y + 20f, fillTeal)
        canvas.drawText("FOLHA DE REGISTRO DE PLANTÃO — UTI (ENFERMAGEM)", MARGIN + 6f, y + 13.5f, titlePaint)
        y += 22f

        val headerBoxH = 34f
        canvas.drawRect(MARGIN, y, MARGIN + contentWidth, y + headerBoxH, borderPaint)
        val pName = shift?.patientName ?: "________________________________________"
        val bed = shift?.bedNumber ?: "______"
        val weight = shift?.let { "%.1f kg".format(it.weightKg) } ?: "______ kg"
        val date = shift?.shiftDate ?: "___/___/______"
        val turno = shift?.shiftType?.label ?: (blankShiftType?.label ?: "Diurno / Noturno")
        val tech = shift?.technicianName ?: "____________________________"

        canvas.drawText("Paciente: $pName", MARGIN + 5f, y + 12f, headerCellPaint)
        canvas.drawText("Leito: $bed", MARGIN + 340f, y + 12f, headerCellPaint)
        canvas.drawText("Peso: $weight", MARGIN + 450f, y + 12f, headerCellPaint)
        canvas.drawText("Data: $date", MARGIN + 5f, y + 26f, cellPaint)
        canvas.drawText("Turno: $turno", MARGIN + 145f, y + 26f, cellPaint)
        canvas.drawText("Técnico(a): $tech", MARGIN + 340f, y + 26f, cellPaint)
        y += headerBoxH + 8f

        // 2. SINAIS VITAIS (13 linhas horárias)
        canvas.drawText("1. SINAIS VITAIS", MARGIN, y + 7f, sectionPaint)
        y += 10f

        val vitalCols = floatArrayOf(34f, 52f, 34f, 34f, 34f, 38f, 38f, 40f, 30f, contentWidth - 334f)
        val vitalHeaders = listOf("Hora", "PA", "PAM", "FC", "FR", "SpO2", "Temp", "HGT", "Dor", "Observações")
        val rowH = 14.5f

        drawTableRow(canvas, MARGIN, y, vitalCols, rowH, vitalHeaders, headerCellPaint, borderPaint, fillHeader)
        y += rowH

        val hours = shift?.shiftType?.hours ?: (blankShiftType ?: ShiftType.DIURNO).hours
        for (i in hours.indices) {
            val v = vitals.getOrNull(i)
            val rowValues = listOf(
                v?.hourLabel ?: hours[i],
                v?.bloodPressure ?: "",
                v?.pam?.toString() ?: "",
                v?.heartRate?.toString() ?: "",
                v?.respRate?.toString() ?: "",
                v?.spo2?.let { "$it%" } ?: "",
                v?.temperature?.let { "%.1f".format(it) } ?: "",
                v?.hgt?.toString() ?: "",
                v?.painScore?.toString() ?: "",
                v?.notes?.take(38) ?: ""
            )
            drawTableRow(canvas, MARGIN, y, vitalCols, rowH, rowValues, cellPaint, borderPaint, null)
            y += rowH
        }

        y += 8f

        // 3. DROGAS VASOATIVAS / INFUSÕES CONTÍNUAS (até 7 linhas na folha A4)
        canvas.drawText("2. DROGAS VASOATIVAS / INFUSÕES CONTÍNUAS", MARGIN, y + 7f, sectionPaint)
        y += 10f

        val drugCols = floatArrayOf(115f, 44f, 52f, 88f, 46f, 46f, 66f, contentWidth - 457f)
        val drugHeaders = listOf("Droga", "Dose", "Unidade", "Faixa Ref.", "Taxa(mL/h)", "Início", "Tempo Rest.", "Término Prev.")
        drawTableRow(canvas, MARGIN, y, drugCols, rowH, drugHeaders, headerCellPaint, borderPaint, fillHeader)
        y += rowH

        val drugRowCount = maxOf(6, minOf(infusions.size, 8))
        for (i in 0 until drugRowCount) {
            val inf = infusions.getOrNull(i)
            val values = if (inf != null) {
                val startStr = "%02d:%02d".format(inf.startHour, inf.startMinute)
                val durStr = "%dh%02dm".format(inf.durationHours, inf.durationMinutes)
                val endStr = if (inf.crossedMidnight) "${inf.predictedEndLabel} (+1d)" else inf.predictedEndLabel
                listOf(
                    inf.drugName.take(24),
                    inf.currentDose,
                    inf.unit,
                    inf.referenceRangeLabel.take(18),
                    inf.currentRateMlH,
                    startStr,
                    durStr,
                    endStr
                )
            } else {
                List(8) { "" }
            }
            drawTableRow(canvas, MARGIN, y, drugCols, rowH, values, cellPaint, borderPaint, null)
            y += rowH
        }

        y += 10f

        // 4. BLOCO INFERIOR: ESQUERDA = GASTOS COM MATERIAIS | DIREITA = BALANÇO HÍDRICO / ELIMINAÇÕES
        val gap = 10f
        val leftW = (contentWidth - gap) * 0.44f
        val rightW = contentWidth - gap - leftW
        val rightX = MARGIN + leftW + gap

        canvas.drawText("3. GASTOS COM MATERIAIS", MARGIN, y + 7f, sectionPaint)
        canvas.drawText("4. BALANÇO HÍDRICO / ELIMINAÇÕES (SEM mL)", rightX, y + 7f, sectionPaint)
        y += 10f

        val bottomStartY = y
        val smallRowH = 13.5f

        // Esquerda: Materiais
        val matCols = floatArrayOf(leftW * 0.56f, leftW * 0.14f, leftW * 0.30f)
        drawTableRow(canvas, MARGIN, bottomStartY, matCols, smallRowH, listOf("Material", "Qtd", "Obs."), headerCellPaint, borderPaint, fillHeader)
        var leftY = bottomStartY + smallRowH
        val maxMatRows = 14
        for (i in 0 until maxMatRows) {
            val m = materials.getOrNull(i)
            val vals = if (m != null) {
                listOf(m.materialName.take(24), m.quantity.toString(), m.notes.take(14))
            } else {
                listOf("", "", "")
            }
            drawTableRow(canvas, MARGIN, leftY, matCols, smallRowH, vals, cellPaint, borderPaint, null)
            leftY += smallRowH
        }

        // Direita: Balanço Hídrico (13 horas + linha de TOTAL)
        val fbCols = floatArrayOf(32f, 26f, 32f, 26f, 38f, 26f, rightW - 180f)
        drawTableRow(
            canvas, rightX, bottomStartY, fbCols, smallRowH,
            listOf("Hora", "SVD", "Fralda", "Vaso", "Papag.", "Fez.", "Aspecto"),
            headerCellPaint, borderPaint, fillHeader
        )
        var rightY = bottomStartY + smallRowH
        var sumSvd = 0; var sumFralda = 0; var sumVaso = 0; var sumPapagaio = 0; var sumStool = 0
        for (i in hours.indices) {
            val f = fluids.getOrNull(i)
            if (f != null) {
                if (f.diuresisSvd) sumSvd++
                if (f.diuresisFralda) sumFralda++
                if (f.diuresisVaso) sumVaso++
                if (f.diuresisPapagaio) sumPapagaio++
                sumStool += f.stoolCount
            }
            val vals = listOf(
                f?.hourLabel ?: hours[i],
                if (f?.diuresisSvd == true) "X" else "",
                if (f?.diuresisFralda == true) "X" else "",
                if (f?.diuresisVaso == true) "X" else "",
                if (f?.diuresisPapagaio == true) "X" else "",
                if ((f?.stoolCount ?: 0) > 0) f!!.stoolCount.toString() else "",
                f?.stoolAspect?.take(18) ?: ""
            )
            drawTableRow(canvas, rightX, rightY, fbCols, smallRowH, vals, cellPaint, borderPaint, null)
            rightY += smallRowH
        }

        // Linha de TOTAL do Balanço
        val totalVals = if (shift != null) {
            listOf("TOTAL", "$sumSvd", "$sumFralda", "$sumVaso", "$sumPapagaio", "$sumStool", "")
        } else {
            listOf("TOTAL", "", "", "", "", "", "")
        }
        drawTableRow(canvas, rightX, rightY, fbCols, smallRowH, totalVals, headerCellPaint, borderPaint, fillHeader)

        // Rodapé LGPD e Aviso Clínico
        canvas.drawText(
            "Plantão UTI • Registro auxiliar de enfermagem (não calcula nem sugere doses). Dados armazenados localmente (LGPD).",
            MARGIN,
            PAGE_HEIGHT - 12f,
            mutedPaint
        )
    }

    private fun drawTableRow(
        canvas: Canvas,
        startX: Float,
        y: Float,
        colWidths: FloatArray,
        height: Float,
        values: List<String>,
        textPaint: Paint,
        borderPaint: Paint,
        bgPaint: Paint?
    ) {
        var x = startX
        for (i in colWidths.indices) {
            val w = colWidths[i]
            if (bgPaint != null) {
                canvas.drawRect(x, y, x + w, y + height, bgPaint)
            }
            canvas.drawRect(x, y, x + w, y + height, borderPaint)
            val txt = values.getOrElse(i) { "" }
            if (txt.isNotEmpty()) {
                canvas.drawText(txt, x + 2.5f, y + height - 3.8f, textPaint)
            }
            x += w
        }
    }
}
