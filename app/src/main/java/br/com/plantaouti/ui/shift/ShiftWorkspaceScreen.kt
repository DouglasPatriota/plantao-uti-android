package br.com.plantaouti.ui.shift

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import br.com.plantaouti.ui.components.ThemeToggleButton

private enum class ShiftTab(val label: String, val icon: ImageVector) {
    VITALS("Sinais", Icons.Rounded.MonitorHeart),
    DRUGS("Drogas", Icons.Rounded.Medication),
    MATERIALS("Materiais", Icons.Rounded.Inventory2),
    NOTES("Anotações", Icons.Rounded.Description),
    FLUIDS("Balanço", Icons.Rounded.WaterDrop),
    SUMMARY("Resumo", Icons.AutoMirrored.Rounded.Assignment),
    PRINT("Impressão", Icons.Rounded.Print),
    CALCULATIONS("Cálculos", Icons.Rounded.Calculate)
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
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
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
                    ThemeToggleButton(
                        currentThemeMode = settings.themeMode,
                        onToggle = { newMode -> viewModel.setThemeMode(newMode) }
                    )
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Configurações")
                    }
                }
            )
        },
        bottomBar = {
            // ScrollableTabRow no rodapé garante que TODAS as abas e ícones apareçam perfeitamente
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 8.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                ShiftTab.entries.forEachIndexed { index, tab ->
                    val isSelected = selectedTabIndex == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        text = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    )
                }
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
}
