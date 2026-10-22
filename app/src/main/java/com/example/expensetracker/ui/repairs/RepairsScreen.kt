package com.example.expensetracker.ui.repairs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.data.local.entity.RepairEntity
import com.example.expensetracker.ui.theme.ExpenseRed
import com.example.expensetracker.ui.theme.ExpenseRedContainer
import com.example.expensetracker.ui.theme.ProfitGreen
import com.example.expensetracker.ui.theme.ProfitGreenContainer
import com.example.expensetracker.ui.theme.WarningAmber
import com.example.expensetracker.ui.theme.WarningAmberContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("uk", "UA"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairsScreen(viewModel: RepairsViewModel) {
    val repairs by viewModel.repairs.collectAsStateWithLifecycle()
    val totalDebt by viewModel.totalRepairDebt.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { snackbarHostState.showSnackbar(it) }
    }

    if (showAddDialog) {
        AddRepairDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { asset, client, desc, labor, parts, comp, paid ->
                viewModel.createRepair(asset, client, desc, labor, parts, comp, paid)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ремонти", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Новий ремонт") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (totalDebt > 0) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = WarningAmberContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Не отримано компенсацій",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "₴ " + String.format(Locale.US, "%,.2f", totalDebt),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WarningAmber
                        )
                    }
                }
            }

            if (repairs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Build,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            "Ремонтів ще немає",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Натисніть «Новий ремонт», щоб облікувати витрати на запчастини та роботи.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(repairs, key = { it.id }) { repair ->
                        RepairCard(
                            repair = repair,
                            onCompensate = { viewModel.markCompensated(repair.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RepairCard(repair: RepairEntity, onCompensate: () -> Unit) {
    val resultPositive = repair.result >= 0
    val resultColor = if (resultPositive) ProfitGreen else ExpenseRed
    val resultBg = if (resultPositive) ProfitGreenContainer else ExpenseRedContainer

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        repair.assetName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = buildString {
                            if (repair.clientName.isNotBlank()) append("Клієнт: ${repair.clientName} • ")
                            append(dateFormat.format(Date(repair.repairDate)))
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(resultBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        (if (resultPositive) "+₴ " else "-₴ ") +
                            String.format(Locale.US, "%,.2f", kotlin.math.abs(repair.result)),
                        color = resultColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (repair.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    repair.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Запчастини", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₴ " + String.format(Locale.US, "%.2f", repair.partsCost), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Роботи", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₴ " + String.format(Locale.US, "%.2f", repair.laborCost), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Компенсація", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₴ " + String.format(Locale.US, "%.2f", repair.compensationAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (repair.compensationAmount > 0 && !repair.isCompensated) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onCompensate,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Отримати компенсацію")
                }
            } else if (repair.isCompensated) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "✓ Компенсацію отримано",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ProfitGreen
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRepairDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        assetName: String,
        clientName: String,
        description: String,
        laborCost: Double,
        parts: List<RepairFormPart>,
        compensationAmount: Double,
        compensationPaid: Boolean
    ) -> Unit
) {
    var assetName by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var laborCost by remember { mutableStateOf("") }
    var compensation by remember { mutableStateOf("") }
    var compensationPaid by remember { mutableStateOf(false) }
    val parts = remember { mutableStateListOf<RepairFormPart>() }

    var partName by remember { mutableStateOf("") }
    var partQty by remember { mutableStateOf("1") }
    var partCost by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новий ремонт") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = assetName,
                    onValueChange = { assetName = it },
                    label = { Text("Що ремонтуємо *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Клієнт (порожньо = внутрішній)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Опис / несправність") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = laborCost,
                    onValueChange = { laborCost = it },
                    label = { Text("Вартість робіт (грн)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()
                Text("Запчастини", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                parts.forEachIndexed { index, part ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${part.name} × ${part.quantity} = ₴ ${String.format(Locale.US, "%.2f", part.unitCost * part.quantity)}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { parts.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Видалити")
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = partName,
                        onValueChange = { partName = it },
                        label = { Text("Назва") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = partQty,
                        onValueChange = { partQty = it },
                        label = { Text("К-сть") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = partCost,
                        onValueChange = { partCost = it },
                        label = { Text("Ціна/шт") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                TextButton(
                    onClick = {
                        val qty = partQty.toIntOrNull() ?: 0
                        val cost = partCost.toDoubleOrNull() ?: 0.0
                        if (partName.isNotBlank() && qty > 0 && cost >= 0) {
                            parts.add(RepairFormPart(partName.trim(), null, qty, cost))
                            partName = ""
                            partQty = "1"
                            partCost = ""
                        }
                    }
                ) { Text("+ Додати запчастину") }

                HorizontalDivider()
                OutlinedTextField(
                    value = compensation,
                    onValueChange = { compensation = it },
                    label = { Text("Сума компенсації від клієнта (грн)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Компенсацію сплачено одразу", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = compensationPaid, onCheckedChange = { compensationPaid = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val labor = laborCost.toDoubleOrNull() ?: 0.0
                    val comp = compensation.toDoubleOrNull() ?: 0.0
                    if (assetName.isNotBlank()) {
                        onConfirm(
                            assetName,
                            clientName,
                            description,
                            labor,
                            parts.toList(),
                            comp,
                            compensationPaid
                        )
                    }
                }
            ) { Text("Зберегти") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Скасувати") }
        }
    )
}
