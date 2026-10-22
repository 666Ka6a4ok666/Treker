package com.example.expensetracker.ui.products

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Search
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
import com.example.expensetracker.data.local.entity.ProductEntity
import com.example.expensetracker.ui.theme.ExpenseRed
import com.example.expensetracker.ui.theme.ExpenseRedContainer
import com.example.expensetracker.ui.theme.ProfitGreen
import com.example.expensetracker.ui.theme.ProfitGreenContainer
import com.example.expensetracker.ui.theme.WarningAmber
import com.example.expensetracker.ui.theme.WarningAmberContainer
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ProductViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
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
            onConfirm = { quantity, note, isCredit, debtorName ->
                viewModel.sellProduct(product.id, quantity, note, isCredit, debtorName)
                sellingProduct = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Склад товарів", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Новий товар") },
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
            when (val current = state) {
                is ProductUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ProductUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Помилка: ${current.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is ProductUiState.Success -> {
                    val products = current.products
                    val categories = remember(products) {
                        products.map { it.category }.filter { it.isNotBlank() }.distinct()
                    }

                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        placeholder = { Text("Пошук за назвою або SKU...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Очистити")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Categories chip row
                    if (categories.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("Всі (${products.size})") }
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
                    }

                    val filteredProducts = remember(products, searchQuery, selectedCategory) {
                        products.filter { product ->
                            val matchesSearch = searchQuery.isBlank() ||
                                    product.name.contains(searchQuery, ignoreCase = true) ||
                                    product.sku.contains(searchQuery, ignoreCase = true)
                            val matchesCategory = selectedCategory == null || product.category == selectedCategory
                            matchesSearch && matchesCategory
                        }
                    }

                    if (filteredProducts.isEmpty()) {
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
                                        imageVector = Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Text(
                                    text = if (searchQuery.isNotEmpty() || selectedCategory != null) "Нічого не знайдено" else "Товари відсутні",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (searchQuery.isNotEmpty() || selectedCategory != null)
                                        "Спробуйте змінити фільтри або пошуковий запит"
                                    else
                                        "Натисніть «Новий товар», щоб додати першу позицію.",
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
                            items(filteredProducts, key = { it.id }) { product ->
                                var showDeleteConfirm by remember { mutableStateOf(false) }
                                if (showDeleteConfirm) {
                                    AlertDialog(
                                        onDismissRequest = { showDeleteConfirm = false },
                                        title = { Text("Видалити товар?") },
                                        text = { Text("«${product.name}» буде видалено зі складу.") },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    viewModel.deleteProduct(product)
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
                                ProductCardItem(
                                    product = product,
                                    onSell = { sellingProduct = product },
                                    onDelete = { showDeleteConfirm = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCardItem(
    product: ProductEntity,

    onSell: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val (statusText, statusBg, statusFg) = when {
        product.stockQuantity == 0 -> Triple("Немає в наявності", ExpenseRedContainer, ExpenseRed)
        product.stockQuantity <= 5 -> Triple("Закінчується: ${product.stockQuantity} шт.", WarningAmberContainer, WarningAmber)
        else -> Triple("В наявності: ${product.stockQuantity} шт.", ProfitGreenContainer, ProfitGreen)
    }

    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (product.category.isNotBlank() || product.sku.isNotBlank()) {
                        Text(
                            text = listOfNotNull(
                                product.category.takeIf { it.isNotBlank() },
                                product.sku.takeIf { it.isNotBlank() }?.let { "SKU: $it" }
                            ).joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Stock status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusFg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pricing & Margin Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Собівартість", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₴ " + String.format(Locale.US, "%.2f", product.costPrice), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Ціна продажу", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₴ " + String.format(Locale.US, "%.2f", product.salePrice), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                }
                Column {
                    Text("Маржа", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("+₴ " + String.format(Locale.US, "%.2f", product.marginPerUnit), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ProfitGreen)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSell,
                enabled = product.stockQuantity > 0,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (product.stockQuantity > 0) "Реалізувати (Продати)" else "Немає в наявності")
            }
        }
    }
}

@Composable
fun SellProductDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, note: String, isCredit: Boolean, debtorName: String) -> Unit
) {
    var quantity by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }
    var isCredit by remember { mutableStateOf(false) }
    var debtorName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Продаж: ${product.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Доступно на складі: ${product.stockQuantity} шт.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Кількість для продажу") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("В борг / під реалізацію", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isCredit, onCheckedChange = { isCredit = it })
                }

                if (isCredit) {
                    OutlinedTextField(
                        value = debtorName,
                        onValueChange = { debtorName = it },
                        label = { Text("ПІБ / Назва покупця") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Нотатка (коментар)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantity.toIntOrNull() ?: 0
                    if (qty > 0 && qty <= product.stockQuantity) {
                        onConfirm(qty, note, isCredit, debtorName)
                    }
                }
            ) { Text("Підтвердити") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Скасувати") }
        }
    )
}

@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, cost: Double, sale: Double, qty: Int, sku: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Загальне") }
    var costPrice by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var sku by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новий товар на склад") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Назва товару") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Категорія") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("Артикул (SKU)") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = costPrice,
                        onValueChange = { costPrice = it },
                        label = { Text("Собівартість") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = salePrice,
                        onValueChange = { salePrice = it },
                        label = { Text("Ціна продажу") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Початковий залишок (шт)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val cost = costPrice.toDoubleOrNull() ?: 0.0
                val sale = salePrice.toDoubleOrNull() ?: 0.0
                val qty = quantity.toIntOrNull() ?: 0
                if (name.isNotBlank()) {
                    onConfirm(name.trim(), category.trim(), cost, sale, qty, sku.trim())
                }
            }) { Text("Додати на склад") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Скасувати") }
        }
    )
}
