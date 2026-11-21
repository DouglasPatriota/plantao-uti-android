package br.com.plantaouti.ui.patients

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Badge
import br.com.plantaouti.data.local.entity.PatientShiftEntity
import br.com.plantaouti.domain.calculator.MedicalCalculators
import br.com.plantaouti.domain.model.ShiftType
import br.com.plantaouti.pdf.PdfShareAndPrintHelper
import br.com.plantaouti.pdf.ShiftPdfGenerator
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
                            "Leitos e registro de enfermagem",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showBlankPdfDialog = true }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Folha PDF em branco")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Novo Paciente") }
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
                    label = { Text("Em andamento (${shifts.count { !it.isEnded }})") }
                )
                FilterChip(
                    selected = showEnded,
                    onClick = { showEnded = true },
                    label = { Text("Todos (${shifts.size})") }
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
                    Icon(
                        Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Nenhum paciente no plantão", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Toque em \"Novo Paciente\" para iniciar a folha digital do leito.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredShifts, key = { it.id }) { shift ->
                        PatientShiftCard(
                            shift = shift,
                            onClick = { onOpenShift(shift.id) },
                            onEdit = { editingShift = shift },
                            onToggleEnd = {
                                if (shift.isEnded) viewModel.reopenShift(shift)
                                else viewModel.endShift(shift)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        PatientShiftDialog(
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
        PatientShiftDialog(
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
            }
        )
    }

    if (showBlankPdfDialog) {
        AlertDialog(
            onDismissRequest = { showBlankPdfDialog = false },
            title = { Text("Gerar Folha PDF em Branco") },
            text = { Text("Escolha o turno para preencher as horas da folha A4 em branco:") },
            confirmButton = {
                TextButton(onClick = {
                    val file = ShiftPdfGenerator.generateBlankShiftPdf(context, ShiftType.DIURNO)
                    PdfShareAndPrintHelper.sharePdf(context, file)
                    showBlankPdfDialog = false
                }) { Text("Diurno (07h–19h)") }
            },
            dismissButton = {
                TextButton(onClick = {
                    val file = ShiftPdfGenerator.generateBlankShiftPdf(context, ShiftType.NOTURNO)
                    PdfShareAndPrintHelper.sharePdf(context, file)
                    showBlankPdfDialog = false
                }) { Text("Noturno (19h–07h)") }
            }
        )
    }
}

@Composable
private fun PatientShiftCard(
    shift: PatientShiftEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onToggleEnd: () -> Unit
) {
    val age = remember(shift.birthDate) { MedicalCalculators.calculateAge(shift.birthDate) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (shift.isEnded)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        label = { Text("Leito ${shift.bedNumber}", fontWeight = FontWeight.Bold) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        shift.shiftType.shortLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar paciente")
                    }
                    IconButton(onClick = onToggleEnd) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = if (shift.isEnded) "Reabrir plantão" else "Encerrar plantão",
                            tint = if (shift.isEnded) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(shift.patientName, style = MaterialTheme.typography.titleMedium)
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
                Spacer(Modifier.height(4.dp))
                Text(
                    "Plantão encerrado",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun PatientShiftDialog(
    initial: PatientShiftEntity?,
    defaultTechName: String,
    onDismiss: () -> Unit,
    onSave: (name: String, bed: String, weight: Double, date: String, birthDate: String, type: ShiftType, tech: String) -> Unit
) {
    val today = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }
    var name by remember { mutableStateOf(initial?.patientName ?: "") }
    var bed by remember { mutableStateOf(initial?.bedNumber ?: "") }
    var weight by remember { mutableStateOf(initial?.weightKg?.toString() ?: "") }
    var date by remember { mutableStateOf(initial?.shiftDate ?: today) }
    var birthDate by remember { mutableStateOf(initial?.birthDate ?: "") }
    var tech by remember { mutableStateOf(initial?.technicianName ?: defaultTechName) }
    var shiftType by remember { mutableStateOf(initial?.shiftType ?: ShiftType.DIURNO) }

    val computedAge = remember(birthDate) { MedicalCalculators.calculateAge(birthDate) }
    val canSave = name.isNotBlank() && bed.isNotBlank() && (weight.replace(",", ".").toDoubleOrNull() ?: 0.0) > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Novo Paciente no Plantão" else "Editar Paciente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do paciente *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bed,
                        onValueChange = { bed = it },
                        label = { Text("Leito *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Peso (kg) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { birthDate = MedicalCalculators.formatDateMask(it) },
                        label = { Text("Nascimento (dd/mm/aaaa)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.3f)
                    )
                    Card(modifier = Modifier.weight(0.7f)) {
                        Column(
                            modifier = Modifier.padding(8.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Idade", style = MaterialTheme.typography.labelSmall)
                            Text(
                                if (computedAge != null) "$computedAge anos" else "—",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = MedicalCalculators.formatDateMask(it) },
                    label = { Text("Data do plantão (dd/mm/aaaa)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tech,
                    onValueChange = { tech = it },
                    label = { Text("Técnico responsável") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShiftType.entries.forEach { type ->
                        FilterChip(
                            selected = shiftType == type,
                            onClick = { if (initial == null) shiftType = type },
                            label = { Text(type.shortLabel) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    val w = weight.replace(",", ".").toDoubleOrNull() ?: 70.0
                    onSave(name, bed, w, date, birthDate, shiftType, tech)
                }
            ) { Text("Salvar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
