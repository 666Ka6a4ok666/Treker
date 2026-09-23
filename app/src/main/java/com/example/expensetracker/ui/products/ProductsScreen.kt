package com.example.expensetracker.ui.products
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.data.local.entity.ProductEntity
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ProductViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    var sellingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    if (showDialog) {
        AddProductDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name, category, cost, sale, qty, sku ->
                viewModel.addProduct(name, category, cost, sale, qty, sku)
                showDialog = false
            }
        )
    }
    sellingProduct?.let { product ->
        SellProductDialog(
            product = product,
            onDismiss = { sellingProduct = null },
            onConfirm = { quantity, note ->
                viewModel.sellProduct(product.id, quantity, note)
                sellingProduct = null
            }
        )
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Склад товарів") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Додати товар")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (val current = state) {
                is ProductUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ProductUiState.Error -> Text("Помилка: ${current.message}", color = Color.Red, modifier = Modifier.align(Alignment.Center))
                is ProductUiState.Success -> {
                    if (current.products.isEmpty()) {
                        Text("Товари відсутні. Натисніть +, щоб додати.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(current.products, key = { it.id }) { product ->
                                ProductCardItem(
                                    product = product,
                                    onSell = { sellingProduct = product }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellProductDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, note: String) -> Unit
) {
    var quantity by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Реалізація товару: ${product.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("В наявності: ${product.stockQuantity} шт.")
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Кількість для реалізації") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Нотатка") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val qty = quantity.toIntOrNull() ?: 0
                    if (qty > 0) {
                        onConfirm(qty, note)
                    }
                }
            ) { Text("Продати") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Скасувати") }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, cost: Double, sale: Double, qty: Int, sku: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Загальне") }
    var costPrice by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новий товар") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Назва") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Категорія") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = costPrice, onValueChange = { costPrice = it }, label = { Text("Собівартість") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = salePrice, onValueChange = { salePrice = it }, label = { Text("Ціна продажу") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Кількість") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    name,
                    category,
                    costPrice.toDoubleOrNull() ?: 0.0,
                    salePrice.toDoubleOrNull() ?: 0.0,
                    quantity.toIntOrNull() ?: 0,
                    sku
                )
            }) { Text("Додати") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Скасувати") }
        }
    )
}
@Composable
fun ProductCardItem(
    product: ProductEntity,
    onSell: () -> Unit = {}
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (product.stockQuantity <= 5) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = "Залишок: ${product.stockQuantity} шт.",
                    fontWeight = FontWeight.Bold,
                    color = if (product.stockQuantity <= 5) Color(0xFFE65100) else Color(0xFF2E7D32)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Закупка: ${product.costPrice} грн", fontSize = 14.sp, color = Color.Gray)
                Text(text = "Продаж: ${product.salePrice} грн", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Маржа: +${product.marginPerUnit} грн", fontSize = 14.sp, color = Color(0xFF1B5E20))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onSell,
                enabled = product.stockQuantity > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Продати (Реалізувати)")
            }
        }
    }
}
