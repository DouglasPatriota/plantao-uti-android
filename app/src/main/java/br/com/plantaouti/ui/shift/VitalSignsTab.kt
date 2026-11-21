package br.com.plantaouti.ui.shift

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.plantaouti.data.datastore.VitalThresholds
import br.com.plantaouti.data.local.entity.VitalSignEntity
import br.com.plantaouti.domain.calculator.VitalSignCalculator
import br.com.plantaouti.domain.validator.AlertLevel
import br.com.plantaouti.domain.validator.VitalAlertEvaluator
import br.com.plantaouti.ui.theme.AlertCriticalBgDark
import br.com.plantaouti.ui.theme.AlertCriticalBgLight
import br.com.plantaouti.ui.theme.AlertCriticalTextDark
import br.com.plantaouti.ui.theme.AlertCriticalTextLight
import br.com.plantaouti.ui.theme.AlertWarningBgDark
import br.com.plantaouti.ui.theme.AlertWarningBgLight
import br.com.plantaouti.ui.theme.AlertWarningTextDark
import br.com.plantaouti.ui.theme.AlertWarningTextLight

private enum class TrendMetric(val label: String) {
    PAM("PA / PAM"),
    FC("FC (bpm)"),
    SPO2("SpO₂ (%)"),
    TEMP("Temp (°C)")
}

@Composable
fun VitalSignsTab(viewModel: ShiftWorkspaceViewModel) {
    val vitals by viewModel.vitals.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val thresholds = settings.vitalThresholds

    var showChart by remember { mutableStateOf(false) }
    var selectedMetric by remember { mutableStateOf(TrendMetric.PAM) }
    var editingVital by remember { mutableStateOf<VitalSignEntity?>(null) }
    val hScroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Toque em uma hora para editar", style = MaterialTheme.typography.labelLarge)
            FilterChip(
                selected = showChart,
                onClick = { showChart = !showChart },
                label = { Text(if (showChart) "Ocultar Gráfico" else "Gráfico Tendência") }
            )
        }

        if (showChart) {
            VitalTrendChartCard(
                vitals = vitals,
                metric = selectedMetric,
                onSelectMetric = { selectedMetric = it }
            )
        }

        // Cabeçalho fixo estilo planilha
        Row(
            modifier = Modifier
                .horizontalScroll(hScroll)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            SheetHeaderCell("Hora", 62.dp)
            SheetHeaderCell("PA", 86.dp)
            SheetHeaderCell("PAM", 58.dp)
            SheetHeaderCell("FC", 58.dp)
            SheetHeaderCell("FR", 54.dp)
            SheetHeaderCell("SpO₂", 64.dp)
            SheetHeaderCell("Temp", 64.dp)
            SheetHeaderCell("HGT", 64.dp)
            SheetHeaderCell("Dor", 54.dp)
            SheetHeaderCell("Observações", 160.dp)
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(vitals, key = { it.id }) { row ->
                val pam = row.pam ?: VitalSignCalculator.calculatePamFromString(row.bloodPressure)
                Row(
                    modifier = Modifier
                        .horizontalScroll(hScroll)
                        .clickable { editingVital = row }
                ) {
                    SheetValueCell(row.hourLabel, 62.dp, AlertLevel.NORMAL, bold = true)
                    SheetValueCell(row.bloodPressure.ifBlank { "—" }, 86.dp, VitalAlertEvaluator.evaluatePam(pam, thresholds))
                    SheetValueCell(pam?.toString() ?: "—", 58.dp, VitalAlertEvaluator.evaluatePam(pam, thresholds), bold = true)
                    SheetValueCell(row.heartRate?.toString() ?: "—", 58.dp, VitalAlertEvaluator.evaluateHeartRate(row.heartRate, thresholds))
                    SheetValueCell(row.respRate?.toString() ?: "—", 54.dp, VitalAlertEvaluator.evaluateRespRate(row.respRate, thresholds))
                    SheetValueCell(row.spo2?.let { "$it%" } ?: "—", 64.dp, VitalAlertEvaluator.evaluateSpo2(row.spo2, thresholds))
                    SheetValueCell(row.temperature?.let { "%.1f".format(it) } ?: "—", 64.dp, VitalAlertEvaluator.evaluateTemp(row.temperature, thresholds))
                    SheetValueCell(row.hgt?.toString() ?: "—", 64.dp, VitalAlertEvaluator.evaluateHgt(row.hgt, thresholds))
                    SheetValueCell(row.painScore?.toString() ?: "—", 54.dp, VitalAlertEvaluator.evaluatePain(row.painScore))
                    SheetValueCell(row.notes.ifBlank { "—" }, 160.dp, AlertLevel.NORMAL, alignStart = true)
                }
            }
        }
    }

    editingVital?.let { current ->
        VitalEditDialog(
            vital = current,
            thresholds = thresholds,
            onDismiss = { editingVital = null },
            onSave = { updated ->
                viewModel.updateVital(updated)
                editingVital = null
            }
        )
    }
}

@Composable
private fun SheetHeaderCell(text: String, width: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(44.dp)
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun SheetValueCell(
    text: String,
    width: Dp,
    alertLevel: AlertLevel,
    bold: Boolean = false,
    alignStart: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = when (alertLevel) {
        AlertLevel.NORMAL -> MaterialTheme.colorScheme.surface
        AlertLevel.WARNING -> if (isDark) AlertWarningBgDark else AlertWarningBgLight
        AlertLevel.CRITICAL -> if (isDark) AlertCriticalBgDark else AlertCriticalBgLight
    }
    val txtColor = when (alertLevel) {
        AlertLevel.NORMAL -> MaterialTheme.colorScheme.onSurface
        AlertLevel.WARNING -> if (isDark) AlertWarningTextDark else AlertWarningTextLight
        AlertLevel.CRITICAL -> if (isDark) AlertCriticalTextDark else AlertCriticalTextLight
    }

    Box(
        modifier = Modifier
            .width(width)
            .height(50.dp) // >= 48dp para toque fácil com uma mão
            .background(bgColor)
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            .padding(horizontal = 6.dp),
        contentAlignment = if (alignStart) Alignment.CenterStart else Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold || alertLevel != AlertLevel.NORMAL) FontWeight.SemiBold else FontWeight.Normal,
            color = txtColor,
            maxLines = 1,
            textAlign = if (alignStart) TextAlign.Start else TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VitalEditDialog(
    vital: VitalSignEntity,
    thresholds: VitalThresholds,
    onDismiss: () -> Unit,
    onSave: (VitalSignEntity) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    var pa by remember { mutableStateOf(vital.bloodPressure) }
    var fc by remember { mutableStateOf(vital.heartRate?.toString() ?: "") }
    var fr by remember { mutableStateOf(vital.respRate?.toString() ?: "") }
    var spo2 by remember { mutableStateOf(vital.spo2?.toString() ?: "") }
    var temp by remember { mutableStateOf(vital.temperature?.toString() ?: "") }
    var hgt by remember { mutableStateOf(vital.hgt?.toString() ?: "") }
    var dor by remember { mutableStateOf(vital.painScore?.toString() ?: "") }
    var notes by remember { mutableStateOf(vital.notes) }

    val computedPam = remember(pa) { VitalSignCalculator.calculatePamFromString(pa) }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header do Registro Rápido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            Icon(
                                Icons.Default.MonitorHeart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Registro Rápido", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            AssistChip(
                                onClick = { },
                                label = { Text(vital.hourLabel, fontWeight = FontWeight.Bold) }
                            )
                        }
                        Text("Toque nos atalhos clínicos rápidos ou digite", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar")
                }
            }

            // 1. Pressão Arterial (PA) + PAM Automática
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 6.dp))
                            Text("Pressão Arterial", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            "PAM: ${computedPam?.let { "$it mmHg" } ?: "—"}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    OutlinedTextField(
                        value = pa,
                        onValueChange = { pa = it },
                        label = { Text("PA (120x80)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                        modifier = Modifier.fillMaxWidth()
                    )
                    QuickFillChipRow(
                        options = listOf("120x80", "130x80", "140x90", "100x60", "90x50"),
                        selectedValue = pa,
                        onSelect = { pa = it }
                    )
                }
            }

            // 2. Frequência Cardíaca (FC) & Frequência Respiratória (FR)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumericNextField(fc, { fc = it.filter(Char::isDigit) }, "FC (bpm)", Modifier.weight(1f))
                NumericNextField(fr, { fr = it.filter(Char::isDigit) }, "FR (irpm)", Modifier.weight(1f))
            }
            QuickFillChipRow(
                options = listOf("60", "70", "78", "80", "90", "100", "115"),
                selectedValue = fc,
                onSelect = { fc = it }
            )

            // 3. Saturação (SpO₂) & Temperatura (°C)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumericNextField(spo2, { spo2 = it.filter(Char::isDigit) }, "SpO₂ (%)", Modifier.weight(1f))
                OutlinedTextField(
                    value = temp,
                    onValueChange = { temp = it },
                    label = { Text("Temp (°C)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Atalhos SpO₂", style = MaterialTheme.typography.labelSmall)
                    QuickFillChipRow(
                        options = listOf("95%", "96%", "97%", "98%", "99%"),
                        selectedValue = if (spo2.isNotBlank()) "$spo2%" else "",
                        onSelect = { spo2 = it.replace("%", "") }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Atalhos Temp", style = MaterialTheme.typography.labelSmall)
                    QuickFillChipRow(
                        options = listOf("36,0", "36,5", "36,8", "37,2", "38,0"),
                        selectedValue = temp,
                        onSelect = { temp = it }
                    )
                }
            }

            // 4. HGT & Dor
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumericNextField(hgt, { hgt = it.filter(Char::isDigit) }, "HGT (mg/dL)", Modifier.weight(1f))
                NumericNextField(dor, { dor = it.filter(Char::isDigit).take(2) }, "Dor (0-10)", Modifier.weight(1f))
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Observações") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Rodapé com Ações Sticky
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSave(
                            vital.copy(
                                bloodPressure = pa.trim(),
                                pam = computedPam,
                                heartRate = fc.toIntOrNull(),
                                respRate = fr.toIntOrNull(),
                                spo2 = spo2.toIntOrNull()?.coerceIn(0, 100),
                                temperature = temp.replace(",", ".").toDoubleOrNull(),
                                hgt = hgt.toIntOrNull(),
                                painScore = dor.toIntOrNull()?.coerceIn(0, 10),
                                notes = notes.trim()
                            )
                        )
                    },
                    modifier = Modifier.weight(1.5f).height(48.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text("Salvar Registro")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickFillChipRow(
    options: List<String>,
    selectedValue: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { option ->
            val isSelected = selectedValue.trim().equals(option.trim(), ignoreCase = true)
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(option) },
                label = {
                    Text(
                        option,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.height(48.dp)
            )
        }
    }
}

@Composable
private fun NumericNextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
        modifier = modifier
    )
}

@Composable
private fun VitalTrendChartCard(
    vitals: List<VitalSignEntity>,
    metric: TrendMetric,
    onSelectMetric: (TrendMetric) -> Unit
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    val points: List<Pair<String, Float>> = remember(vitals, metric) {
        vitals.mapNotNull { v ->
            val value: Float? = when (metric) {
                TrendMetric.PAM -> (v.pam ?: VitalSignCalculator.calculatePamFromString(v.bloodPressure))?.toFloat()
                TrendMetric.FC -> v.heartRate?.toFloat()
                TrendMetric.SPO2 -> v.spo2?.toFloat()
                TrendMetric.TEMP -> v.temperature?.toFloat()
            }
            value?.let { v.hourLabel to it }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                TrendMetric.entries.forEach { m ->
                    FilterChip(
                        selected = metric == m,
                        onClick = { onSelectMetric(m) },
                        label = { Text(m.label) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (points.size < 2) {
                Text(
                    "Registre pelo menos 2 horários para visualizar a tendência de ${metric.label}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val minVal = points.minOf { it.second } - 2f
                val maxVal = points.maxOf { it.second } + 2f
                val span = (maxVal - minVal).coerceAtLeast(1f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                    // Linhas horizontais de grade
                    for (i in 0..3) {
                        val y = size.height * i / 3f
                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    }
                    val path = Path()
                    points.forEachIndexed { idx, (_, value) ->
                        val x = idx * stepX
                        val y = size.height - ((value - minVal) / span) * size.height
                        if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        drawCircle(color = lineColor, radius = 5.dp.toPx(), center = Offset(x, y))
                    }
                    drawPath(path = path, color = lineColor, style = Stroke(width = 2.5.dp.toPx()))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${points.first().first}: ${points.first().second}", style = MaterialTheme.typography.labelMedium)
                    Text("Mín: %.1f • Máx: %.1f".format(points.minOf { it.second }, points.maxOf { it.second }), style = MaterialTheme.typography.labelMedium)
                    Text("${points.last().first}: ${points.last().second}", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
