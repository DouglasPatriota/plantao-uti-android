package br.com.plantaouti.ui.patients

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Scale
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SingleBed
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.plantaouti.data.local.entity.PatientShiftEntity
import br.com.plantaouti.domain.calculator.MedicalCalculators
import br.com.plantaouti.domain.model.ShiftType
import br.com.plantaouti.pdf.PdfShareAndPrintHelper
import br.com.plantaouti.pdf.ShiftPdfGenerator
import br.com.plantaouti.ui.components.FormOutlinedTextField
import br.com.plantaouti.ui.components.FormSectionHeader
import br.com.plantaouti.ui.components.FullWidthActionButton
import br.com.plantaouti.ui.components.QuickStepper
import br.com.plantaouti.ui.components.SelectableOptionCard
import br.com.plantaouti.ui.components.ThemeToggleButton
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(
    onOpenShift: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: PatientsViewModel = hiltViewModel()
) {
    val shifts by viewModel.shifts.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showEnded by remember { mutableStateOf(false) }
    var editingShift by remember { mutableStateOf<PatientShiftEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showBlankPdfDialog by remember { mutableStateOf(false) }
    var shiftToDelete by remember { mutableStateOf<PatientShiftEntity?>(null) }

    val filteredShifts = remember(shifts, showEnded) {
        if (showEnded) shifts else shifts.filter { !it.isEnded }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Plantão UTI", fontWeight = FontWeight.Bold)
                        Text(
                            "Leitos e registros de enfermagem",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    ThemeToggleButton(
                        currentThemeMode = settings.themeMode,
                        onToggle = { newMode -> viewModel.setThemeMode(newMode) }
                    )
                    IconButton(onClick = { showBlankPdfDialog = true }) {
                        Icon(Icons.Rounded.PictureAsPdf, contentDescription = "Folha PDF em branco")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Configurações")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Novo Paciente", fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !showEnded,
                    onClick = { showEnded = false },
                    label = { Text("Em andamento (${shifts.count { !it.isEnded }})", fontWeight = FontWeight.SemiBold) },
                    shape = RoundedCornerShape(12.dp)
                )
                FilterChip(
                    selected = showEnded,
                    onClick = { showEnded = true },
                    label = { Text("Todos (${shifts.size})", fontWeight = FontWeight.SemiBold) },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (filteredShifts.isEmpty()) {
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
                                Icons.Rounded.LocalHospital,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Nenhum paciente no plantão", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Toque em \"Novo Paciente\" para iniciar o registro digital do leito.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredShifts, key = { it.id }) { shift ->
                        PatientShiftCard(
                            shift = shift,
                            onClick = { onOpenShift(shift.id) },
                            onEdit = { editingShift = shift },
                            onToggleEnd = {
                                if (shift.isEnded) viewModel.reopenShift(shift)
                                else viewModel.endShift(shift)
                            },
                            onDelete = { shiftToDelete = shift }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        PatientShiftBottomSheet(
            initial = null,
            defaultTechName = settings.defaultTechnicianName,
            onDismiss = { showCreateDialog = false },
            onSave = { name, bed, weight, date, birth, type, tech ->
                showCreateDialog = false
                viewModel.createShift(name, bed, weight, date, birth, type, tech) { newId ->
                    onOpenShift(newId)
                }
            }
        )
    }

    editingShift?.let { current ->
        PatientShiftBottomSheet(
            initial = current,
            defaultTechName = settings.defaultTechnicianName,
            onDismiss = { editingShift = null },
            onSave = { name, bed, weight, date, birth, type, tech ->
                viewModel.updateShift(
                    current.copy(
                        patientName = name,
                        bedNumber = bed,
                        weightKg = weight,
                        shiftDate = date,
                        birthDate = birth,
                        shiftType = type,
                        technicianName = tech
                    )
                )
                editingShift = null
            },
            onDelete = {
                editingShift = null
                shiftToDelete = current
            }
        )
    }

    shiftToDelete?.let { current ->
        AlertDialog(
            onDismissRequest = { shiftToDelete = null },
            title = { Text("Excluir Paciente", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja realmente excluir o paciente ${current.patientName} (Leito ${current.bedNumber})? Esta ação é irreversível e apagará todos os dados vinculados a este plantão.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteShift(current)
                        shiftToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Excluir", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { shiftToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    if (showBlankPdfDialog) {
        AlertDialog(
            onDismissRequest = { showBlankPdfDialog = false },
            title = { Text("Gerar Folha PDF em Branco", fontWeight = FontWeight.Bold) },
            text = { Text("Escolha o turno para preencher as horas da folha A4 em branco:") },
            confirmButton = {
                TextButton(onClick = {
                    val file = ShiftPdfGenerator.generateBlankShiftPdf(context, ShiftType.DIURNO)
                    PdfShareAndPrintHelper.sharePdf(context, file)
                    showBlankPdfDialog = false
                }) { Text("Diurno (07h–19h)", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = {
                    val file = ShiftPdfGenerator.generateBlankShiftPdf(context, ShiftType.NOTURNO)
                    PdfShareAndPrintHelper.sharePdf(context, file)
                    showBlankPdfDialog = false
                }) { Text("Noturno (19h–07h)", fontWeight = FontWeight.Bold) }
            }
        )
    }
}

@Composable
private fun PatientShiftCard(
    shift: PatientShiftEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onToggleEnd: () -> Unit,
    onDelete: () -> Unit
) {
    val age = remember(shift.birthDate) { MedicalCalculators.calculateAge(shift.birthDate) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (shift.isEnded)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = onClick,
                        label = { Text("Leito ${shift.bedNumber}", fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        shift.shiftType.shortLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Excluir paciente",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Editar paciente")
                    }
                    IconButton(onClick = onToggleEnd) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = if (shift.isEnded) "Reabrir plantão" else "Encerrar plantão",
                            tint = if (shift.isEnded) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(shift.patientName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            val ageInfo = if (age != null) " • $age anos" else ""
            Text(
                "Peso: %.1f kg%s • Data: %s • Téc.: %s".format(
                    shift.weightKg, ageInfo, shift.shiftDate, shift.technicianName
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (shift.isEnded) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "✓ Plantão encerrado",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PatientShiftBottomSheet(
    initial: PatientShiftEntity?,
    defaultTechName: String,
    onDismiss: () -> Unit,
    onSave: (name: String, bed: String, weight: Double, date: String, birthDate: String, type: ShiftType, tech: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val today = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }

    var name by remember { mutableStateOf(initial?.patientName ?: "") }
    var bed by remember { mutableStateOf(initial?.bedNumber ?: "") }
    var weightStr by remember { mutableStateOf(initial?.weightKg?.toString() ?: "70.0") }
    var date by remember { mutableStateOf(initial?.shiftDate ?: today) }
    var birthDateState by remember { mutableStateOf(TextFieldValue(initial?.birthDate ?: "")) }
    val birthDate = birthDateState.text
    var tech by remember { mutableStateOf(initial?.technicianName ?: defaultTechName) }
    var shiftType by remember { mutableStateOf(initial?.shiftType ?: ShiftType.DIURNO) }

    val currentWeightDouble = weightStr.replace(",", ".").toDoubleOrNull() ?: 70.0
    val computedAge = remember(birthDate) { MedicalCalculators.calculateAge(birthDate) }
    val canSave = name.isNotBlank() && bed.isNotBlank() && currentWeightDouble > 0.0

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.PersonAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        if (initial == null) "Novo Paciente / Leito" else "Editar Paciente",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Identificação e dados do plantão",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Bloco 1: Dados do Paciente
            FormSectionHeader(title = "Dados Básicos do Paciente")

            FormOutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nome completo do paciente *",
                leadingIcon = Icons.Rounded.Person
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FormOutlinedTextField(
                    value = bed,
                    onValueChange = { bed = it },
                    label = "Leito UTI *",
                    leadingIcon = Icons.Rounded.SingleBed,
                    modifier = Modifier.weight(1f)
                )

                Column(modifier = Modifier.weight(1.2f)) {
                    FormOutlinedTextField(
                        value = weightStr,
                        onValueChange = { weightStr = it },
                        label = "Peso *",
                        leadingIcon = Icons.Rounded.Scale,
                        unitSuffix = "kg",
                        keyboardType = KeyboardType.Decimal
                    )
                    Spacer(Modifier.height(4.dp))
                    QuickStepper(
                        onDecrement = {
                            val w = (currentWeightDouble - 1.0).coerceAtLeast(1.0)
                            weightStr = "%.1f".format(w).replace(",", ".")
                        },
                        onIncrement = {
                            val w = currentWeightDouble + 1.0
                            weightStr = "%.1f".format(w).replace(",", ".")
                        },
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FormOutlinedTextField(
                    value = birthDateState,
                    onValueChange = { birthDateState = formatBirthDate(it) },
                    label = "Data Nascimento",
                    leadingIcon = Icons.Rounded.Cake,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1.3f)
                )

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(56.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Idade Calculada",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            if (computedAge != null) "$computedAge anos" else "—",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Bloco 2: Configuração do Turno
            FormSectionHeader(title = "Configuração do Plantão")

            Text("Tipo de Turno *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SelectableOptionCard(
                    title = "Diurno (07h–19h)",
                    subtitle = "Plantão 12h Dia",
                    icon = Icons.Rounded.WbSunny,
                    isSelected = shiftType == ShiftType.DIURNO,
                    onClick = { if (initial == null) shiftType = ShiftType.DIURNO },
                    modifier = Modifier.weight(1f)
                )

                SelectableOptionCard(
                    title = "Noturno (19h–07h)",
                    subtitle = "Plantão 12h Noite",
                    icon = Icons.Rounded.Bedtime,
                    isSelected = shiftType == ShiftType.NOTURNO,
                    onClick = { if (initial == null) shiftType = ShiftType.NOTURNO },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FormOutlinedTextField(
                    value = date,
                    onValueChange = { date = MedicalCalculators.formatDateMask(it) },
                    label = "Data do Plantão",
                    leadingIcon = Icons.Rounded.CalendarToday,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )

                FormOutlinedTextField(
                    value = tech,
                    onValueChange = { tech = it },
                    label = "Técnico Responsável",
                    leadingIcon = Icons.Rounded.SupportAgent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Botao Principal Fixo
            FullWidthActionButton(
                text = if (initial == null) "Iniciar Plantão do Leito" else "Salvar Alterações",
                enabled = canSave,
                onClick = {
                    onSave(name, bed, currentWeightDouble, date, birthDate, shiftType, tech)
                }
            )

            if (initial != null && onDelete != null) {
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Excluir Paciente do Plantão", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun formatBirthDate(currentVal: TextFieldValue): TextFieldValue {
    val text = currentVal.text
    val selection = currentVal.selection
    
    var digitsBeforeCursor = 0
    for (i in 0 until minOf(selection.start, text.length)) {
        if (text[i].isDigit()) {
            digitsBeforeCursor++
        }
    }
    
    val digits = text.filter { it.isDigit() }.take(8)
    val formatted = StringBuilder()
    for (i in digits.indices) {
        if (i == 2 || i == 4) {
            formatted.append('/')
        }
        formatted.append(digits[i])
    }
    val formattedStr = formatted.toString()
    
    val newCursorPos = when {
        digitsBeforeCursor <= 2 -> digitsBeforeCursor
        digitsBeforeCursor <= 4 -> digitsBeforeCursor + 1
        else -> digitsBeforeCursor + 2
    }.coerceAtMost(formattedStr.length)
    
    return TextFieldValue(
        text = formattedStr,
        selection = TextRange(newCursorPos)
    )
}
