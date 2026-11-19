package br.com.plantaouti.ui.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import br.com.plantaouti.data.datastore.AppThemeMode
import br.com.plantaouti.data.local.entity.DrugCatalogEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val drugCatalog by viewModel.drugCatalog.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var techName by remember(settings.defaultTechnicianName) { mutableStateOf(settings.defaultTechnicianName) }
    var alarm1 by remember(settings.alarmMinutesBeforeFirst) { mutableStateOf(settings.alarmMinutesBeforeFirst.toString()) }
    var alarm2 by remember(settings.alarmMinutesBeforeSecond) { mutableStateOf(settings.alarmMinutesBeforeSecond.toString()) }
    var pinInput by remember { mutableStateOf("") }
    var showEraseConfirm by remember { mutableStateOf(false) }
    var showDrugDialog by remember { mutableStateOf<DrugCatalogEntity?>(null) }
    var showNewDrugDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                title = { Text("Configurações & LGPD") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Aparência
            SectionTitle("Aparência e Plantão")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = settings.themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                        label = { Text(mode.label.substringBefore(" ")) }
                    )
                }
            }
            OutlinedTextField(
                value = techName,
                onValueChange = {
                    techName = it
                    viewModel.setTechnicianName(it)
                },
                label = { Text("Nome padrão do técnico") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            // 2. Alarmes de Infusão
            SectionTitle("Alarmes de Fim de Infusão")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = alarm1,
                    onValueChange = { alarm1 = it.filter(Char::isDigit).take(3) },
                    label = { Text("1º aviso (min antes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = alarm2,
                    onValueChange = { alarm2 = it.filter(Char::isDigit).take(2) },
                    label = { Text("2º aviso (min antes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    viewModel.setAlarmMinutes(alarm1.toIntOrNull() ?: 30, alarm2.toIntOrNull() ?: 10)
                }) { Text("Salvar") }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !viewModel.alarmScheduler.canScheduleExactAlarms()) {
                OutlinedButton(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Permitir Alarmes Exatos (Android 12+)")
                }
            }

            HorizontalDivider()

            // 3. Tabela Editável de Drogas Vasoativas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("Catálogo de Drogas (${drugCatalog.size})")
                Row {
                    IconButton(onClick = { showNewDrugDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar droga")
                    }
                    IconButton(onClick = { viewModel.restoreDefaultDrugs() }) {
                        Icon(Icons.Default.Restore, contentDescription = "Restaurar padrão")
                    }
                }
            }
            drugCatalog.forEach { drug ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(drug.name, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${drug.unit} • Ref.: ${drug.referenceRangeLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { showDrugDialog = drug }) { Text("Editar") }
                        IconButton(onClick = { viewModel.deleteDrug(drug) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover droga")
                        }
                    }
                }
            }

            HorizontalDivider()

            // 4. Privacidade e Segurança (LGPD)
            SectionTitle("Privacidade e Segurança (LGPD)")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Bloquear captura de tela (FLAG_SECURE)", fontWeight = FontWeight.Medium)
                    Text(
                        "Impede prints e gravação nas telas com dados de pacientes.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(checked = settings.flagSecureEnabled, onCheckedChange = { viewModel.setFlagSecure(it) })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Bloqueio do app (Biometria / PIN)", fontWeight = FontWeight.Medium)
                    Text(
                        "Bloqueia após ${settings.inactivityTimeoutMinutes} min de inatividade.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(checked = settings.appLockEnabled, onCheckedChange = { viewModel.setAppLock(it) })
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { pinInput = it.filter(Char::isDigit).take(6) },
                    label = { Text("Definir PIN (4–6 dígitos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    enabled = pinInput.length >= 4,
                    onClick = {
                        viewModel.setPin(pinInput)
                        pinInput = ""
                    }
                ) { Text("Salvar PIN") }
            }

            Button(
                onClick = { showEraseConfirm = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apagar Todos os Dados dos Plantões")
            }

            HorizontalDivider()

            // 5. Termos e Sobre
            SectionTitle("Sobre e Termos de Uso")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Plantão UTI v1.0.0", fontWeight = FontWeight.Bold)
                    Text(
                        "Aviso Clínico Importante: Este aplicativo é uma ferramenta auxiliar de registro de enfermagem (substituindo a folha de papel). Ele NÃO calcula, NÃO prescreve e NÃO sugere doses de medicamentos — apenas registra os valores informados pelo profissional de saúde.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Privacidade (LGPD): Todos os dados ficam armazenados exclusivamente no aparelho em banco criptografado (SQLCipher), sem anúncios, sem rastreadores e sem envio para servidores externos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showEraseConfirm) {
        AlertDialog(
            onDismissRequest = { showEraseConfirm = false },
            title = { Text("Apagar todos os plantões?") },
            text = { Text("Esta ação remove permanentemente todos os pacientes, sinais vitais, infusões, materiais e balanços registrados neste aparelho.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eraseAllShiftData { showEraseConfirm = false }
                }) { Text("Apagar Tudo", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEraseConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    if (showNewDrugDialog || showDrugDialog != null) {
        DrugEditorDialog(
            initial = showDrugDialog,
            onDismiss = {
                showNewDrugDialog = false
                showDrugDialog = null
            },
            onSave = { entity ->
                viewModel.saveDrug(entity)
                showNewDrugDialog = false
                showDrugDialog = null
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun DrugEditorDialog(
    initial: DrugCatalogEntity?,
    onDismiss: () -> Unit,
    onSave: (DrugCatalogEntity) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var unit by remember { mutableStateOf(initial?.unit ?: "") }
    var refLabel by remember { mutableStateOf(initial?.referenceRangeLabel ?: "") }
    var minText by remember { mutableStateOf(initial?.minDose?.toString() ?: "") }
    var maxText by remember { mutableStateOf(initial?.maxDose?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nova Droga no Catálogo" else "Editar Droga") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome *") }, singleLine = true)
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unidade *") }, singleLine = true)
                OutlinedTextField(value = refLabel, onValueChange = { refLabel = it }, label = { Text("Texto da faixa de referência") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minText,
                        onValueChange = { minText = it },
                        label = { Text("Mín") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxText,
                        onValueChange = { maxText = it },
                        label = { Text("Máx") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && unit.isNotBlank(),
                onClick = {
                    onSave(
                        DrugCatalogEntity(
                            id = initial?.id ?: 0L,
                            name = name.trim(),
                            unit = unit.trim(),
                            referenceRangeLabel = refLabel.trim(),
                            minDose = minText.replace(",", ".").toDoubleOrNull(),
                            maxDose = maxText.replace(",", ".").toDoubleOrNull(),
                            isDefault = initial?.isDefault ?: false
                        )
                    )
                }
            ) { Text("Salvar") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
