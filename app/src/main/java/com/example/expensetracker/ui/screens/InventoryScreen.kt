package com.example.expensetracker.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.local.entity.ProductEntity
import com.example.expensetracker.ui.viewmodel.MainViewModel
@Composable
fun InventoryScreen(viewModel: MainViewModel) {
    val products by viewModel.products.collectAsState()
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Електроніка") }
    var costPrice by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var stockQuantity by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Управління Складом", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Назва товару") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = costPrice,
                onValueChange = { costPrice = it },
                label = { Text("Собівартість") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = salePrice,
                onValueChange = { salePrice = it },
                label = { Text("Ціна продажу") },
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = stockQuantity,
            onValueChange = { stockQuantity = it },
            label = { Text("Кількість") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                val cost = costPrice.toDoubleOrNull() ?: 0.0
                val sale = salePrice.toDoubleOrNull() ?: 0.0
                val qty = stockQuantity.toIntOrNull() ?: 0
                if (name.isNotBlank() && qty > 0) {
                    viewModel.insertProduct(
                        ProductEntity(
                            name = name,
                            category = category,
                            costPrice = cost,
                            salePrice = sale,
                            stockQuantity = qty
                        )
                    )
                    name = ""
                    costPrice = ""
                    salePrice = ""
                    stockQuantity = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Додати Товар")
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(products) { product ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Залишок: ${product.stockQuantity} шт.")
                        Text(text = "Собівартість: ₴${product.costPrice} | Продаж: ₴${product.salePrice}")
                    }
                }
            }
        }
    }
}