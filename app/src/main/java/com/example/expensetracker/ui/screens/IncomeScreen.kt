package com.example.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.local.entity.IncomeEntity
import com.example.expensetracker.ui.common.DatePeriod
import com.example.expensetracker.ui.common.DatePeriodChips
import com.example.expensetracker.ui.common.formatDateTime
import com.example.expensetracker.ui.theme.ExpenseRed
import com.example.expensetracker.ui.theme.ProfitGreen
import com.example.expensetracker.ui.theme.ProfitGreenContainer
import com.example.expensetracker.ui.viewmodel.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeScreen(viewModel: MainViewModel) {
    val incomes by viewModel.incomes.collectAsState()
    val totalIncomeSum by viewModel.totalIncome.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddBottomSheet by remember { mutableStateOf(false) }
        var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedPeriod by remember { mutableStateOf(DatePeriod.ALL) }

    val categories = remember { listOf("Волонтерські", "Інвестиції", "Гранти", "Інше") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Залучені кошти", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddBottomSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Нове надходження") },
                containerColor = ProfitGreen,
                contentColor = MaterialTheme.colorScheme.surface
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("Всі") }
                )
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = if (selectedCategory == cat) null else cat
                        },
                        label = { Text(cat) }
                    )
                }
            }

                        DatePeriodChips(
                selected = selectedPeriod,
                onSelect = { selectedPeriod = it }
            )

            val filteredIncomes = remember(incomes, selectedCategory, selectedPeriod) {
                val byCategory = if (selectedCategory == null) incomes else incomes.filter { it.category == selectedCategory }
                byCategory.filter { selectedPeriod.contains(it.incomeDate) }
            }

            if (filteredIncomes.isEmpty()) {
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
                                .background(ProfitGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolunteerActivism,
                                contentDescription = null,
                                tint = ProfitGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = if (selectedCategory != null) "Немає записів у цій категорії" else "Надходження відсутні",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Натисніть «Нове надходження», щоб зафіксувати волонтерську допомогу чи інвестиції.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Summary Banner
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = ProfitGreenContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Всього залучено коштів",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                                                        Text(
                                text = "+₴ " + String.format(Locale.US, "%,.2f", totalIncomeSum),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = ProfitGreen
                            )
                        }
                        Text(
                            text = "${filteredIncomes.size} надходжень",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredIncomes, key = { it.id }) { income ->
                        var showDeleteConfirm by remember { mutableStateOf(false) }
                        if (showDeleteConfirm) {
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirm = false },
                                title = { Text("Видалити надходження?") },
                                text = { Text("«${income.title}» буде видалено разом із надходженням у касі.") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            viewModel.deleteIncome(income.id)
                                            showDeleteConfirm = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                                    ) { Text("Видалити") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Скасувати") }
                                }
                            )
                        }
                        IncomeItemCard(
                            income = income,
                            onDelete = { showDeleteConfirm = true }
                        )
                    }
                }
            }
        }

        if (showAddBottomSheet) {
            AddIncomeBottomSheet(
                onDismiss = { showAddBottomSheet = false },
                onConfirm = { title, category, amount, note ->
                    viewModel.insertIncome(
                        IncomeEntity(
                            title = title,
                            category = category,
                            amount = amount,
                            note = note
                        )
                    )
                    showAddBottomSheet = false
                }
            )
        }
    }
}



@Composable
fun IncomeItemCard(income: IncomeEntity, onDelete: () -> Unit = {}) {
    val icon: ImageVector = when (income.category) {
        "Волонтерські" -> Icons.Default.VolunteerActivism
        "Інвестиції" -> Icons.Default.ShowChart
        "Гранти" -> Icons.Default.CardGiftcard
        else -> Icons.Default.MonetizationOn
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ProfitGreenContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ProfitGreen,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = income.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                                Text(
                    text = income.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDateTime(income.incomeDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                if (income.note.isNotBlank()) {
                    Text(
                        text = income.note,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+₴ " + String.format(Locale.US, "%,.2f", income.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ProfitGreen
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Видалити",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, amount: Double, note: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val categories = remember { listOf("Волонтерські", "Інвестиції", "Гранти", "Інше") }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Волонтерські") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Залучення коштів",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Джерело / Назва (наприклад: Фонд, Інвестор)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = !expandedDropdown }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Тип надходження") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedDropdown) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Сума надходження (грн)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Примітка / цільове призначення") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val incAmount = amount.toDoubleOrNull()
                    if (title.isNotBlank() && incAmount != null && incAmount > 0) {
                        onConfirm(title.trim(), category, incAmount, note.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Заберегти надходження")
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
