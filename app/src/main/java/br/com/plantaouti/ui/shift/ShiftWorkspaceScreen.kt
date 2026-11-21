package br.com.plantaouti.ui.shift

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.plantaouti.domain.calculator.MedicalCalculators

private enum class ShiftTab(val label: String, val icon: ImageVector) {
    VITALS("Sinais", Icons.Default.MonitorHeart),
    DRUGS("Drogas", Icons.Default.Medication),
    MATERIALS("Materiais", Icons.Default.Inventory2),
    NOTES("Anotações", Icons.Default.Description),
    FLUIDS("Balanço", Icons.Default.WaterDrop),
    SUMMARY("Resumo", Icons.Default.Assignment),
    PRINT("Impressão", Icons.Default.Print),
    CALCULATIONS("Cálculos", Icons.Default.Calculate)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftWorkspaceScreen(
    windowWidthSizeClass: WindowWidthSizeClass,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ShiftWorkspaceViewModel = hiltViewModel()
) {
    val shift by viewModel.shift.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showMoreSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val isTabletExpanded = windowWidthSizeClass == WindowWidthSizeClass.Expanded

    LaunchedEffect(Unit) {
        viewModel.feedback.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                title = {
                    Column {
                        Text(
                            shift?.let { "Leito ${it.bedNumber} — ${it.patientName}" } ?: "Carregando...",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        shift?.let { currentShift ->
                            val age = MedicalCalculators.calculateAge(currentShift.birthDate)
                            val ageStr = if (age != null) " • $age anos" else ""
                            Text(
                                "${currentShift.shiftType.label} • %.1f kg%s • ${currentShift.shiftDate}".format(currentShift.weightKg, ageStr),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                // 5 Ícones principais + "Mais" (Stitch Layout M3)
                val primaryTabs = listOf(
                    ShiftTab.VITALS,
                    ShiftTab.DRUGS,
                    ShiftTab.MATERIALS,
                    ShiftTab.NOTES
                )

                primaryTabs.forEach { tab ->
                    val index = ShiftTab.entries.indexOf(tab)
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, maxLines = 1) }
                    )
                }

                NavigationBarItem(
                    selected = showMoreSheet || selectedTabIndex >= 4,
                    onClick = { showMoreSheet = true },
                    icon = { Icon(Icons.Default.GridView, contentDescription = "Mais") },
                    label = { Text("Mais", maxLines = 1) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val selectedTab = ShiftTab.entries[selectedTabIndex]
            // Layout adaptativo em tablet: mostra Sinais Vitais lado a lado com Drogas Vasoativas
            if (isTabletExpanded && (selectedTab == ShiftTab.VITALS || selectedTab == ShiftTab.DRUGS)) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1.15f).fillMaxHeight()) {
                        VitalSignsTab(viewModel = viewModel)
                    }
                    VerticalDivider()
                    Box(modifier = Modifier.weight(0.85f).fillMaxHeight()) {
                        InfusionsTab(viewModel = viewModel)
                    }
                }
            } else {
                when (selectedTab) {
                    ShiftTab.VITALS -> VitalSignsTab(viewModel = viewModel)
                    ShiftTab.DRUGS -> InfusionsTab(viewModel = viewModel)
                    ShiftTab.MATERIALS -> MaterialsTab(viewModel = viewModel)
                    ShiftTab.NOTES -> NursingNotesTab(viewModel = viewModel)
                    ShiftTab.FLUIDS -> FluidBalanceTab(viewModel = viewModel)
                    ShiftTab.SUMMARY -> SummaryTab(viewModel = viewModel)
                    ShiftTab.PRINT -> CustomPrintTab(viewModel = viewModel)
                    ShiftTab.CALCULATIONS -> CalculationsTab(viewModel = viewModel)
                }
            }
        }
    }

    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Módulos e Recursos do Leito",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                MoreMenuItemCard(
                    title = "Balanço Hídrico & Eliminações",
                    subtitle = "Registro de diurese, evacuções e drenagens",
                    icon = Icons.Default.WaterDrop,
                    onClick = {
                        selectedTabIndex = ShiftTab.entries.indexOf(ShiftTab.FLUIDS)
                        showMoreSheet = false
                    }
                )

                MoreMenuItemCard(
                    title = "Resumo & Passagem de Plantão",
                    subtitle = "Consolidado geral e encerramento do leito",
                    icon = Icons.Default.Assignment,
                    onClick = {
                        selectedTabIndex = ShiftTab.entries.indexOf(ShiftTab.SUMMARY)
                        showMoreSheet = false
                    }
                )

                MoreMenuItemCard(
                    title = "Impressão de Relatórios A4",
                    subtitle = "Gerar PDF customizado e impressão nativa",
                    icon = Icons.Default.Print,
                    onClick = {
                        selectedTabIndex = ShiftTab.entries.indexOf(ShiftTab.PRINT)
                        showMoreSheet = false
                    }
                )

                MoreMenuItemCard(
                    title = "Cálculos Médicos (Beta)",
                    subtitle = "Diluição, gotejamento, regra de três e vasoativos",
                    icon = Icons.Default.Calculate,
                    onClick = {
                        selectedTabIndex = ShiftTab.entries.indexOf(ShiftTab.CALCULATIONS)
                        showMoreSheet = false
                    }
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MoreMenuItemCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
