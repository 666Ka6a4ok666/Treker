package com.example.expensetracker.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.ui.viewmodel.MainViewModel
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val totalProfit by viewModel.totalGrossProfit.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val netProfit by viewModel.netProfit.collectAsState()
    val products by viewModel.products.collectAsState()
    val inventoryValue = products.sumOf { it.costPrice * it.stockQuantity }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Фінансова Аналітика", style = MaterialTheme.typography.headlineMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Загальна виручка: ₴%.2f".format(totalRevenue))
                Text(text = "Валовий прибуток: ₴%.2f".format(totalProfit))
                Text(text = "Загальні витрати: ₴%.2f".format(totalExpenses))
                Text(
                    text = "Чистий прибуток: ₴%.2f".format(netProfit),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(text = "Собівартість товарів на складі: ₴%.2f".format(inventoryValue))
            }
        }
    }
}