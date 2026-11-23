package br.com.plantaouti.ui.shift

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.plantaouti.data.local.entity.FluidBalanceEntity
import br.com.plantaouti.ui.components.FormOutlinedTextField
import br.com.plantaouti.ui.components.FormSectionHeader
import br.com.plantaouti.ui.components.FullWidthActionButton
import br.com.plantaouti.ui.components.QuickStepper
import br.com.plantaouti.ui.components.TabularNumbersTextStyle

private val STOOL_ASPECT_CHIPS = listOf("Pastoso", "Semipastoso", "Líquido", "Endurecido", "Melena", "Muco")

@Composable
fun FluidBalanceTab(viewModel: ShiftWorkspaceViewModel) {
    val rows by viewModel.fluidBalance.collectAsStateWithLifecycle()
    val hScroll = rememberScrollState()
    val haptic = LocalHapticFeedback.current
    var editingStoolRow by remember { mutableStateOf<FluidBalanceEntity?>(null) }

    val totalSvd = rows.count { it.diuresisSvd }
    val totalFralda = rows.count { it.diuresisFralda }
    val totalVaso = rows.count { it.diuresisVaso }
    val totalPapagaio = rows.count { it.diuresisPapagaio }
    val totalStool = rows.sumOf { it.stoolCount }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Marque a via de diurese e toque em Fezes para registrar evacuações (sem registro em mL).",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .horizontalScroll(hScroll)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            FbHeader("Hora", 60.dp)
            FbHeader("SVD", 56.dp)
            FbHeader("Fralda", 62.dp)
            FbHeader("Vaso", 56.dp)
            FbHeader("Papag.", 64.dp)
            FbHeader("Fezes / Aspecto", 170.dp)
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(rows, key = { it.id }) { item ->
                Row(modifier = Modifier.horizontalScroll(hScroll)) {
                    FbTextCell(item.hourLabel, 60.dp, bold = true)
                    FbCheckCell(item.diuresisSvd, 56.dp) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.updateFluidBalance(item.copy(diuresisSvd = it))
                    }
                    FbCheckCell(item.diuresisFralda, 62.dp) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.updateFluidBalance(item.copy(diuresisFralda = it))
                    }
                    FbCheckCell(item.diuresisVaso, 56.dp) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.updateFluidBalance(item.copy(diuresisVaso = it))
                    }
                    FbCheckCell(item.diuresisPapagaio, 64.dp) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.updateFluidBalance(item.copy(diuresisPapagaio = it))
                    }
                    Box(
                        modifier = Modifier
                            .width(170.dp)
                            .height(50.dp)
                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            .clickable { editingStoolRow = item }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        val txt = when {
                            item.stoolCount > 0 && item.stoolAspect.isNotBlank() -> "${item.stoolCount}x (${item.stoolAspect})"
                            item.stoolCount > 0 -> "${item.stoolCount}x"
                            else -> "Toque p/ registrar"
                        }
                        Text(
                            text = txt,
                            style = MaterialTheme.typography.bodyMedium.merge(TabularNumbersTextStyle),
                            color = if (item.stoolCount > 0) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Linha de TOTAL
        Row(
            modifier = Modifier
                .horizontalScroll(hScroll)
                .background(MaterialTheme.colorScheme.secondaryContainer)
        ) {
            FbTotalCell("TOTAL", 60.dp)
            FbTotalCell("$totalSvd", 56.dp)
            FbTotalCell("$totalFralda", 62.dp)
            FbTotalCell("$totalVaso", 56.dp)
            FbTotalCell("$totalPapagaio", 64.dp)
            FbTotalCell("$totalStool evacuações", 170.dp)
        }
    }

    editingStoolRow?.let { current ->
        StoolEditBottomSheet(
            item = current,
            onDismiss = { editingStoolRow = null },
            onSave = { count, aspect ->
                viewModel.updateFluidBalance(current.copy(stoolCount = count, stoolAspect = aspect))
                editingStoolRow = null
            }
        )
    }
}

@Composable
private fun FbHeader(label: String, width: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(44.dp)
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FbTextCell(label: String, width: Dp, bold: Boolean = false) {
    Box(
        modifier = Modifier
            .width(width)
            .height(50.dp)
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun FbCheckCell(checked: Boolean, width: Dp, onCheckedChange: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .width(width)
            .height(50.dp)
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun FbTotalCell(label: String, width: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(48.dp)
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge.merge(TabularNumbersTextStyle),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun StoolEditBottomSheet(
    item: FluidBalanceEntity,
    onDismiss: () -> Unit,
    onSave: (Int, String) -> Unit
) {
    var count by remember { mutableIntStateOf(item.stoolCount) }
    var aspect by remember { mutableStateOf(item.stoolAspect) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                                Icons.Rounded.WaterDrop,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Registro de Evacuações — ${item.hourLabel}h",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Quantidade e consistência das fezes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Fechar")
                }
            }

            FormSectionHeader(title = "Contagem de Evacuações")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$count evacuação(ões)",
                    style = MaterialTheme.typography.titleLarge.merge(TabularNumbersTextStyle),
                    fontWeight = FontWeight.Bold
                )

                QuickStepper(
                    onDecrement = { if (count > 0) count-- },
                    onIncrement = { count++ }
                )
            }

            FormSectionHeader(title = "Aspecto das Fezes")

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                STOOL_ASPECT_CHIPS.forEach { chip ->
                    FilterChip(
                        selected = aspect.equals(chip, ignoreCase = true),
                        onClick = {
                            aspect = chip
                            if (count == 0) count = 1
                        },
                        label = { Text(chip, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            FormOutlinedTextField(
                value = aspect,
                onValueChange = { aspect = it },
                label = "Aspecto / Observação Curta"
            )

            FullWidthActionButton(
                text = "Salvar Evacuação",
                onClick = { onSave(count, aspect.trim()) }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
