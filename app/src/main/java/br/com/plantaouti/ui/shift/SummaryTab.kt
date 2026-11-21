package br.com.plantaouti.ui.shift

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import br.com.plantaouti.ui.components.FullWidthActionButton
import br.com.plantaouti.ui.components.TabularNumbersTextStyle

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
        Text("Passagem de Plantão", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${currentShift.patientName} — Leito ${currentShift.bedNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Turno: ${currentShift.shiftType.label} • Data: ${currentShift.shiftDate}")
                Text("Peso: %.1f kg • Técnico(a): ${currentShift.technicianName}".format(currentShift.weightKg))
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Sinais Vitais (${filledVitals.size} horários registrados)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (lastVital != null) {
                    Text(
                        "Último (${lastVital.hourLabel}h): PA ${lastVital.bloodPressure.ifBlank { "—" }} | PAM ${lastVital.pam ?: "—"} | FC ${lastVital.heartRate ?: "—"} | SpO₂ ${lastVital.spo2?.let { "$it%" } ?: "—"} | Temp ${lastVital.temperature?.let { "%.1f°C".format(it) } ?: "—"}",
                        style = MaterialTheme.typography.bodyMedium.merge(TabularNumbersTextStyle)
                    )
                } else {
                    Text("Nenhum sinal vital registrado ainda.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (alertHours.isNotEmpty()) {
                    Text(
                        "⚠ Horários com valores fora da faixa: ${alertHours.joinToString { "${it.hourLabel}h" }}",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Infusões Contínuas Ativas (${activeInfusions.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (activeInfusions.isEmpty()) {
                    Text("Nenhuma infusão ativa no momento.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    activeInfusions.forEach { inf ->
                        val endSuffix = if (inf.crossedMidnight) " (+1d)" else ""
                        Text("• ${inf.drugName}: ${inf.currentDose} ${inf.unit} (${inf.currentRateMlH} mL/h) — término ${inf.predictedEndLabel}$endSuffix", style = MaterialTheme.typography.bodyMedium.merge(TabularNumbersTextStyle))
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Eliminações & Materiais", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Diurese (registros): SVD=$totalSvd • Fralda=$totalFralda • Vaso=$totalVaso • Papagaio=$totalPapagaio", style = MaterialTheme.typography.bodyMedium.merge(TabularNumbersTextStyle))
                Text("Evacuações: $totalStool", style = MaterialTheme.typography.bodyMedium.merge(TabularNumbersTextStyle))
                Text("Materiais lançados: ${materials.size} tipos (${materials.sumOf { it.quantity }} unidades)", style = MaterialTheme.typography.bodyMedium.merge(TabularNumbersTextStyle))
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    val file = ShiftPdfGenerator.generateFilledShiftPdf(
                        context, currentShift, vitals, infusions, materials, fluids
                    )
                    PdfShareAndPrintHelper.sharePdf(context, file)
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("Compartilhar", fontWeight = FontWeight.Bold)
            }

            FilledTonalButton(
                onClick = {
                    val file = ShiftPdfGenerator.generateFilledShiftPdf(
                        context, currentShift, vitals, infusions, materials, fluids
                    )
                    PdfShareAndPrintHelper.printPdf(context, file, "Plantao_Leito_${currentShift.bedNumber}")
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Icon(Icons.Rounded.Print, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("Imprimir A4", fontWeight = FontWeight.Bold)
            }
        }

        OutlinedButton(
            onClick = {
                val blank = ShiftPdfGenerator.generateBlankShiftPdf(context, currentShift.shiftType)
                PdfShareAndPrintHelper.sharePdf(context, blank, "Folha A4 em Branco")
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(Icons.Rounded.Description, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Gerar Folha PDF em Branco (${currentShift.shiftType.shortLabel})", fontWeight = FontWeight.Bold)
        }

        if (!currentShift.isEnded) {
            FullWidthActionButton(
                text = "Encerrar Plantão deste Paciente",
                icon = Icons.Rounded.CheckCircle,
                onClick = { viewModel.endCurrentShift() }
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}
