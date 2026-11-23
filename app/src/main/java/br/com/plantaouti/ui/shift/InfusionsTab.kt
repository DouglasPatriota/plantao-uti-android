package br.com.plantaouti.ui.shift

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.plantaouti.data.local.entity.DrugCatalogEntity
import br.com.plantaouti.data.local.entity.InfusionEntity
import br.com.plantaouti.domain.calculator.InfusionTimeCalculator
import br.com.plantaouti.domain.validator.DoseRangeValidator
import br.com.plantaouti.ui.components.FormOutlinedTextField
import br.com.plantaouti.ui.components.FormSectionHeader
import br.com.plantaouti.ui.components.FullWidthActionButton
import br.com.plantaouti.ui.components.SelectableOptionCard
import br.com.plantaouti.ui.components.TabularNumbersTextStyle
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun InfusionsTab(viewModel: ShiftWorkspaceViewModel) {
    val infusions by viewModel.infusions.collectAsStateWithLifecycle()
    val catalog by viewModel.drugCatalog.collectAsStateWithLifecycle()

    var showAddSheet by remember { mutableStateOf(false) }
    var editingInfusion by remember { mutableStateOf<InfusionEntity?>(null) }
    var historyInfusion by remember { mutableStateOf<InfusionEntity?>(null) }

    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (infusions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Medication,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Nenhuma infusão contínua registrada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Toque em \"Nova Droga\" para selecionar medicamentos de alta vigilância ou de infusão contínua.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(infusions, key = { it.id }) { infusion ->
                    InfusionCard(
                        infusion = infusion,
                        nowMillis = nowMillis,
                        onEdit = { editingInfusion = infusion },
                        onShowHistory = { historyInfusion = infusion },
                        onToggleActive = { viewModel.toggleInfusionActive(infusion, !infusion.isActive) }
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showAddSheet = true },
            icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
            text = { Text("Nova Droga", fontWeight = FontWeight.Bold) },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

    if (showAddSheet) {
        InfusionFormBottomSheet(
            catalog = catalog,
            existing = null,
            onDismiss = { showAddSheet = false },
            onConfirm = { name, unit, refLabel, minD, maxD, dose, rate, sh, sm, dh, dm, alarm, _ ->
                viewModel.addInfusion(name, unit, refLabel, minD, maxD, dose, rate, sh, sm, dh, dm, alarm)
                showAddSheet = false
            }
        )
    }

    editingInfusion?.let { current ->
        InfusionFormBottomSheet(
            catalog = catalog,
            existing = current,
            onDismiss = { editingInfusion = null },
            onConfirm = { _, _, _, _, _, dose, rate, sh, sm, dh, dm, alarm, note ->
                viewModel.updateInfusionDoseOrRate(current, dose, rate, sh, sm, dh, dm, alarm, note)
                editingInfusion = null
            }
        )
    }

    historyInfusion?.let { current ->
        InfusionHistoryDialog(
            infusion = current,
            viewModel = viewModel,
            onDismiss = { historyInfusion = null }
        )
    }
}

@Composable
private fun InfusionCard(
    infusion: InfusionEntity,
    nowMillis: Long,
    onEdit: () -> Unit,
    onShowHistory: () -> Unit,
    onToggleActive: () -> Unit
) {
    val remaining = infusion.endEpochMillis - nowMillis
    val remainingText = if (infusion.isActive) {
        InfusionTimeCalculator.formatRemainingMillis(remaining)
    } else "Pausada/Encerrada"

    val isUrgent = infusion.isActive && remaining in 1..(15 * 60_000L)
    val doseCheck = DoseRangeValidator.validate(
        infusion.currentDose,
        infusion.minDose,
        infusion.maxDose,
        infusion.unit,
        infusion.referenceRangeLabel
    )

    val borderColor = when {
        isUrgent || doseCheck.isOutOfRange -> MaterialTheme.colorScheme.error
        !infusion.isActive -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isUrgent || doseCheck.isOutOfRange) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUrgent)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Medication,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(infusion.drugName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Ref.: ${infusion.referenceRangeLabel.ifBlank { "Não definida" }} (${infusion.unit})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = onShowHistory) {
                        Icon(Icons.Rounded.History, contentDescription = "Histórico")
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Alterar dose/taxa")
                    }
                    IconButton(onClick = onToggleActive) {
                        Icon(
                            if (infusion.isActive) Icons.Rounded.PauseCircle else Icons.Rounded.PlayCircle,
                            contentDescription = "Pausar/Retomar",
                            tint = if (infusion.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Dose Atual", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${infusion.currentDose} ${infusion.unit}",
                        style = MaterialTheme.typography.titleLarge.merge(TabularNumbersTextStyle),
                        fontWeight = FontWeight.Bold,
                        color = if (doseCheck.isOutOfRange) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Vazão da Bomba", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${infusion.currentRateMlH} mL/h",
                        style = MaterialTheme.typography.titleLarge.merge(TabularNumbersTextStyle),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (doseCheck.isOutOfRange) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Dose fora da faixa de referência",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            val startStr = "%02d:%02d".format(infusion.startHour, infusion.startMinute)
            val endStr = if (infusion.crossedMidnight) "${infusion.predictedEndLabel} (+1d)" else infusion.predictedEndLabel

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Início $startStr • Término $endStr", style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (infusion.alarmEnabled && infusion.isActive) {
                        Icon(Icons.Rounded.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        remainingText,
                        style = MaterialTheme.typography.titleSmall.merge(TabularNumbersTextStyle),
                        fontWeight = FontWeight.Bold,
                        color = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun InfusionFormBottomSheet(
    catalog: List<DrugCatalogEntity>,
    existing: InfusionEntity?,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        unit: String,
        refLabel: String,
        minD: Double?,
        maxD: Double?,
        dose: String,
        rate: String,
        startH: Int,
        startM: Int,
        durH: Int,
        durM: Int,
        alarm: Boolean,
        note: String
    ) -> Unit
) {
    val nowTime = remember { LocalTime.now() }
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isManualDrug by remember { mutableStateOf(false) }
    var selectedDrug by remember { mutableStateOf<DrugCatalogEntity?>(catalog.firstOrNull()) }

    var manualName by remember { mutableStateOf(existing?.drugName ?: "") }
    var manualUnit by remember { mutableStateOf(existing?.unit ?: "mcg/kg/min") }
    var dose by remember { mutableStateOf(existing?.currentDose ?: "") }
    var rate by remember { mutableStateOf(existing?.currentRateMlH ?: "") }
    var startHour by remember { mutableStateOf((existing?.startHour ?: nowTime.hour).toString()) }
    var startMin by remember { mutableStateOf((existing?.startMinute ?: nowTime.minute).toString().padStart(2, '0')) }
    var durHour by remember { mutableStateOf((existing?.durationHours ?: 6).toString()) }
    var durMin by remember { mutableStateOf((existing?.durationMinutes ?: 0).toString().padStart(2, '0')) }
    var alarmEnabled by remember { mutableStateOf(existing?.alarmEnabled ?: true) }
    var changeNote by remember { mutableStateOf("") }
    var outOfRangeWarning by remember { mutableStateOf<String?>(null) }

    val activeName = existing?.drugName ?: if (isManualDrug) manualName else (selectedDrug?.name ?: "")
    val activeUnit = existing?.unit ?: if (isManualDrug) manualUnit else (selectedDrug?.unit ?: "")
    val activeRef = existing?.referenceRangeLabel ?: if (isManualDrug) "" else (selectedDrug?.referenceRangeLabel ?: "")
    val activeMin = existing?.minDose ?: if (isManualDrug) null else selectedDrug?.minDose
    val activeMax = existing?.maxDose ?: if (isManualDrug) null else selectedDrug?.maxDose

    val sH = startHour.toIntOrNull()?.coerceIn(0, 23) ?: nowTime.hour
    val sM = startMin.toIntOrNull()?.coerceIn(0, 59) ?: nowTime.minute
    val dH = durHour.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val dM = durMin.toIntOrNull()?.coerceIn(0, 59) ?: 0
    val previewEnd = remember(sH, sM, dH, dM) {
        InfusionTimeCalculator.calculatePredictedEnd(sH, sM, dH, dM)
    }

    val canSubmit = activeName.isNotBlank() && activeUnit.isNotBlank() && dose.isNotBlank() && rate.isNotBlank()

    fun submitConfirmed() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onConfirm(activeName, activeUnit, activeRef, activeMin, activeMax, dose, rate, sH, sM, dH, dM, alarmEnabled, changeNote)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Medication,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            if (existing == null) "Nova Infusão Contínua" else "Alterar ${existing.drugName}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Medicamento de Alta Vigilância (MAV)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Fechar")
                }
            }

            // Seção 1: Seleção da Droga
            if (existing == null) {
                FormSectionHeader(title = "Seleção do Medicamento")

                if (!isManualDrug) {
                    Text("Escolha a droga pré-cadastrada:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        catalog.forEach { drug ->
                            SelectableOptionCard(
                                title = drug.name,
                                subtitle = "Unidade: ${drug.unit}",
                                icon = Icons.Rounded.Favorite,
                                isSelected = selectedDrug?.id == drug.id,
                                onClick = { selectedDrug = drug },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    FormOutlinedTextField(
                        value = manualName,
                        onValueChange = { manualName = it },
                        label = "Nome da Droga / Medicamento *",
                        leadingIcon = Icons.Rounded.Medication
                    )
                    FormOutlinedTextField(
                        value = manualUnit,
                        onValueChange = { manualUnit = it },
                        label = "Unidade de Medida (ex.: mcg/kg/min) *"
                    )
                }

                TextButton(onClick = { isManualDrug = !isManualDrug }) {
                    Text(
                        if (isManualDrug) "← Escolher da lista pré-cadastrada" else "+ Digitar outra droga manualmente",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Seção 2: Concentração e Dose
            FormSectionHeader(
                title = "Parâmetros de Infusão",
                subtitle = if (activeRef.isNotBlank()) "Faixa de Referência: $activeRef ($activeUnit)" else null
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FormOutlinedTextField(
                    value = dose,
                    onValueChange = { dose = it },
                    label = "Dose Prescrita *",
                    leadingIcon = Icons.Rounded.Favorite,
                    unitSuffix = activeUnit,
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )

                FormOutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = "Vazão da Bomba *",
                    leadingIcon = Icons.Rounded.Schedule,
                    unitSuffix = "mL/h",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
            }

            // Chips de Ajuste Rápido de Dose
            Text("Atalhos de Ajuste Rápido de Dose:", style = MaterialTheme.typography.labelSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("+0.01", "+0.05", "+0.1", "+0.5", "+1.0", "+5.0").forEach { step ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            val curr = dose.replace(",", ".").toDoubleOrNull() ?: 0.0
                            val add = step.replace("+", "").toDoubleOrNull() ?: 0.0
                            dose = "%.2f".format(curr + add).replace(",", ".")
                        },
                        label = { Text(step, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Seção 3: Horários & Alarme
            FormSectionHeader(title = "Horário & Duração Estimada")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FormOutlinedTextField(
                    value = startHour,
                    onValueChange = { startHour = it.filter(Char::isDigit).take(2) },
                    label = "Hora Início (0-23)",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                FormOutlinedTextField(
                    value = startMin,
                    onValueChange = { startMin = it.filter(Char::isDigit).take(2) },
                    label = "Minuto Início (0-59)",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FormOutlinedTextField(
                    value = durHour,
                    onValueChange = { durHour = it.filter(Char::isDigit).take(2) },
                    label = "Duração (Horas)",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                FormOutlinedTextField(
                    value = durMin,
                    onValueChange = { durMin = it.filter(Char::isDigit).take(2) },
                    label = "Duração (Minutos)",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                val suffix = if (previewEnd.crossedMidnight) " (+1 dia)" else ""
                Text(
                    "⏱ Término Previsto: ${previewEnd.endTimeFormatted}$suffix",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(14.dp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ativar alarme automático de término", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Switch(checked = alarmEnabled, onCheckedChange = { alarmEnabled = it })
            }

            if (existing != null) {
                FormOutlinedTextField(
                    value = changeNote,
                    onValueChange = { changeNote = it },
                    label = "Motivo / Observação da alteração"
                )
            }

            // Botao Principal Fixo
            FullWidthActionButton(
                text = if (existing == null) "Iniciar Infusão" else "Salvar Alterações da Infusão",
                enabled = canSubmit,
                onClick = {
                    val check = DoseRangeValidator.validate(dose, activeMin, activeMax, activeUnit, activeRef)
                    if (check.isOutOfRange) {
                        outOfRangeWarning = check.message
                    } else {
                        submitConfirmed()
                    }
                }
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    outOfRangeWarning?.let { msg ->
        AlertDialog(
            onDismissRequest = { outOfRangeWarning = null },
            icon = { Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Dose fora da faixa de referência", fontWeight = FontWeight.Bold) },
            text = {
                Text("""$msg

O aplicativo apenas registra o valor informado pelo profissional. Confirma o registro desta dose?""")
            },
            confirmButton = {
                TextButton(onClick = {
                    outOfRangeWarning = null
                    submitConfirmed()
                }) { Text("Confirmar e Salvar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                OutlinedButton(onClick = { outOfRangeWarning = null }) { Text("Revisar") }
            }
        )
    }
}

@Composable
private fun InfusionHistoryDialog(
    infusion: InfusionEntity,
    viewModel: ShiftWorkspaceViewModel,
    onDismiss: () -> Unit
) {
    val history by viewModel.observeHistory(infusion.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM HH:mm").withZone(ZoneId.systemDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Histórico — ${infusion.drugName}", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(history, key = { it.id }) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                formatter.format(Instant.ofEpochMilli(entry.recordedAtMillis)),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Dose: ${entry.dose} ${entry.unit} • Taxa: ${entry.rateMlH} mL/h",
                                style = MaterialTheme.typography.titleMedium.merge(TabularNumbersTextStyle),
                                fontWeight = FontWeight.Bold
                            )
                            if (entry.note.isNotBlank()) {
                                Text(entry.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fechar", fontWeight = FontWeight.Bold) }
        }
    )
}
