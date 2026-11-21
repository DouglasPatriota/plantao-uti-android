package br.com.plantaouti.ui.shift

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.plantaouti.domain.calculator.MedicalCalculators

private enum class CalcType(val label: String) {
    DILUTION("Diluição"),
    RULE_OF_THREE("Regra de Três"),
    DRIP("Gotejamento"),
    INFUSION("Infusão Contínua")
}

@Composable
fun CalculationsTab(viewModel: ShiftWorkspaceViewModel) {
    val shift by viewModel.shift.collectAsStateWithLifecycle()
    var selectedCalc by remember { mutableStateOf(CalcType.DILUTION) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
    ) {
        // Banner Fixo de Alerta Âmbar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF33291A)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Aviso Beta",
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.padding(end = 10.dp)
                )
                Text(
                    "Ferramenta em fase de testes. Sempre conferir todos os cálculos antes da administração de medicamentos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFFE0B2),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Seletor de Calculadora (FilterChips)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcType.entries.forEach { calc ->
                FilterChip(
                    selected = selectedCalc == calc,
                    onClick = { selectedCalc = calc },
                    label = { Text(calc.label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        when (selectedCalc) {
            CalcType.DILUTION -> DilutionCalculatorCard()
            CalcType.RULE_OF_THREE -> RuleOfThreeCalculatorCard()
            CalcType.DRIP -> DripRateCalculatorCard()
            CalcType.INFUSION -> ContinuousInfusionCalculatorCard(patientWeight = shift?.weightKg ?: 70.0)
        }
    }
}

@Composable
private fun DilutionCalculatorCard() {
    var drugName by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var presentation by remember { mutableStateOf("") }
    var volume by remember { mutableStateOf("") }

    val doseNum = dose.replace(",", ".").toDoubleOrNull() ?: 0.0
    val presNum = presentation.replace(",", ".").toDoubleOrNull() ?: 0.0
    val volNum = volume.replace(",", ".").toDoubleOrNull() ?: 0.0

    val result = remember(doseNum, presNum, volNum) {
        MedicalCalculators.calculateDilution(doseNum, presNum, volNum)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Cálculo de Diluição de Medicamentos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = drugName,
                onValueChange = { drugName = it },
                label = { Text("Nome da medicação (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dose,
                    onValueChange = { dose = it },
                    label = { Text("Dose prescrita (mg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = presentation,
                    onValueChange = { presentation = it },
                    label = { Text("Apresentação (mg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = volume,
                onValueChange = { volume = it },
                label = { Text("Volume da ampola (mL)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))

            ResultCard(
                title = "Volume a administrar:",
                value = result?.let { "%.2f mL".format(it) } ?: "—"
            )
        }
    }
}

@Composable
private fun RuleOfThreeCalculatorCard() {
    var available by remember { mutableStateOf("") }
    var prescribed by remember { mutableStateOf("") }
    var vehicleVolume by remember { mutableStateOf("1") }

    val availNum = available.replace(",", ".").toDoubleOrNull() ?: 0.0
    val prescNum = prescribed.replace(",", ".").toDoubleOrNull() ?: 0.0
    val vehNum = vehicleVolume.replace(",", ".").toDoubleOrNull() ?: 1.0

    val result = remember(availNum, vehNum, prescNum) {
        MedicalCalculators.calculateRuleOfThree(availNum, vehNum, prescNum)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Regra de Três Proporcional", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = available,
                    onValueChange = { available = it },
                    label = { Text("Qtd. disponível (A)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = vehicleVolume,
                    onValueChange = { vehicleVolume = it },
                    label = { Text("Volume veículo (mL)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = prescribed,
                onValueChange = { prescribed = it },
                label = { Text("Qtd. prescrita (C)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))

            ResultCard(
                title = "Resultado calculado (X):",
                value = result?.let { "%.2f mL".format(it) } ?: "—"
            )
        }
    }
}

@Composable
private fun DripRateCalculatorCard() {
    var volume by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("") }

    val volNum = volume.replace(",", ".").toDoubleOrNull() ?: 0.0
    val hrsNum = hours.replace(",", ".").toDoubleOrNull() ?: 0.0

    val dripResult = remember(volNum, hrsNum) {
        MedicalCalculators.calculateDripRate(volNum, hrsNum)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Cálculo de Gotejamento de Soro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = volume,
                    onValueChange = { volume = it },
                    label = { Text("Volume total (mL)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Tempo (horas)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ResultCard(
                    title = "Macrogotas:",
                    value = dripResult?.let { "%.1f got/min".format(it.dropsPerMin) } ?: "—",
                    modifier = Modifier.weight(1f)
                )
                ResultCard(
                    title = "Microgotas:",
                    value = dripResult?.let { "%.1f microgot/min".format(it.microdropsPerMin) } ?: "—",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ContinuousInfusionCalculatorCard(patientWeight: Double) {
    var weightStr by remember { mutableStateOf(patientWeight.toString()) }
    var doseStr by remember { mutableStateOf("") }
    var concStr by remember { mutableStateOf("") }

    val weightNum = weightStr.replace(",", ".").toDoubleOrNull() ?: patientWeight
    val doseNum = doseStr.replace(",", ".").toDoubleOrNull() ?: 0.0
    val concNum = concStr.replace(",", ".").toDoubleOrNull() ?: 0.0

    val infusionRate = remember(weightNum, doseNum, concNum) {
        MedicalCalculators.calculateContinuousInfusion(weightNum, doseNum, concNum)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Infusão Contínua (Vasoativos)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = weightStr,
                onValueChange = { weightStr = it },
                label = { Text("Peso do paciente (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = doseStr,
                    onValueChange = { doseStr = it },
                    label = { Text("Dose (mcg/kg/min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = concStr,
                    onValueChange = { concStr = it },
                    label = { Text("Concentração (mcg/mL)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(4.dp))

            ResultCard(
                title = "Taxa da Bomba (mL/h):",
                value = infusionRate?.let { "%.1f mL/h".format(it) } ?: "—"
            )
        }
    }
}

@Composable
private fun ResultCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
