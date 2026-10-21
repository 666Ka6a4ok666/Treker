package com.example.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(viewModel: MainViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    val defaultCategories = remember {
        listOf("Інше")
    }
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
    var expanded by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                TextButton(
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
                ) {
                    Text("Додати")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Скасувати")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Облік Витрат", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Назва витрати") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Категорія витрат") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availableCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("+ Додати нову категорію...") },
                            onClick = {
                                expanded = false
                                newCategoryInput = ""
                                showAddCategoryDialog = true
                            }
                        )
                    }
                }
                FilledTonalIconButton(
                    onClick = {
                        newCategoryInput = ""
                        showAddCategoryDialog = true
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Додати категорію")
                }
            }
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Сума (грн)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Нотатка (необов'язково)") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val expenseAmount = amount.toDoubleOrNull()
                    if (title.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Введіть назву витрати") }
                    } else if (expenseAmount == null || expenseAmount <= 0) {
                        scope.launch { snackbarHostState.showSnackbar("Введіть коректну суму витрати (> 0)") }
                    } else {
                        viewModel.insertExpense(
                            ExpenseEntity(
                                title = title.trim(),
                                category = category,
                                amount = expenseAmount,
                                note = note.trim()
                            )
                        )
                        title = ""
                        category = "Інше"
                        amount = ""
                        note = ""
                        scope.launch { snackbarHostState.showSnackbar("Витрату успішно додано!") }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Додати Витрату")
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(expenses) { expense ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = expense.title, style = MaterialTheme.typography.titleMedium)
                            Text(text = "Категорія: ${expense.category}")
                            Text(text = "Сума: ₴${expense.amount}")
                            if (expense.note.isNotBlank()) {
                                Text(text = "Нотатка: ${expense.note}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
