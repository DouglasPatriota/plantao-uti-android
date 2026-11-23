package br.com.plantaouti.ui.shift

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.plantaouti.pdf.PdfPrintOptions
import br.com.plantaouti.pdf.PdfShareAndPrintHelper
import br.com.plantaouti.pdf.ShiftPdfGenerator
import br.com.plantaouti.ui.components.FullWidthActionButton

@Composable
fun CustomPrintTab(viewModel: ShiftWorkspaceViewModel) {
    val context = LocalContext.current
    val shift by viewModel.shift.collectAsStateWithLifecycle()
    val vitals by viewModel.vitals.collectAsStateWithLifecycle()
    val infusions by viewModel.infusions.collectAsStateWithLifecycle()
    val materials by viewModel.materials.collectAsStateWithLifecycle()
    val fluids by viewModel.fluidBalance.collectAsStateWithLifecycle()

    var incMaterials by remember { mutableStateOf(true) }
    var incDrugs by remember { mutableStateOf(true) }
    var incEvolutions by remember { mutableStateOf(true) }
    var incVitals by remember { mutableStateOf(true) }
    var incProcedures by remember { mutableStateOf(false) }

    val allSelected = incMaterials && incDrugs && incEvolutions && incVitals && incProcedures
    val currentShift = shift

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Impressão de Relatório Personalizado",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Selecione as seções que deseja incluir no relatório PDF de impressão:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PrintOptionCard(
            title = "Relatório Completo (Selecionar Todos)",
            subtitle = "Marcar todas as seções clínicas abaixo",
            checked = allSelected,
            onCheckedChange = { check ->
                incMaterials = check
                incDrugs = check
                incEvolutions = check
                incVitals = check
                incProcedures = check
            }
        )

        PrintOptionCard(
            title = "Gastos com Materiais",
            subtitle = "Lista de materiais e insumos utilizados no plantão",
            checked = incMaterials,
            onCheckedChange = { incMaterials = it }
        )

        PrintOptionCard(
            title = "Drogas Vasoativas / Medicamentos",
            subtitle = "Infusões contínuas ativas, doses e histórico",
            checked = incDrugs,
            onCheckedChange = { incDrugs = it }
        )

        PrintOptionCard(
            title = "Evoluções & Balanço Hídrico",
            subtitle = "Eliminações, diurese e anotações do plantão",
            checked = incEvolutions,
            onCheckedChange = { incEvolutions = it }
        )

        PrintOptionCard(
            title = "Sinais Vitais",
            subtitle = "Tabela de horários (PA, PAM, FC, FR, SpO₂, Temp, HGT)",
            checked = incVitals,
            onCheckedChange = { incVitals = it }
        )

        PrintOptionCard(
            title = "Procedimentos de Enfermagem",
            subtitle = "Procedimentos e intervenções realizadas",
            checked = incProcedures,
            onCheckedChange = { incProcedures = it }
        )

        // Miniatura A4
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(60.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.PictureAsPdf,
                        contentDescription = "Prévia PDF A4",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Folha A4 — Layout UTI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Formatação limpa e pronta para prontuário físico ou PDF",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (currentShift != null) {
                        val file = ShiftPdfGenerator.generateCustomShiftPdf(
                            context = context,
                            shift = currentShift,
                            vitals = vitals,
                            infusions = infusions,
                            materials = materials,
                            fluids = fluids,
                            options = PdfPrintOptions(incMaterials, incDrugs, incEvolutions, incVitals, incProcedures)
                        )
                        PdfShareAndPrintHelper.sharePdf(context, file)
                    } else {
                        Toast.makeText(context, "Nenhum paciente selecionado", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Icon(Icons.Rounded.Visibility, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("Prévia", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    if (currentShift != null) {
                        val file = ShiftPdfGenerator.generateCustomShiftPdf(
                            context = context,
                            shift = currentShift,
                            vitals = vitals,
                            infusions = infusions,
                            materials = materials,
                            fluids = fluids,
                            options = PdfPrintOptions(incMaterials, incDrugs, incEvolutions, incVitals, incProcedures)
                        )
                        PdfShareAndPrintHelper.sharePdf(context, file)
                    } else {
                        Toast.makeText(context, "Nenhum paciente selecionado", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("Compartilhar", fontWeight = FontWeight.Bold)
            }
        }

        FullWidthActionButton(
            text = "Imprimir Relatório Personalizado (A4)",
            icon = Icons.Rounded.Print,
            onClick = {
                if (currentShift != null) {
                    val file = ShiftPdfGenerator.generateCustomShiftPdf(
                        context = context,
                        shift = currentShift,
                        vitals = vitals,
                        infusions = infusions,
                        materials = materials,
                        fluids = fluids,
                        options = PdfPrintOptions(incMaterials, incDrugs, incEvolutions, incVitals, incProcedures)
                    )
                    PdfShareAndPrintHelper.printPdf(context, file)
                } else {
                    Toast.makeText(context, "Nenhum paciente selecionado", Toast.LENGTH_SHORT).show()
                }
            }
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PrintOptionCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                if (checked) 2.dp else 1.dp,
                if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(16.dp)
            )
            .clickable { onCheckedChange(!checked) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checked)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
