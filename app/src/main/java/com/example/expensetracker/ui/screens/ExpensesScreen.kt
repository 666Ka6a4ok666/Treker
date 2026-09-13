package com.example.expensetracker.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.ui.viewmodel.MainViewModel
@Composable
fun ExpensesScreen(viewModel: MainViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Оренда") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
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
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Сума (грн)") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                val expenseAmount = amount.toDoubleOrNull() ?: 0.0
                if (title.isNotBlank() && expenseAmount > 0) {
                    viewModel.insertExpense(
                        ExpenseEntity(
                            title = title,
                            category = category,
                            amount = expenseAmount,
                            note = note
                        )
                    )
                    title = ""
                    amount = ""
                    note = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Додати Витрату")
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(expenses) { expense ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = expense.title, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Категорія: ${expense.category}")
                        Text(text = "Сума: ₴${expense.amount}")
                    }
                }
            }
        }
    }
}