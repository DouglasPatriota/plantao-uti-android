package br.com.plantaouti.ui.shift

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import br.com.plantaouti.ui.components.FormOutlinedTextField
import br.com.plantaouti.ui.components.FormSectionHeader
import br.com.plantaouti.ui.components.TabularNumbersTextStyle

private enum class CalcType(val label: String) {
    DILUTION("Diluição"),
    RULE_OF_THREE("Regra de Três"),
    DRIP("Gotejamento"),
    INFUSION("Infusão Contínua")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalculationsTab(viewModel: ShiftWorkspaceViewModel) {
    val shift by viewModel.shift.collectAsStateWithLifecycle()
    var selectedCalc by remember { mutableStateOf(CalcType.DILUTION) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner Fixo de Alerta
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF33291A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = "Aviso Beta",
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 10.dp)
                )
                Text(
                    "Ferramenta em fase de testes. Sempre conferir todos os cálculos antes da administração de medicamentos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFFE0B2),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Seletor de Calculadora (FilterChips em FlowRow)
        Text("Tipo de Cálculo Médico", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcType.entries.forEach { calc ->
                FilterChip(
                    selected = selectedCalc == calc,
                    onClick = { selectedCalc = calc },
                    label = { Text(calc.label, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        when (selectedCalc) {
            CalcType.DILUTION -> DilutionCalculatorCard()
            CalcType.RULE_OF_THREE -> RuleOfThreeCalculatorCard()
            CalcType.DRIP -> DripRateCalculatorCard()
            CalcType.INFUSION -> ContinuousInfusionCalculatorCard(patientWeight = shift?.weightKg ?: 70.0)
        }

        Spacer(Modifier.height(24.dp))
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
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FormSectionHeader(title = "Cálculo de Diluição de Medicamentos")

            FormOutlinedTextField(
                value = drugName,
                onValueChange = { drugName = it },
                label = "Nome da medicação (opcional)"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FormOutlinedTextField(
                    value = dose,
                    onValueChange = { dose = it },
                    label = "Dose prescrita",
                    unitSuffix = "mg",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                FormOutlinedTextField(
                    value = presentation,
                    onValueChange = { presentation = it },
                    label = "Apresentação",
                    unitSuffix = "mg",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
            }

            FormOutlinedTextField(
                value = volume,
                onValueChange = { volume = it },
                label = "Volume da ampola",
                unitSuffix = "mL",
                keyboardType = KeyboardType.Decimal
            )

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
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FormSectionHeader(title = "Regra de Três Proporcional")

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FormOutlinedTextField(
                    value = available,
                    onValueChange = { available = it },
                    label = "Qtd. disponível (A)",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                FormOutlinedTextField(
                    value = vehicleVolume,
                    onValueChange = { vehicleVolume = it },
                    label = "Volume veículo",
                    unitSuffix = "mL",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
            }

            FormOutlinedTextField(
                value = prescribed,
                onValueChange = { prescribed = it },
                label = "Qtd. prescrita (C)",
                keyboardType = KeyboardType.Decimal
            )

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
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FormSectionHeader(title = "Cálculo de Gotejamento de Soro")

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FormOutlinedTextField(
                    value = volume,
                    onValueChange = { volume = it },
                    label = "Volume total",
                    unitSuffix = "mL",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                FormOutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = "Tempo",
                    unitSuffix = "horas",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FormSectionHeader(title = "Infusão Contínua (Vasoativos)")

            FormOutlinedTextField(
                value = weightStr,
                onValueChange = { weightStr = it },
                label = "Peso do paciente",
                unitSuffix = "kg",
                keyboardType = KeyboardType.Decimal
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FormOutlinedTextField(
                    value = doseStr,
                    onValueChange = { doseStr = it },
                    label = "Dose desejada",
                    unitSuffix = "mcg/kg/min",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                FormOutlinedTextField(
                    value = concStr,
                    onValueChange = { concStr = it },
                    label = "Concentração solução",
                    unitSuffix = "mcg/mL",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
            }

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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium.merge(TabularNumbersTextStyle),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
