package br.com.plantaouti.ui.shift

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private enum class ShiftTab(val label: String, val icon: ImageVector) {
    VITALS("Sinais", Icons.Default.MonitorHeart),
    DRUGS("Drogas", Icons.Default.Medication),
    MATERIALS("Materiais", Icons.Default.Inventory2),
    FLUIDS("Balanço", Icons.Default.WaterDrop),
    SUMMARY("Resumo", Icons.Default.Assignment)
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
                        shift?.let {
                            Text(
                                "${it.shiftType.label} • %.1f kg • ${it.shiftDate}".format(it.weightKg),
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
            NavigationBar {
                ShiftTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
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
                    ShiftTab.FLUIDS -> FluidBalanceTab(viewModel = viewModel)
                    ShiftTab.SUMMARY -> SummaryTab(viewModel = viewModel)
                }
            }
        }
    }
}
