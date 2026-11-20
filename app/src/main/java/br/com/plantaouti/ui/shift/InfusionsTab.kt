package br.com.plantaouti.ui.shift

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun InfusionsTab(viewModel: ShiftWorkspaceViewModel) {
    val infusions by viewModel.infusions.collectAsStateWithLifecycle()
    val catalog by viewModel.drugCatalog.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingInfusion by remember { mutableStateOf<InfusionEntity?>(null) }
    var historyInfusion by remember { mutableStateOf<InfusionEntity?>(null) }

    // Ticker a cada 30 segundos para atualizar contagem regressiva na tela
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
                Text("Nenhuma infusão contínua registrada", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Toque em \"Nova Droga\" para selecionar da lista pré-cadastrada ou informar outra droga.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
            onClick = { showAddDialog = true },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Nova Droga") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

    if (showAddDialog) {
        InfusionFormDialog(
            catalog = catalog,
            existing = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, unit, refLabel, minD, maxD, dose, rate, sh, sm, dh, dm, alarm, _ ->
                viewModel.addInfusion(name, unit, refLabel, minD, maxD, dose, rate, sh, sm, dh, dm, alarm)
                showAddDialog = false
            }
        )
    }

    editingInfusion?.let { current ->
        InfusionFormDialog(
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isUrgent)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(infusion.drugName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Ref.: ${infusion.referenceRangeLabel.ifBlank { "Não definida" }} (${infusion.unit})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = onShowHistory) {
                        Icon(Icons.Default.History, contentDescription = "Histórico de alterações")
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Alterar dose/taxa")
                    }
                    IconButton(onClick = onToggleActive) {
                        Icon(
                            if (infusion.isActive) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = "Pausar/Retomar"
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Dose: ${infusion.currentDose} ${infusion.unit}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (doseCheck.isOutOfRange) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Text(
                    "Taxa: ${infusion.currentRateMlH} mL/h",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (doseCheck.isOutOfRange) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Dose fora da faixa de referência",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
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
                        Icon(Icons.Default.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        remainingText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfusionFormDialog(
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

    var expanded by remember { mutableStateOf(false) }
    var isManualDrug by remember { mutableStateOf(false) }
    var selectedDrug by remember { mutableStateOf<DrugCatalogEntity?>(catalog.firstOrNull()) }

    var manualName by remember { mutableStateOf(existing?.drugName ?: "") }
    var manualUnit by remember { mutableStateOf(existing?.unit ?: "") }
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

    fun submitConfirmed() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onConfirm(activeName, activeUnit, activeRef, activeMin, activeMax, dose, rate, sH, sM, dH, dM, alarmEnabled, changeNote)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Nova Infusão Contínua" else "Alterar ${existing.drugName}") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (existing == null) {
                    if (!isManualDrug) {
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(
                                value = selectedDrug?.name ?: "Selecione a droga",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Droga pré-cadastrada") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                catalog.forEach { drug ->
                                    DropdownMenuItem(
                                        text = { Text("${drug.name} (${drug.unit})") },
                                        onClick = {
                                            selectedDrug = drug
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = manualName,
                            onValueChange = { manualName = it },
                            label = { Text("Nome da droga *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = manualUnit,
                            onValueChange = { manualUnit = it },
                            label = { Text("Unidade (ex.: mcg/kg/min) *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    TextButton(onClick = { isManualDrug = !isManualDrug }) {
                        Text(if (isManualDrug) "Escolher da lista pré-cadastrada" else "Adicionar outra droga manualmente")
                    }
                }

                // Dose com faixa de referência ao lado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = dose,
                        onValueChange = { dose = it },
                        label = { Text("Dose ($activeUnit) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    if (activeRef.isNotBlank()) {
                        Card(modifier = Modifier.weight(0.95f)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Faixa de ref.", style = MaterialTheme.typography.labelSmall)
                                Text(activeRef, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Taxa da bomba (mL/h) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Hora de início (HH:mm)", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startHour,
                        onValueChange = { startHour = it.filter(Char::isDigit).take(2) },
                        label = { Text("Hora") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = startMin,
                        onValueChange = { startMin = it.filter(Char::isDigit).take(2) },
                        label = { Text("Min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Tempo para acabar (h:min)", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = durHour,
                        onValueChange = { durHour = it.filter(Char::isDigit).take(2) },
                        label = { Text("Horas") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = durMin,
                        onValueChange = { durMin = it.filter(Char::isDigit).take(2) },
                        label = { Text("Min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val suffix = if (previewEnd.crossedMidnight) " (dia seguinte)" else ""
                    Text(
                        "Término previsto: ${previewEnd.endTimeFormatted}$suffix",
                        modifier = Modifier.padding(10.dp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ativar alarme antes do término")
                    Switch(checked = alarmEnabled, onCheckedChange = { alarmEnabled = it })
                }

                if (existing != null) {
                    OutlinedTextField(
                        value = changeNote,
                        onValueChange = { changeNote = it },
                        label = { Text("Motivo/obs. da alteração (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = activeName.isNotBlank() && activeUnit.isNotBlank() && dose.isNotBlank() && rate.isNotBlank(),
                onClick = {
                    val check = DoseRangeValidator.validate(dose, activeMin, activeMax, activeUnit, activeRef)
                    if (check.isOutOfRange) {
                        outOfRangeWarning = check.message
                    } else {
                        submitConfirmed()
                    }
                }
            ) { Text("Salvar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    // Aviso claro pedindo confirmação se dose fora da faixa (nunca bloqueia)
    outOfRangeWarning?.let { msg ->
        AlertDialog(
            onDismissRequest = { outOfRangeWarning = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Dose fora da faixa de referência") },
            text = {
                Text("""$msg

O aplicativo apenas registra o valor informado pelo profissional. Confirma o registro desta dose?""")
            },
            confirmButton = {
                TextButton(onClick = {
                    outOfRangeWarning = null
                    submitConfirmed()
                }) { Text("Confirmar e Salvar") }
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
        title = { Text("Histórico — ${infusion.drugName}") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(history, key = { it.id }) { entry ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                formatter.format(Instant.ofEpochMilli(entry.recordedAtMillis)),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Dose: ${entry.dose} ${entry.unit} • Taxa: ${entry.rateMlH} mL/h",
                                fontWeight = FontWeight.SemiBold
                            )
                            if (entry.note.isNotBlank()) {
                                Text(entry.note, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    )
}
