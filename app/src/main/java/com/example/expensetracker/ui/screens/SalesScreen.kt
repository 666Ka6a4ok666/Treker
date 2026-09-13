package com.example.expensetracker.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.local.entity.ProductEntity
import com.example.expensetracker.ui.sales.SalesViewModel
import kotlinx.coroutines.launch
@Composable
fun SalesScreen(viewModel: SalesViewModel) {
    val sales by viewModel.salesList.collectAsState()
    val products by viewModel.products.collectAsState()
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        viewModel.eventMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
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
            Text(text = "Продаж Товарів", style = MaterialTheme.typography.headlineMedium)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedProduct?.name ?: "Оберіть товар",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Товар") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    products.forEach { product ->
                        DropdownMenuItem(
                            text = {
                                Text("${product.name} — залишок: ${product.stockQuantity} шт.")
                            },
                            onClick = {
                                selectedProduct = product
                                expanded = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Кількість") },
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
                    val qty = quantity.toIntOrNull() ?: 0
                    val product = selectedProduct
                    if (product != null && qty > 0) {
                        viewModel.sellProduct(product.id, qty, note)
                        quantity = "1"
                        note = ""
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Оберіть товар та введіть кількість")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Продати")
            }
            HorizontalDivider()
            Text(text = "Історія продажів", style = MaterialTheme.typography.titleMedium)
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
}
