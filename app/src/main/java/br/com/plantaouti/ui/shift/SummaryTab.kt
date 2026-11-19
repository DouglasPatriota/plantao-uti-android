package br.com.plantaouti.ui.shift

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.plantaouti.domain.validator.VitalAlertEvaluator
import br.com.plantaouti.pdf.PdfShareAndPrintHelper
import br.com.plantaouti.pdf.ShiftPdfGenerator

@Composable
fun SummaryTab(viewModel: ShiftWorkspaceViewModel) {
    val shift by viewModel.shift.collectAsStateWithLifecycle()
    val vitals by viewModel.vitals.collectAsStateWithLifecycle()
    val infusions by viewModel.infusions.collectAsStateWithLifecycle()
    val materials by viewModel.materials.collectAsStateWithLifecycle()
    val fluids by viewModel.fluidBalance.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val currentShift = shift ?: return
    val filledVitals = vitals.filter {
        it.bloodPressure.isNotBlank() || it.heartRate != null || it.spo2 != null || it.temperature != null
    }
    val lastVital = filledVitals.lastOrNull()
    val alertHours = filledVitals.filter { VitalAlertEvaluator.hasAnyAlert(it, settings.vitalThresholds) }
    val activeInfusions = infusions.filter { it.isActive }

    val totalSvd = fluids.count { it.diuresisSvd }
    val totalFralda = fluids.count { it.diuresisFralda }
    val totalVaso = fluids.count { it.diuresisVaso }
    val totalPapagaio = fluids.count { it.diuresisPapagaio }
    val totalStool = fluids.sumOf { it.stoolCount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Passagem de Plantão", style = MaterialTheme.typography.titleLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${currentShift.patientName} — Leito ${currentShift.bedNumber}", style = MaterialTheme.typography.titleMedium)
                Text("Turno: ${currentShift.shiftType.label} • Data: ${currentShift.shiftDate}")
                Text("Peso: %.1f kg • Técnico(a): ${currentShift.technicianName}".format(currentShift.weightKg))
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Sinais Vitais (${filledVitals.size} horários registrados)", style = MaterialTheme.typography.titleSmall)
                if (lastVital != null) {
                    Text(
                        "Último (${lastVital.hourLabel}): PA ${lastVital.bloodPressure.ifBlank { "—" }} | PAM ${lastVital.pam ?: "—"} | FC ${lastVital.heartRate ?: "—"} | SpO₂ ${lastVital.spo2?.let { "$it%" } ?: "—"} | Temp ${lastVital.temperature?.let { "%.1f°C".format(it) } ?: "—"}"
                    )
                } else {
                    Text("Nenhum sinal vital registrado ainda.")
                }
                if (alertHours.isNotEmpty()) {
                    Text(
                        "Horários com valores fora da faixa: ${alertHours.joinToString { it.hourLabel }}",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Infusões Contínuas Ativas (${activeInfusions.size})", style = MaterialTheme.typography.titleSmall)
                if (activeInfusions.isEmpty()) {
                    Text("Nenhuma infusão ativa no momento.")
                } else {
                    activeInfusions.forEach { inf ->
                        val endSuffix = if (inf.crossedMidnight) " (+1d)" else ""
                        Text("• ${inf.drugName}: ${inf.currentDose} ${inf.unit} (${inf.currentRateMlH} mL/h) — término ${inf.predictedEndLabel}$endSuffix")
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Eliminações & Materiais", style = MaterialTheme.typography.titleSmall)
                Text("Diurese (contagem): SVD=$totalSvd • Fralda=$totalFralda • Vaso=$totalVaso • Papagaio=$totalPapagaio")
                Text("Evacuações: $totalStool")
                Text("Materiais registrados: ${materials.size} tipos (${materials.sumOf { it.quantity }} unidades)")
            }
        }

        // Botões de Exportação PDF A4, Impressão e PDF em Branco
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    val file = ShiftPdfGenerator.generateFilledShiftPdf(
                        context, currentShift, vitals, infusions, materials, fluids
                    )
                    PdfShareAndPrintHelper.sharePdf(context, file)
                },
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Compartilhar PDF")
            }

            FilledTonalButton(
                onClick = {
                    val file = ShiftPdfGenerator.generateFilledShiftPdf(
                        context, currentShift, vitals, infusions, materials, fluids
                    )
                    PdfShareAndPrintHelper.printPdf(context, file, "Plantao_Leito_${currentShift.bedNumber}")
                },
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Icon(Icons.Default.Print, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Imprimir A4")
            }
        }

        OutlinedButton(
            onClick = {
                val blank = ShiftPdfGenerator.generateBlankShiftPdf(context, currentShift.shiftType)
                PdfShareAndPrintHelper.sharePdf(context, blank, "Folha A4 em Branco")
            },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Description, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Gerar Folha PDF em Branco (${currentShift.shiftType.shortLabel})")
        }

        if (!currentShift.isEnded) {
            FilledTonalButton(
                onClick = { viewModel.endCurrentShift() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Encerrar Plantão deste Paciente")
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
