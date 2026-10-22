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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.local.entity.ProductEntity
import com.example.expensetracker.data.local.entity.SaleEntity
import com.example.expensetracker.ui.common.DatePeriod
import com.example.expensetracker.ui.common.DatePeriodChips
import com.example.expensetracker.ui.common.formatDateTime
import com.example.expensetracker.ui.sales.SalesViewModel
import com.example.expensetracker.ui.theme.ProfitGreen
import com.example.expensetracker.ui.theme.ProfitGreenContainer
import com.example.expensetracker.ui.theme.WarningAmber
import com.example.expensetracker.ui.theme.WarningAmberContainer
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(viewModel: SalesViewModel) {
    val sales by viewModel.salesList.collectAsState()
    val products by viewModel.products.collectAsState()
    val totalUnpaidDebt by viewModel.totalUnpaidDebt.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddBottomSheet by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Всі") } // Всі / В борг (Неоплачені) / Оплачені
    var selectedPeriod by remember { mutableStateOf(DatePeriod.ALL) }

    LaunchedEffect(Unit) {
        viewModel.eventMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Продажі та Реалізація", fontWeight = FontWeight.Bold) },
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
                text = { Text("Зареєструвати продаж") },
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
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Всі", "В борг (Неоплачені)", "Оплачені").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }

            DatePeriodChips(
                selected = selectedPeriod,
                onSelect = { selectedPeriod = it }
            )

            val filteredSales = remember(sales, selectedFilter, selectedPeriod) {
                val byStatus = when (selectedFilter) {
                    "В борг (Неоплачені)" -> sales.filter { it.isCredit && !it.isPaid }
                    "Оплачені" -> sales.filter { it.isPaid }
                    else -> sales
                }
                byStatus.filter { selectedPeriod.contains(it.saleDate) }
            }

            if (filteredSales.isEmpty()) {
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
                                imageVector = Icons.Default.PointOfSale,
                                contentDescription = null,
                                tint = ProfitGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = if (selectedFilter != "Всі") "Записів у категорії «$selectedFilter» не знайдено" else "Продажі відсутні",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Натисніть «Зареєструвати продаж», щоб обрати товар та здійснити реалізацію.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val totalRevenue = remember(filteredSales) { filteredSales.sumOf { it.totalAmount } }
                val totalProfit = remember(filteredSales) { filteredSales.sumOf { it.profit } }

                // Summary Banner
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (totalUnpaidDebt > 0) WarningAmberContainer else ProfitGreenContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Загальна виручка",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "+₴ " + String.format(Locale.US, "%,.2f", totalRevenue),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ProfitGreen
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Валовий прибуток",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "+₴ " + String.format(Locale.US, "%,.2f", totalProfit),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ProfitGreen
                                )
                            }
                        }

                        if (totalUnpaidDebt > 0) {
                            HorizontalDivider(color = WarningAmber.copy(alpha = 0.3f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Борги покупців (Дебіторка):",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = WarningAmber
                                )
                                Text(
                                    text = "₴ " + String.format(Locale.US, "%,.2f", totalUnpaidDebt),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningAmber
                                )
                            }
                        }
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSales, key = { it.id }) { sale ->
                        SaleItemCard(
                            sale = sale,
                            onPayDebt = { viewModel.markSaleAsPaid(sale.id) }
                        )
                    }
                }
            }
        }

        if (showAddBottomSheet) {
            AddSaleBottomSheet(
                products = products,
                onDismiss = { showAddBottomSheet = false },
                onConfirm = { productId, qty, note, isCredit, debtorName ->
                    viewModel.sellProduct(productId, qty, note, isCredit, debtorName)
                    showAddBottomSheet = false
                }
            )
        }
    }
}

@Composable
fun SaleItemCard(
    sale: SaleEntity,
    onPayDebt: () -> Unit = {}
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (sale.isCredit && !sale.isPaid) WarningAmberContainer else ProfitGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (sale.isCredit) Icons.Default.CreditCard else Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = if (sale.isCredit && !sale.isPaid) WarningAmber else ProfitGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sale.productName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Кількість: ${sale.quantity} шт. • ₴${sale.unitPrice}/шт.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDateTime(sale.saleDate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (sale.isCredit && sale.debtorName.isNotBlank()) {
                        Text(
                            text = "Контрагент/Боржник: ${sale.debtorName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (!sale.isPaid) WarningAmber else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (sale.note.isNotBlank()) {
                        Text(
                            text = sale.note,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "+₴ " + String.format(Locale.US, "%,.2f", sale.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Прибуток: +₴" + String.format(Locale.US, "%.2f", sale.profit),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ProfitGreen
                    )
                }
            }

            // Debt status badge or button
            if (sale.isCredit) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))

                if (!sale.isPaid) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WarningAmberContainer
                        ) {
                            Text(
                                text = "В борг (НЕ ОПЛАЧЕНО)",
                                color = WarningAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Button(
                            onClick = onPayDebt,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Погасити борг", fontSize = 12.sp)
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ProfitGreenContainer
                    ) {
                        Text(
                            text = "В борг (ОПЛАЧЕНО)",
                            color = ProfitGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleBottomSheet(
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onConfirm: (productId: Long, quantity: Int, note: String, isCredit: Boolean, debtorName: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }
    var isCredit by remember { mutableStateOf(false) }
    var debtorName by remember { mutableStateOf("") }
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
                text = "Зареєструвати продаж / під реалізацію",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            ExposedDropdownMenuBox(
                expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = !expandedDropdown }
            ) {
                OutlinedTextField(
                    value = selectedProduct?.let { "${it.name} (Залишок: ${it.stockQuantity} шт.)" } ?: "Оберіть товар зі складу",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Товар") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedDropdown) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
                    if (products.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Склад порожній — додайте товари") },
                            onClick = { expandedDropdown = false }
                        )
                    } else {
                        products.forEach { product ->
                            DropdownMenuItem(
                                text = {
                                    Text("${product.name} — ${product.stockQuantity} шт. (₴${product.salePrice})")
                                },
                                onClick = {
                                    selectedProduct = product
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Кількість для реалізації") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Switch for sale on credit (в борг)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Відпуск товару в борг", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text("Під реалізацію / відстрочка платежу", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = isCredit,
                    onCheckedChange = { isCredit = it }
                )
            }

            if (isCredit) {
                OutlinedTextField(
                    value = debtorName,
                    onValueChange = { debtorName = it },
                    label = { Text("ПІБ / Назва контрагента (Боржника)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Примітка (необов'язково)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val product = selectedProduct
                    val qty = quantity.toIntOrNull() ?: 0
                    if (product != null && qty > 0 && qty <= product.stockQuantity) {
                        onConfirm(product.id, qty, note.trim(), isCredit, debtorName.trim())
                    }
                },
                enabled = selectedProduct != null && (quantity.toIntOrNull() ?: 0) > 0,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isCredit) "Оформити відпуск в борг" else "Підтвердити та продати")
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
