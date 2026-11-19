package br.com.plantaouti.ui.shift

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

@Composable
fun MaterialsTab(viewModel: ShiftWorkspaceViewModel) {
    val materials by viewModel.materials.collectAsStateWithLifecycle()
    val catalog by viewModel.materialCatalog.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var query by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }

    val suggestions = remember(query, catalog) {
        if (query.isBlank()) catalog.take(10)
        else catalog.filter { it.name.contains(query.trim(), ignoreCase = true) }.take(8)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Material (digite ou escolha abaixo)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))

        // Sugestões rápidas de autocomplete
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(suggestions, key = { it.id }) { item ->
                AssistChip(
                    onClick = { query = item.name },
                    label = { Text(item.name) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = quantityText,
                onValueChange = { quantityText = it.filter(Char::isDigit).take(3) },
                label = { Text("Qtd") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(84.dp)
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Observação (opcional)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(
                enabled = query.isNotBlank(),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val qty = quantityText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    viewModel.addMaterial(query, qty, notes)
                    query = ""
                    quantityText = "1"
                    notes = ""
                },
                modifier = Modifier.height(52.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "Itens registrados no plantão (${materials.sumOf { it.quantity }} un.)",
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(Modifier.height(6.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(materials, key = { it.id }) { usage ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(usage.materialName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            if (usage.notes.isNotBlank()) {
                                Text(
                                    usage.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.incrementMaterial(usage, -1) }) {
                                Icon(Icons.Default.Remove, contentDescription = "Diminuir")
                            }
                            Text(
                                "${usage.quantity}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            FilledTonalButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.incrementMaterial(usage, +1)
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("+1", fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { viewModel.deleteMaterial(usage) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover")
                            }
                        }
                    }
                }
            }
        }
    }
}
