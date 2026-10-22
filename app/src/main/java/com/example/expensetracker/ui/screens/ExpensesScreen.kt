package com.example.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.ui.common.DatePeriod
import com.example.expensetracker.ui.common.DatePeriodChips
import com.example.expensetracker.ui.common.formatDateTime
import com.example.expensetracker.ui.theme.ExpenseRed
import com.example.expensetracker.ui.theme.ExpenseRedContainer
import com.example.expensetracker.ui.viewmodel.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(viewModel: MainViewModel) {
    val expenses by viewModel.expenses.collectAsState()



        val snackbarHostState = remember { SnackbarHostState() }
    var showAddBottomSheet by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf(DatePeriod.ALL) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Облік Витрат", fontWeight = FontWeight.Bold) },
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
                text = { Text("Додати витрату") },
                containerColor = ExpenseRed,
                contentColor = MaterialTheme.colorScheme.surface
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (expenses.isEmpty()) {
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
                                .background(ExpenseRedContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = ExpenseRed,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = "Витрати відсутні",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Натисніть «Додати витрату», щоб зафіксувати першу операцію.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                        } else {
                val filteredExpenses = remember(expenses, selectedPeriod) {
                    expenses.filter { selectedPeriod.contains(it.expenseDate) }
                }
                val totalExpenseSum = remember(filteredExpenses) { filteredExpenses.sumOf { it.amount } }

                Column(modifier = Modifier.fillMaxSize()) {
                    DatePeriodChips(
                        selected = selectedPeriod,
                        onSelect = { selectedPeriod = it }
                    )

                    // Summary banner
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = ExpenseRedContainer
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
                                    text = "Загальна сума витрат",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "-₴ " + String.format(Locale.US, "%,.2f", totalExpenseSum),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                            }
                                                        Text(
                                text = "${filteredExpenses.size} записів",
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

                                                items(filteredExpenses, key = { it.id }) { expense ->
                            var showDeleteConfirm by remember { mutableStateOf(false) }
                            if (showDeleteConfirm) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteConfirm = false },
                                    title = { Text("Видалити витрату?") },
                                    text = { Text("«${expense.title}» буде видалено разом із рухом грошей у касі.") },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                viewModel.deleteExpense(expense.id)
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
                            ExpenseItemCard(
                                expense = expense,
                                onDelete = { showDeleteConfirm = true }
                            )
                        }
                    }
                }
            }
        }

        if (showAddBottomSheet) {
            AddExpenseBottomSheet(
                expenses = expenses,
                onDismiss = { showAddBottomSheet = false },
                onConfirm = { title, category, amount, note ->
                    viewModel.insertExpense(
                        ExpenseEntity(
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
fun ExpenseItemCard(expense: ExpenseEntity, onDelete: () -> Unit = {}) {
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
                    .background(ExpenseRedContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = ExpenseRed,
                    modifier = Modifier.size(22.dp)
                )
            }

                        Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDateTime(expense.expenseDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                if (expense.note.isNotBlank()) {
                    Text(
                        text = expense.note,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "-₴ " + String.format(Locale.US, "%,.2f", expense.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
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
fun AddExpenseBottomSheet(
    expenses: List<ExpenseEntity>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, amount: Double, note: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val defaultCategories = remember { listOf("Інше", "Оренда", "Комунальні", "Закупівля", "Зарплата", "Логістика", "Маркетинг") }
    var customCategories by remember { mutableStateOf(listOf<String>()) }
    val availableCategories = remember(expenses, customCategories) {
        (defaultCategories + expenses.map { it.category } + customCategories)
            .filter { it.isNotBlank() }
            .distinct()
    }

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Інше") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var expandedCategoryMenu by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Нова категорія витрат") },
            text = {
                OutlinedTextField(
                    value = newCategoryInput,
                    onValueChange = { newCategoryInput = it },
                    label = { Text("Назва категорії") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newCategoryInput.trim()
                        if (trimmed.isNotBlank()) {
                            if (!availableCategories.contains(trimmed)) {
                                customCategories = customCategories + trimmed
                            }
                            category = trimmed
                            showAddCategoryDialog = false
                        }
                    }
                ) { Text("Додати") }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) { Text("Скасувати") }
            }
        )
    }

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
                text = "Нова витрата",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Назва витрати") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedCategoryMenu,
                    onExpandedChange = { expandedCategoryMenu = !expandedCategoryMenu },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Категорія") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCategoryMenu) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategoryMenu,
                        onDismissRequest = { expandedCategoryMenu = false }
                    ) {
                        availableCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expandedCategoryMenu = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("+ Додати категорію...") },
                            onClick = {
                                expandedCategoryMenu = false
                                newCategoryInput = ""
                                showAddCategoryDialog = true
                            }
                        )
                    }
                }

                IconButton(
                    onClick = {
                        newCategoryInput = ""
                        showAddCategoryDialog = true
                    }
                ) {
                    Icon(Icons.Default.Category, contentDescription = "Нова категорія")
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Сума (грн)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Нотатка (необов'язково)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val expAmount = amount.toDoubleOrNull()
                    if (title.isNotBlank() && expAmount != null && expAmount > 0) {
                        onConfirm(title.trim(), category, expAmount, note.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Заберегти витрату")
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
