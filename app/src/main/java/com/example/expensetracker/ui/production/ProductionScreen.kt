package com.example.expensetracker.ui.production

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Factory
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
import com.example.expensetracker.data.local.entity.ProductionOrderEntity
import com.example.expensetracker.data.local.entity.RecipeItemWithComponent
import com.example.expensetracker.data.local.entity.WarehouseComponentEntity
import com.example.expensetracker.ui.common.formatDateTime
import com.example.expensetracker.ui.theme.ExpenseRed
import com.example.expensetracker.ui.theme.ProfitGreen
import com.example.expensetracker.ui.theme.ProfitGreenContainer
import com.example.expensetracker.ui.theme.WarningAmber
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionScreen(viewModel: ProductionViewModel) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val components by viewModel.components.collectAsStateWithLifecycle()
    val recipe by viewModel.recipe.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var updateCost by remember { mutableStateOf(true) }

    var showRecipeEditor by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { snackbarHostState.showSnackbar(it) }
    }

    // Синхронізуємо вибраний продукт зі списком (щоб бачити актуальний залишок).
    LaunchedEffect(products, selectedProduct?.id) {
        selectedProduct = selectedProduct?.let { sel -> products.firstOrNull { it.id == sel.id } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Виробництво", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ElevationProductSelector(
                products = products,
                selected = selectedProduct,
                onSelect = {
                    selectedProduct = it
                    viewModel.selectProduct(it?.id)
                }
            )

            if (selectedProduct == null) {
                EmptyProductionHint()
            } else {
                val product = selectedProduct!!
                val qty = quantity.toIntOrNull() ?: 0

                // Перевірка наявності складників та собівартість партії
                var batchCost = 0.0
                var canProduce = recipe.isNotEmpty() && qty > 0
                recipe.forEach { item ->
                    batchCost += item.unitCost * item.quantityRequired * qty
                    if (item.currentStock < item.quantityRequired * qty) canProduce = false
                }

                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Recipe card
                    item {
                        RecipeCard(
                            product = product,
                            recipe = recipe,
                            onEdit = { showRecipeEditor = true },
                            onAdd = { showRecipeEditor = true }
                        )
                    }

                    // Production controls
                    item {
                        ProductionControlsCard(
                            product = product,
                            quantity = quantity,
                            onQuantityChange = { quantity = it },
                            updateCost = updateCost,
                            onUpdateCostChange = { updateCost = it },
                            recipeEmpty = recipe.isEmpty(),
                            canProduce = canProduce,
                            batchCost = batchCost,
                            onProduce = {
                                val q = quantity.toIntOrNull() ?: 0
                                if (q > 0 && canProduce) {
                                    viewModel.produce(product.id, q, updateCost)
                                }
                            }
                        )
                    }

                    // Orders history
                    if (orders.isNotEmpty()) {
                        item {
                            Text(
                                text = "Історія виробництва",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        items(orders, key = { it.id }) { order ->
                            ProductionOrderCard(order)
                        }
                    }
                }
            }
        }

        if (showRecipeEditor && selectedProduct != null) {
            RecipeEditorSheet(
                product = selectedProduct!!,
                recipe = recipe,
                components = components,
                onDismiss = { showRecipeEditor = false },
                onAddOrUpdate = { componentId, qtyReq ->
                    viewModel.addOrUpdateRecipeItem(selectedProduct!!.id, componentId, qtyReq)
                },
                onRemove = { componentId ->
                    viewModel.removeRecipeItem(selectedProduct!!.id, componentId)
                }
            )
        }







    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ElevationProductSelector(
    products: List<ProductEntity>,
    selected: ProductEntity?,
    onSelect: (ProductEntity?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = selected?.let { "${it.name} (залишок: ${it.stockQuantity} шт.)" } ?: "Оберіть продукт для виробництва",
            onValueChange = {},
            readOnly = true,
            label = { Text("Продукт") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (products.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Немає продуктів — додайте товар на склад") },
                    onClick = { expanded = false }
                )
            } else {
                products.forEach { product ->
                    DropdownMenuItem(
                        text = { Text("${product.name} — ${product.stockQuantity} шт.") },
                        onClick = {
                            onSelect(product)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyProductionHint() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(

                    imageVector = Icons.Default.Factory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text("Оберіть продукт", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Оберіть продукт зі списку, щоб налаштувати рецепт і запустити виробництво.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecipeCard(
    product: ProductEntity,
    recipe: List<RecipeItemWithComponent>,
    onEdit: () -> Unit,
    onAdd: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Рецепт: ${product.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = if (recipe.isEmpty()) onAdd else onEdit) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (recipe.isEmpty()) "Додати" else "Редагувати")
                }
            }

            if (recipe.isEmpty()) {
                Text(
                    "Рецепт порожній. Додайте складники, щоб можна було виробляти цей продукт.",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarningAmber
                )
            } else {
                recipe.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.componentName, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${item.quantityRequired} × ₴${String.format(Locale.US, "%.2f", item.unitCost)} " +
                                "= ₴${String.format(Locale.US, "%.2f", item.unitCost * item.quantityRequired)}/од.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductionControlsCard(
    product: ProductEntity,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    updateCost: Boolean,
    onUpdateCostChange: (Boolean) -> Unit,
    recipeEmpty: Boolean,
    canProduce: Boolean,
    batchCost: Double,
    onProduce: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = ProfitGreenContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Параметри партії", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = quantity,
                onValueChange = onQuantityChange,
                label = { Text("Кількість до виробництва (шт.)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Оновити собівартість продукту", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Встановити ціну закупки = собівартість партії / кількість",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = updateCost, onCheckedChange = onUpdateCostChange)
            }

            HorizontalDivider(color = ProfitGreen.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Собівартість партії", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "₴ " + String.format(Locale.US, "%,.2f", batchCost),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ProfitGreen
                )
            }

            if (recipeEmpty) {
                Text(
                    "Спочатку додайте складники у рецепт",
                    style = MaterialTheme.typography.bodySmall,

                    color = WarningAmber
                )
            } else if (!canProduce) {
                Text(
                    "Недостатньо складників на складі для цієї кількості",
                    style = MaterialTheme.typography.bodySmall,
                    color = ExpenseRed
                )
            }

            Button(
                onClick = onProduce,
                enabled = canProduce,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen)
            ) {
                Icon(Icons.Default.Factory, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Виготовити партію")
            }
        }
    }
}

@Composable
private fun ProductionOrderCard(order: ProductionOrderEntity) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Factory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(order.productName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${order.componentName}: спожито ${order.componentsConsumed} од. (${order.componentsPerUnit}/од.)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatDateTime(order.productionDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${order.quantityProduced} шт.", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    "-₴ " + String.format(Locale.US, "%.2f", order.totalComponentCost),
                    style = MaterialTheme.typography.labelSmall,
                    color = ExpenseRed
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeEditorSheet(
    product: ProductEntity,
    recipe: List<RecipeItemWithComponent>,
    components: List<WarehouseComponentEntity>,
    onDismiss: () -> Unit,
    onAddOrUpdate: (componentId: Long, quantityRequired: Int) -> Unit,
    onRemove: (componentId: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedComponent by remember { mutableStateOf<WarehouseComponentEntity?>(null) }
    var quantityRequired by remember { mutableStateOf("1") }
    var expanded by remember { mutableStateOf(false) }

    // Складники, яких ще немає в рецепті
    val recipeComponentIds = remember(recipe) { recipe.map { it.componentId }.toSet() }
    val availableComponents = remember(components, recipeComponentIds) {
        components.filter { it.id !in recipeComponentIds }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Рецепт: ${product.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Поточні складники
            if (recipe.isEmpty()) {
                Text(
                    "Поки що немає складників",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                recipe.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.componentName, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${item.quantityRequired} од./шт. • на складі: ${item.currentStock}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onRemove(item.componentId) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Видалити", tint = ExpenseRed)
                        }
                    }
                    HorizontalDivider()
                }
            }

            Text(
                "Додати / оновити складник",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedComponent?.let { "${it.name} (склад: ${it.stockQuantity})" } ?: "Оберіть складник",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Складник") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    if (availableComponents.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Усі складники вже додані") },
                            onClick = { expanded = false }
                        )
                    } else {
                        availableComponents.forEach { comp ->
                            DropdownMenuItem(
                                text = { Text("${comp.name} — ₴${comp.unitCost}/${comp.unit}") },
                                onClick = {
                                    selectedComponent = comp
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = quantityRequired,
                onValueChange = { quantityRequired = it },
                label = { Text("Кількість на 1 шт. продукту") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val comp = selectedComponent
                    val qty = quantityRequired.toIntOrNull() ?: 0
                    if (comp != null && qty > 0) {
                        onAddOrUpdate(comp.id, qty)
                        selectedComponent = null
                        quantityRequired = "1"
                    }
                },
                enabled = selectedComponent != null && (quantityRequired.toIntOrNull() ?: 0) > 0,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Зберегти складник")
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
