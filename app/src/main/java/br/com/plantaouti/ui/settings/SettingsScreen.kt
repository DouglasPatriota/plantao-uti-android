package br.com.plantaouti.ui.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import br.com.plantaouti.ui.components.FormOutlinedTextField
import br.com.plantaouti.ui.components.FormSectionHeader
import br.com.plantaouti.ui.components.FullWidthActionButton
import br.com.plantaouti.ui.components.SelectableOptionCard
import br.com.plantaouti.ui.components.ThemeToggleButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
                title = { Text("Configurações & LGPD", fontWeight = FontWeight.Bold) },
                actions = {
                    ThemeToggleButton(
                        currentThemeMode = settings.themeMode,
                        onToggle = { newMode -> viewModel.setThemeMode(newMode) }
                    )
                }
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
            FormSectionHeader(title = "Aparência e Plantão")

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SelectableOptionCard(
                    title = "Modo Claro",
                    subtitle = "Padrão clínico gelo",
                    icon = Icons.Rounded.WbSunny,
                    isSelected = settings.themeMode == AppThemeMode.LIGHT,
                    onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f)
                )

                SelectableOptionCard(
                    title = "Modo Escuro",
                    subtitle = "Plantão noturno grafite",
                    icon = Icons.Rounded.NightsStay,
                    isSelected = settings.themeMode == AppThemeMode.DARK,
                    onClick = { viewModel.setThemeMode(AppThemeMode.DARK) },
                    modifier = Modifier.weight(1f)
                )
            }

            FormOutlinedTextField(
                value = techName,
                onValueChange = {
                    techName = it
                    viewModel.setTechnicianName(it)
                },
                label = "Nome padrão do técnico responsável",
                leadingIcon = Icons.Rounded.SupportAgent
            )

            HorizontalDivider()

            // 2. Alarmes de Infusão
            FormSectionHeader(title = "Alarmes de Fim de Infusão")

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                FormOutlinedTextField(
                    value = alarm1,
                    onValueChange = { alarm1 = it.filter(Char::isDigit).take(3) },
                    label = "1º aviso",
                    unitSuffix = "min",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )

                FormOutlinedTextField(
                    value = alarm2,
                    onValueChange = { alarm2 = it.filter(Char::isDigit).take(2) },
                    label = "2º aviso",
                    unitSuffix = "min",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            FullWidthActionButton(
                text = "Salvar Tempos de Alarme",
                onClick = {
                    viewModel.setAlarmMinutes(alarm1.toIntOrNull() ?: 30, alarm2.toIntOrNull() ?: 10)
                }
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !viewModel.alarmScheduler.canScheduleExactAlarms()) {
                OutlinedButton(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Permitir Alarmes Exatos (Android 12+)", fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider()

            // 3. Tabela Editável de Drogas Vasoativas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FormSectionHeader(title = "Catálogo de Drogas (${drugCatalog.size})")
                Row {
                    IconButton(onClick = { showNewDrugDialog = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Adicionar droga", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { viewModel.restoreDefaultDrugs() }) {
                        Icon(Icons.Rounded.Restore, contentDescription = "Restaurar padrão", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            drugCatalog.forEach { drug ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.Medication, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(drug.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    "${drug.unit} • Ref.: ${drug.referenceRangeLabel}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row {
                            TextButton(onClick = { showDrugDialog = drug }) { Text("Editar", fontWeight = FontWeight.Bold) }
                            IconButton(onClick = { viewModel.deleteDrug(drug) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Remover droga", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // 4. Privacidade e Segurança (LGPD)
            FormSectionHeader(title = "Privacidade e Segurança (LGPD)")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Bloquear captura de tela (FLAG_SECURE)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Impede prints e gravação nas telas com dados de pacientes.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = settings.flagSecureEnabled, onCheckedChange = { viewModel.setFlagSecure(it) })
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Bloqueio do app (Biometria / PIN)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Bloqueia após ${settings.inactivityTimeoutMinutes} min de inatividade.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = settings.appLockEnabled, onCheckedChange = { viewModel.setAppLock(it) })
                    }

                    FormOutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it.filter(Char::isDigit).take(6) },
                        label = "Definir novo PIN (4–6 dígitos)",
                        leadingIcon = Icons.Rounded.Lock,
                        keyboardType = KeyboardType.NumberPassword
                    )

                    Button(
                        enabled = pinInput.length >= 4,
                        onClick = {
                            viewModel.setPin(pinInput)
                            pinInput = ""
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Salvar Novo PIN", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick = { showEraseConfirm = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Apagar Todos os Dados dos Plantões", fontWeight = FontWeight.Bold)
            }

            HorizontalDivider()

            // 5. Termos e Sobre
            FormSectionHeader(title = "Sobre e Proteção de Dados")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Plantão UTI v1.0.0", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "Aviso Clínico Importante: Este aplicativo é uma ferramenta auxiliar de registro de enfermagem. Ele NÃO calcula, NÃO prescreve e NÃO sugere doses de medicamentos — apenas registra os valores informados pelo profissional de saúde.",
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
            title = { Text("Apagar todos os plantões?", fontWeight = FontWeight.Bold) },
            text = { Text("Esta ação remove permanentemente todos os pacientes, sinais vitais, infusões, materiais e balanços registrados neste aparelho.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eraseAllShiftData { showEraseConfirm = false }
                }) { Text("Apagar Tudo", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
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
        title = { Text(if (initial == null) "Nova Droga no Catálogo" else "Editar Droga", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FormOutlinedTextField(value = name, onValueChange = { name = it }, label = "Nome da droga *")
                FormOutlinedTextField(value = unit, onValueChange = { unit = it }, label = "Unidade (ex.: mcg/kg/min) *")
                FormOutlinedTextField(value = refLabel, onValueChange = { refLabel = it }, label = "Texto da faixa de referência")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormOutlinedTextField(
                        value = minText,
                        onValueChange = { minText = it },
                        label = "Mín",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                    FormOutlinedTextField(
                        value = maxText,
                        onValueChange = { maxText = it },
                        label = "Máx",
                        keyboardType = KeyboardType.Decimal,
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
            ) { Text("Salvar", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
