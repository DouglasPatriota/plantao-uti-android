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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
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
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (item.stoolCount > 0) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Linha de TOTAL fixa no rodapé da aba
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
        StoolEditDialog(
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
        Text(label, fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal)
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
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StoolEditDialog(
    item: FluidBalanceEntity,
    onDismiss: () -> Unit,
    onSave: (Int, String) -> Unit
) {
    var count by remember { mutableIntStateOf(item.stoolCount) }
    var aspect by remember { mutableStateOf(item.stoolAspect) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Evacuações — ${item.hourLabel}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Número de evacuações:", fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { if (count > 0) count-- }) { Text("-") }
                        Text("$count", style = MaterialTheme.typography.titleMedium)
                        OutlinedButton(onClick = { count++ }) { Text("+") }
                    }
                }
                Text("Aspecto (opções rápidas):", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    STOOL_ASPECT_CHIPS.forEach { chip ->
                        FilterChip(
                            selected = aspect.equals(chip, ignoreCase = true),
                            onClick = {
                                aspect = chip
                                if (count == 0) count = 1
                            },
                            label = { Text(chip) }
                        )
                    }
                }
                OutlinedTextField(
                    value = aspect,
                    onValueChange = { aspect = it },
                    label = { Text("Aspecto / observação curta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(count, aspect.trim()) }) { Text("Salvar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
