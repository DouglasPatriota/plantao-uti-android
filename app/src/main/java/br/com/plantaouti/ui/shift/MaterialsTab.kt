package br.com.plantaouti.ui.shift

import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.plantaouti.ui.components.FormOutlinedTextField
import br.com.plantaouti.ui.components.FormSectionHeader
import br.com.plantaouti.ui.components.FullWidthActionButton
import br.com.plantaouti.ui.components.QuickStepper
import br.com.plantaouti.ui.components.TabularNumbersTextStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaterialsTab(viewModel: ShiftWorkspaceViewModel) {
    val materials by viewModel.materials.collectAsStateWithLifecycle()
    val catalog by viewModel.materialCatalog.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var query by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }

    val currentQty = quantityText.toIntOrNull()?.coerceAtLeast(1) ?: 1

    val suggestions = remember(query, catalog) {
        if (query.isBlank()) catalog.take(10)
        else catalog.filter { it.name.contains(query.trim(), ignoreCase = true) }.take(8)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FormSectionHeader(
                        title = "Registro Rápido de Insumos",
                        subtitle = "Registre materiais e acompanhe o consumo do plant\u00e3o."
                    )

                    FormOutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = "Material (digite ou selecione abaixo) *",
                        leadingIcon = Icons.Rounded.Search
                    )

                    // Sugestões de catálogo em FlowRow
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        suggestions.forEach { item ->
                            FilterChip(
                                selected = query.trim().equals(item.name, ignoreCase = true),
                                onClick = { query = item.name },
                                label = { Text(item.name, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(0.9f)) {
                            FormOutlinedTextField(
                                value = quantityText,
                                onValueChange = { quantityText = it.filter(Char::isDigit).take(3) },
                                label = "Quantidade",
                                keyboardType = KeyboardType.Number,
                                unitSuffix = "un."
                            )
                            Spacer(Modifier.height(4.dp))
                            QuickStepper(
                                onDecrement = {
                                    quantityText = (currentQty - 1).coerceAtLeast(1).toString()
                                },
                                onIncrement = {
                                    quantityText = (currentQty + 1).toString()
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        FormOutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = "Observação (opcional)",
                            modifier = Modifier.weight(1.1f)
                        )
                    }

                    // Chips rápidos de Quantidade
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 2, 5, 10, 20).forEach { q ->
                            FilterChip(
                                selected = currentQty == q,
                                onClick = { quantityText = q.toString() },
                                label = { Text("+$q un.", fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    FullWidthActionButton(
                        text = "Lançar Material no Plantão",
                        enabled = query.isNotBlank(),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val qty = quantityText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                            viewModel.addMaterial(query, qty, notes)
                            query = ""
                            quantityText = "1"
                            notes = ""
                        }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Itens Registrados no Plantão",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        "${materials.size} itens · ${materials.sumOf { it.quantity }} un.",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (materials.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nenhum material registrado no leito ainda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(materials, key = { it.id }) { usage ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Rounded.Inventory2,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    usage.materialName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (usage.notes.isNotBlank()) {
                                    Text(
                                        usage.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.incrementMaterial(usage, -1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Remove,
                                    contentDescription = "Diminuir",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                usage.quantity.toString(),
                                style = MaterialTheme.typography.titleLarge.merge(TabularNumbersTextStyle),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            FilledTonalButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.incrementMaterial(usage, +1)
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("+1", fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = { viewModel.deleteMaterial(usage) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = "Remover",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
