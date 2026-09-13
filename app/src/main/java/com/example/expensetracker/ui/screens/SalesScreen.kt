package com.example.expensetracker.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.ui.sales.SalesViewModel
@Composable
fun SalesScreen(viewModel: SalesViewModel) {
    val sales by viewModel.salesList.collectAsState()
    var quantity by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Продаж Товарів", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Кількість для продажу") },
            modifier = Modifier.fillMaxWidth()
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sales) { sale ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = sale.productName, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Кількість: ${sale.quantity} | Сума: ₴${sale.totalAmount}")
                        Text(text = "Прибуток: ₴${sale.profit}")
                    }
                }
            }
        }
    }
}
