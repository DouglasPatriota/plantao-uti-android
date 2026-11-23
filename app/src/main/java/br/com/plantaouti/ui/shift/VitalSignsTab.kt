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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Bloodtype
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeviceThermostat
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
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
import br.com.plantaouti.ui.components.FormOutlinedTextField
import br.com.plantaouti.ui.components.FormSectionHeader
import br.com.plantaouti.ui.components.FullWidthActionButton
import br.com.plantaouti.ui.components.QuickStepper
import br.com.plantaouti.ui.components.TabularNumbersTextStyle
import br.com.plantaouti.ui.theme.AlertCriticalBgDark
import br.com.plantaouti.ui.theme.AlertCriticalBgLight
import br.com.plantaouti.ui.theme.AlertCriticalTextDark
import br.com.plantaouti.ui.theme.AlertCriticalTextLight
import br.com.plantaouti.ui.theme.AlertWarningBgDark
import br.com.plantaouti.ui.theme.AlertWarningBgLight
import br.com.plantaouti.ui.theme.AlertWarningTextDark
import br.com.plantaouti.ui.theme.AlertWarningTextLight

private enum class VitalViewMode(val label: String) {
    GRID("Grade (Cards)"),
    SHEET("Planilha"),
    TREND("Gráficos")
}

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

    var viewMode by remember { mutableStateOf(VitalViewMode.GRID) }
    var selectedMetric by remember { mutableStateOf(TrendMetric.PAM) }
    var editingVital by remember { mutableStateOf<VitalSignEntity?>(null) }
    val hScroll = rememberScrollState()

    val filledVitals = remember(vitals) {
        vitals.filter {
            it.bloodPressure.isNotBlank() || it.heartRate != null || it.spo2 != null || it.temperature != null
        }
    }
    val latestVital = filledVitals.lastOrNull() ?: vitals.firstOrNull()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Sinais Vitais do Leito",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Toque em qualquer cartão ou horário para editar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                VitalViewMode.entries.forEach { mode ->
                    FilterChip(
                        selected = viewMode == mode,
                        onClick = { viewMode = mode },
                        label = { Text(mode.label, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        when (viewMode) {
            VitalViewMode.GRID -> {
                if (latestVital != null) {
                    VitalSignsGridContent(
                        latest = latestVital,
                        thresholds = thresholds,
                        onEdit = { editingVital = latestVital },
                        allVitals = vitals,
                        onSelectVital = { editingVital = it }
                    )
                }
            }
            VitalViewMode.SHEET -> {
                Column(modifier = Modifier.fillMaxSize()) {
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
            }
            VitalViewMode.TREND -> {
                VitalTrendChartCard(
                    vitals = vitals,
                    metric = selectedMetric,
                    onSelectMetric = { selectedMetric = it }
                )
            }
        }
    }

    editingVital?.let { current ->
        VitalEditBottomSheet(
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

/**
 * Grade de Sinais Vitais em 2 Colunas com Cards Rounded (16.dp), Numeros Tabulares e Destaque de Alerta
 */
@Composable
private fun VitalSignsGridContent(
    latest: VitalSignEntity,
    thresholds: VitalThresholds,
    onEdit: () -> Unit,
    allVitals: List<VitalSignEntity>,
    onSelectVital: (VitalSignEntity) -> Unit
) {
    val pam = latest.pam ?: VitalSignCalculator.calculatePamFromString(latest.bloodPressure)

    val paLevel = VitalAlertEvaluator.evaluatePam(pam, thresholds)
    val fcLevel = VitalAlertEvaluator.evaluateHeartRate(latest.heartRate, thresholds)
    val frLevel = VitalAlertEvaluator.evaluateRespRate(latest.respRate, thresholds)
    val spo2Level = VitalAlertEvaluator.evaluateSpo2(latest.spo2, thresholds)
    val tempLevel = VitalAlertEvaluator.evaluateTemp(latest.temperature, thresholds)
    val hgtLevel = VitalAlertEvaluator.evaluateHgt(latest.hgt, thresholds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Horario Atual
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    "Último Registro: ${latest.hourLabel}h",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            OutlinedButton(
                onClick = onEdit,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Registrar Sinais", fontWeight = FontWeight.Bold)
            }
        }

        // Grid 2 Colunas de Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VitalBlockCard(
                title = "PA / PAM",
                value = latest.bloodPressure.ifBlank { "—" },
                subtitle = pam?.let { "PAM: $it mmHg" } ?: "Sem PAM",
                unit = "mmHg",
                icon = Icons.Rounded.MonitorHeart,
                alertLevel = paLevel,
                onClick = onEdit,
                modifier = Modifier.weight(1f)
            )

            VitalBlockCard(
                title = "Frenq. Cardíaca",
                value = latest.heartRate?.toString() ?: "—",
                subtitle = "Frequência cardíaca",
                unit = "bpm",
                icon = Icons.Rounded.Favorite,
                alertLevel = fcLevel,
                onClick = onEdit,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VitalBlockCard(
                title = "Saturação SpO₂",
                value = latest.spo2?.let { "$it%" } ?: "—",
                subtitle = "Oxigenação sanguínea",
                unit = "%",
                icon = Icons.Rounded.Air,
                alertLevel = spo2Level,
                onClick = onEdit,
                modifier = Modifier.weight(1f)
            )

            VitalBlockCard(
                title = "Temperatura",
                value = latest.temperature?.let { "%.1f".format(it) } ?: "—",
                subtitle = "Temperatura axilar",
                unit = "°C",
                icon = Icons.Rounded.DeviceThermostat,
                alertLevel = tempLevel,
                onClick = onEdit,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VitalBlockCard(
                title = "Freq. Respiratória",
                value = latest.respRate?.toString() ?: "—",
                subtitle = "Frequência respiratória",
                unit = "irpm",
                icon = Icons.Rounded.Air,
                alertLevel = frLevel,
                onClick = onEdit,
                modifier = Modifier.weight(1f)
            )

            VitalBlockCard(
                title = "Glicemia / HGT",
                value = latest.hgt?.toString() ?: "—",
                subtitle = "Glicemia capilar",
                unit = "mg/dL",
                icon = Icons.Rounded.Bloodtype,
                alertLevel = hgtLevel,
                onClick = onEdit,
                modifier = Modifier.weight(1f)
            )
        }

        // Card Escala de Dor
        VitalBlockCard(
            title = "Escala de Dor",
            value = latest.painScore?.let { "$it / 10" } ?: "Sem dor avaliada",
            subtitle = latest.notes.ifBlank { "Toque para adicionar observação" },
            unit = "nível",
            icon = Icons.Rounded.SentimentDissatisfied,
            alertLevel = VitalAlertEvaluator.evaluatePain(latest.painScore),
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // Seletor Rápido de Horários Registrados
        Text(
            "Horários do Plantão (Toque para editar)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allVitals.forEach { item ->
                val hasAlert = VitalAlertEvaluator.hasAnyAlert(item, thresholds)
                FilterChip(
                    selected = item.id == latest.id,
                    onClick = { onSelectVital(item) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${item.hourLabel}h",
                                fontWeight = FontWeight.Bold
                            )
                            if (hasAlert) {
                                Spacer(Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(MaterialTheme.colorScheme.error, CircleShape)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

/**
 * Bloco Visual (Card) de Sinal Vital
 */
@Composable
private fun VitalBlockCard(
    title: String,
    value: String,
    subtitle: String,
    unit: String,
    icon: ImageVector,
    alertLevel: AlertLevel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val borderColor = when (alertLevel) {
        AlertLevel.NORMAL -> MaterialTheme.colorScheme.outline
        AlertLevel.WARNING -> if (isDark) AlertWarningTextDark else AlertWarningTextLight
        AlertLevel.CRITICAL -> if (isDark) AlertCriticalTextDark else AlertCriticalTextLight
    }

    val borderWidth = if (alertLevel != AlertLevel.NORMAL) 2.dp else 1.dp

    val badgeColor = when (alertLevel) {
        AlertLevel.NORMAL -> Color.Transparent
        AlertLevel.WARNING -> if (isDark) AlertWarningBgDark else AlertWarningBgLight
        AlertLevel.CRITICAL -> if (isDark) AlertCriticalBgDark else AlertCriticalBgLight
    }

    val textColor = when (alertLevel) {
        AlertLevel.NORMAL -> MaterialTheme.colorScheme.onSurface
        AlertLevel.WARNING -> if (isDark) AlertWarningTextDark else AlertWarningTextLight
        AlertLevel.CRITICAL -> if (isDark) AlertCriticalTextDark else AlertCriticalTextLight
    }

    Card(
        modifier = modifier
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alertLevel != AlertLevel.NORMAL) badgeColor else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (alertLevel != AlertLevel.NORMAL) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = borderColor
                    ) {
                        Text(
                            if (alertLevel == AlertLevel.CRITICAL) "CRÍTICO" else "ALERTA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Valor com Numeros Tabulares em Destaque
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge.merge(TabularNumbersTextStyle),
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
                if (value != "—" && unit.isNotBlank()) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
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
            .height(50.dp)
            .background(bgColor)
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            .padding(horizontal = 6.dp),
        contentAlignment = if (alignStart) Alignment.CenterStart else Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.merge(TabularNumbersTextStyle),
            fontWeight = if (bold || alertLevel != AlertLevel.NORMAL) FontWeight.Bold else FontWeight.Normal,
            color = txtColor,
            maxLines = 1,
            textAlign = if (alignStart) TextAlign.Start else TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun VitalEditBottomSheet(
    vital: VitalSignEntity,
    thresholds: VitalThresholds,
    onDismiss: () -> Unit,
    onSave: (VitalSignEntity) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                                Icons.Rounded.MonitorHeart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Registrar Sinais — Horário ${vital.hourLabel}h",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Ajuste rápido com botões e valores numéricos",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Fechar")
                }
            }

            // Bloco 1: PA e PAM
            FormSectionHeader(title = "Pressão Arterial & PAM", subtitle = computedPam?.let { "PAM Automática: $it mmHg" } ?: "Digite no formato 120x80")

            FormOutlinedTextField(
                value = pa,
                onValueChange = { pa = it },
                label = "PA (ex.: 120x80)",
                leadingIcon = Icons.Rounded.MonitorHeart,
                unitSuffix = "mmHg"
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("120x80", "130x80", "140x90", "100x60", "90x50", "80x40").forEach { option ->
                    FilterChip(
                        selected = pa.trim() == option,
                        onClick = { pa = option },
                        label = { Text(option, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Bloco 2: Cardio & Respiração
            FormSectionHeader(title = "Frequência Cardíaca & Respiratória")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FormOutlinedTextField(
                        value = fc,
                        onValueChange = { fc = it.filter(Char::isDigit) },
                        label = "Frequência Cardíaca",
                        leadingIcon = Icons.Rounded.Favorite,
                        unitSuffix = "bpm",
                        keyboardType = KeyboardType.Number
                    )
                    Spacer(Modifier.height(4.dp))
                    QuickStepper(
                        onDecrement = {
                            val curr = fc.toIntOrNull() ?: 70
                            fc = (curr - 5).coerceAtLeast(0).toString()
                        },
                        onIncrement = {
                            val curr = fc.toIntOrNull() ?: 70
                            fc = (curr + 5).toString()
                        },
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    FormOutlinedTextField(
                        value = fr,
                        onValueChange = { fr = it.filter(Char::isDigit) },
                        label = "Freq. Respiratória",
                        leadingIcon = Icons.Rounded.Air,
                        unitSuffix = "irpm",
                        keyboardType = KeyboardType.Number
                    )
                    Spacer(Modifier.height(4.dp))
                    QuickStepper(
                        onDecrement = {
                            val curr = fr.toIntOrNull() ?: 16
                            fr = (curr - 2).coerceAtLeast(0).toString()
                        },
                        onIncrement = {
                            val curr = fr.toIntOrNull() ?: 16
                            fr = (curr + 2).toString()
                        },
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            // Bloco 3: Oxigenação & Temperatura
            FormSectionHeader(title = "Saturação (SpO₂) & Temperatura")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FormOutlinedTextField(
                        value = spo2,
                        onValueChange = { spo2 = it.filter(Char::isDigit) },
                        label = "Saturação SpO₂",
                        leadingIcon = Icons.Rounded.Air,
                        unitSuffix = "%",
                        keyboardType = KeyboardType.Number
                    )
                    Spacer(Modifier.height(4.dp))
                    QuickStepper(
                        onDecrement = {
                            val curr = spo2.toIntOrNull() ?: 98
                            spo2 = (curr - 1).coerceAtLeast(0).toString()
                        },
                        onIncrement = {
                            val curr = spo2.toIntOrNull() ?: 98
                            spo2 = (curr + 1).coerceAtMost(100).toString()
                        },
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    FormOutlinedTextField(
                        value = temp,
                        onValueChange = { temp = it },
                        label = "Temperatura",
                        leadingIcon = Icons.Rounded.DeviceThermostat,
                        unitSuffix = "°C",
                        keyboardType = KeyboardType.Decimal
                    )
                    Spacer(Modifier.height(4.dp))
                    QuickStepper(
                        onDecrement = {
                            val curr = temp.replace(",", ".").toDoubleOrNull() ?: 36.5
                            temp = "%.1f".format((curr - 0.2).coerceAtLeast(30.0)).replace(",", ".")
                        },
                        onIncrement = {
                            val curr = temp.replace(",", ".").toDoubleOrNull() ?: 36.5
                            temp = "%.1f".format(curr + 0.2).replace(",", ".")
                        },
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            // Bloco 4: Glicemia (HGT) & Dor
            FormSectionHeader(title = "Glicemia (HGT) & Dor")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FormOutlinedTextField(
                    value = hgt,
                    onValueChange = { hgt = it.filter(Char::isDigit) },
                    label = "Glicemia HGT",
                    leadingIcon = Icons.Rounded.Bloodtype,
                    unitSuffix = "mg/dL",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )

                FormOutlinedTextField(
                    value = dor,
                    onValueChange = { dor = it.filter(Char::isDigit).take(2) },
                    label = "Escala de Dor",
                    leadingIcon = Icons.Rounded.SentimentDissatisfied,
                    unitSuffix = "/ 10",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            FormOutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Observações Clínicas"
            )

            // Botao Principal Fixo
            FullWidthActionButton(
                text = "Salvar Sinais Vitais (${vital.hourLabel}h)",
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
                }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
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
            .padding(14.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                TrendMetric.entries.forEach { m ->
                    FilterChip(
                        selected = metric == m,
                        onClick = { onSelectMetric(m) },
                        label = { Text(m.label, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            if (points.size < 2) {
                Text(
                    "Registre pelo menos 2 horários para visualizar a curva de tendência de ${metric.label}.",
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
                        .height(140.dp)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    val stepX = size.width / (points.size - 1).coerceAtLeast(1)
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
                    Text("${points.first().first}h: ${points.first().second}", style = MaterialTheme.typography.labelMedium)
                    Text("Mín: %.1f • Máx: %.1f".format(points.minOf { it.second }, points.maxOf { it.second }), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text("${points.last().first}h: ${points.last().second}", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
