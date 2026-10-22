package com.example.expensetracker.data.repository

import androidx.room.withTransaction
import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.local.dao.CashTransactionDao
import com.example.expensetracker.data.local.dao.ExpenseDao
import com.example.expensetracker.data.local.dao.IncomeDao
import com.example.expensetracker.data.local.dao.ProductDao
import com.example.expensetracker.data.local.dao.ProductRecipeDao
import com.example.expensetracker.data.local.dao.ProductionOrderDao
import com.example.expensetracker.data.local.dao.RepairDao
import com.example.expensetracker.data.local.dao.RepairPartDao
import com.example.expensetracker.data.local.dao.SaleDao
import com.example.expensetracker.data.local.dao.WarehouseComponentDao
import com.example.expensetracker.data.local.entity.CashTransactionEntity
import com.example.expensetracker.data.local.entity.CashTransactionType
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.data.local.entity.IncomeEntity
import com.example.expensetracker.data.local.entity.ProductEntity
import com.example.expensetracker.data.local.entity.ProductRecipeEntity
import com.example.expensetracker.data.local.entity.ProductionOrderEntity
import com.example.expensetracker.data.local.entity.RecipeItemWithComponent
import com.example.expensetracker.data.local.entity.RepairEntity
import com.example.expensetracker.data.local.entity.RepairPartEntity
import com.example.expensetracker.data.local.entity.SaleEntity
import com.example.expensetracker.data.local.entity.WarehouseComponentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val db: AppDatabase,
    private val productDao: ProductDao,
    private val saleDao: SaleDao,




        private val expenseDao: ExpenseDao,
    private val incomeDao: IncomeDao,
    private val warehouseComponentDao: WarehouseComponentDao,
    private val repairDao: RepairDao,
    private val repairPartDao: RepairPartDao,
    private val cashTransactionDao: CashTransactionDao,
    private val productRecipeDao: ProductRecipeDao,
    private val productionOrderDao: ProductionOrderDao
) {
    private val inflowTypes = CashTransactionType.entries.filter { it.isInflow }.map { it.name }
    private val outflowTypes = CashTransactionType.entries.filter { !it.isInflow }.map { it.name }

        val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val allIncomes: Flow<List<IncomeEntity>> = incomeDao.getAllIncomes()
    val allComponents: Flow<List<WarehouseComponentEntity>> = warehouseComponentDao.getAllComponents()
        val allRepairs: Flow<List<RepairEntity>> = repairDao.getAllRepairs()
    val allCashTransactions: Flow<List<CashTransactionEntity>> = cashTransactionDao.getAll()
    val allProductionOrders: Flow<List<ProductionOrderEntity>> = productionOrderDao.getAllOrders()

    /** Рецепт продукту із деталями складників (для UI редагування рецептів). */
    fun recipeForProduct(productId: Long): Flow<List<RecipeItemWithComponent>> =
        productRecipeDao.getRecipeWithComponents(productId)

    // --- КАСОВИЙ МЕТОД: усе рахується лише з фактичного руху грошей ---

    /** Реально отримані гроші (усі надходження). */
    val totalCashInflow: Flow<Double> = cashTransactionDao.getTotalInflow(inflowTypes)

    /** Реально сплачені гроші (усі видатки). */
    val totalCashOutflow: Flow<Double> = cashTransactionDao.getTotalOutflow(outflowTypes)

    /** Фактично отримана виручка від продажів. */
    val totalRevenue: Flow<Double> = saleDao.getPaidRevenue()

    /** Фактично отриманий валовий прибуток від продажів (лише оплачені). */
    val totalGrossProfit: Flow<Double> = saleDao.getPaidGrossProfit()

    /** Фактично сплачені витрати (довільні витрати + собівартість ремонтів/виробництва). */
    val totalExpenses: Flow<Double> = totalCashOutflow

    /** Фактично отримані інші надходження (волонтерські/інвестиції/гранти). */
    val totalIncome: Flow<Double> = cashTransactionDao.getTotalByType(CashTransactionType.OTHER_INCOME.name)

    /** Дебіторка за продажами в борг (ще не оплачено). */
    val totalUnpaidDebt: Flow<Double> = saleDao.getTotalUnpaidDebt().map { it ?: 0.0 }

    /** Дебіторка за ремонтами (компенсацію ще не отримано). */
    val totalRepairDebt: Flow<Double> = repairDao.getUncompensatedDebt()

    /** Чистий рух грошей (каса): надходження − видатки. */
    val netProfit: Flow<Double> = combine(
        totalCashInflow,
        totalCashOutflow
    ) { inflow, outflow ->
        inflow - outflow
    }

    suspend fun addProduct(product: ProductEntity): Long {
        return productDao.insertProduct(product)
    }

        suspend fun addExpense(expense: ExpenseEntity): Long {
        return db.withTransaction {
            val id = expenseDao.insertExpense(expense)
            // Касовий метод: проведена витрата одразу списує гроші з каси.
            cashTransactionDao.insert(
                CashTransactionEntity(
                    type = CashTransactionType.EXPENSE,
                    amount = expense.amount,
                    description = expense.title,
                    sourceId = id,
                    createdAt = expense.expenseDate
                )
            )
            id
        }
    }

    suspend fun addIncome(income: IncomeEntity): Long {
        return db.withTransaction {
            val id = incomeDao.insertIncome(income)
            // Касовий метод: надходження одразу зараховується в касу.
            cashTransactionDao.insert(
                CashTransactionEntity(
                    type = CashTransactionType.OTHER_INCOME,
                    amount = income.amount,
                    description = income.title,
                    sourceId = id,
                    createdAt = income.incomeDate
                )
            )
            id
        }
    }

        suspend fun deleteExpense(expenseId: Long) {
        db.withTransaction {
            val expense = expenseDao.getExpenseById(expenseId) ?: return@withTransaction
            expenseDao.deleteExpense(expense)
            // Прибираємо відповідний рух грошей, щоб каса залишалась коректною.
            cashTransactionDao.deleteBySource(expenseId, CashTransactionType.EXPENSE.name)
        }
    }

    suspend fun deleteIncome(incomeId: Long) {
        db.withTransaction {
            val income = incomeDao.getIncomeById(incomeId) ?: return@withTransaction
            incomeDao.deleteIncome(income)
            cashTransactionDao.deleteBySource(incomeId, CashTransactionType.OTHER_INCOME.name)
        }
    }

    suspend fun addProductStock(productId: Long, quantity: Int) {
        val product = productDao.getProductById(productId) ?: return
        productDao.updateProduct(product.copy(stockQuantity = product.stockQuantity + quantity))
    }

        suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    /** Додає комплектуючу/сировину на склад. Повертає id створеного запису. */
    suspend fun addComponent(
        name: String,
        stockQuantity: Int,
        unitCost: Double,
        sku: String = "",
        unit: String = "шт."
    ): Long {
        return warehouseComponentDao.insertComponent(
            WarehouseComponentEntity(
                name = name,
                sku = sku.ifBlank { name.take(6).uppercase() + "-" + System.currentTimeMillis().toString().takeLast(4) },
                stockQuantity = stockQuantity,
                unitCost = unitCost,
                unit = unit
            )
        )
    }

    /**
     * Позначає продаж як оплачений і фіксує фактичне надходження грошей у касу.
     * Саме цей метод робить борг «грошима» за касовим методом.
     */
    suspend fun markSaleAsPaid(saleId: Long) {
        db.withTransaction {
            val sale = saleDao.getSaleById(saleId) ?: return@withTransaction
            if (sale.isPaid) return@withTransaction
            val now = System.currentTimeMillis()
            saleDao.markSaleAsPaid(saleId, now)
            cashTransactionDao.insert(
                CashTransactionEntity(
                    type = if (sale.isCredit) {
                        CashTransactionType.DEBT_REPAYMENT
                    } else {
                        CashTransactionType.SALE_INCOME
                    },
                    amount = sale.totalAmount,
                    description = "Оплата за ${sale.productName} (${sale.quantity} шт.)",
                    sourceId = sale.id,
                    createdAt = now
                )
            )
        }
    }

    suspend fun sellProduct(
        productId: Long,
        quantity: Int,
        note: String = "",
        isCredit: Boolean = false,
        debtorName: String = ""
    ): Result<Unit> {
        return try {
            db.withTransaction {
                val product = productDao.getProductById(productId)
                    ?: return@withTransaction Result.failure(Exception("Товар не знайдено"))
                if (product.stockQuantity < quantity) {
                    return@withTransaction Result.failure(Exception("Недостатня кількість на складі! В наявності: ${product.stockQuantity}"))
                }
                val updatedRows = productDao.decreaseStock(productId, quantity)
                if (updatedRows == 0) {
                    return@withTransaction Result.failure(Exception("Не вдалося списати товар зі складу"))
                }
                val totalAmount = product.salePrice * quantity
                val profit = (product.salePrice - product.costPrice) * quantity
                                val now = System.currentTimeMillis()
                val sale = SaleEntity(
                    productId = product.id,
                    productName = product.name,
                    quantity = quantity,
                    unitPrice = product.salePrice,
                    totalAmount = totalAmount,
                    profit = profit,
                    note = note,
                    isCredit = isCredit,
                    debtorName = if (isCredit) debtorName else "",
                    isPaid = !isCredit,
                    paidAt = if (!isCredit) now else null
                )
                val saleId = saleDao.insertSale(sale)
                // Касовий метод: оплачений (не в борг) продаж одразу дає гроші в касу.
                if (!isCredit) {
                    cashTransactionDao.insert(
                        CashTransactionEntity(
                            type = CashTransactionType.SALE_INCOME,
                            amount = totalAmount,
                            description = "Продаж: ${product.name} (${quantity} шт.)",
                            sourceId = saleId,
                            createdAt = now
                        )
                    )
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- РЕМОНТ ---

    /**
     * Створює замовлення на ремонт, фіксує сплачені запчастини та проводить
     * фактичні видатки на ремонт (запчастини + роботи) через касу.
     *
     * @param compensationAmount сума, яку відшкодує клієнт (0 — внутрішній ремонт).
     * @param compensationPaid чи компенсацію сплачено одразу (касовий метод).
     */
    suspend fun createRepair(
        assetName: String,
        clientName: String,
        description: String,
        laborCost: Double,
        parts: List<RepairPartEntity>,
        compensationAmount: Double,
        compensationPaid: Boolean
    ): Result<Long> {
        return try {
            db.withTransaction {
                val now = System.currentTimeMillis()
                val partsCost = parts.sumOf { it.totalCost }
                val repairId = repairDao.insertRepair(
                    RepairEntity(
                        assetName = assetName,
                        clientName = clientName,
                        description = description,
                        laborCost = laborCost,
                        partsCost = partsCost,
                        compensationAmount = compensationAmount,
                        isCompensated = compensationPaid && compensationAmount > 0,
                        repairDate = now,
                        compensatedAt = if (compensationPaid && compensationAmount > 0) now else null
                    )
                )
                // Списуємо зі складу (якщо запчастина прив'язана до комплектуючої) + фіксуємо витрати на запчастини.
                parts.forEach { part ->
                    repairPartDao.insertPart(part.copy(repairId = repairId))
                    part.componentId?.let { warehouseComponentDao.decreaseStock(it, part.quantity) }
                }
                if (partsCost > 0) {
                    cashTransactionDao.insert(
                        CashTransactionEntity(
                            type = CashTransactionType.REPAIR_COST,
                            amount = partsCost,
                            description = "Запчастини для ремонту: $assetName",
                            sourceId = repairId,
                            createdAt = now
                        )
                    )
                }
                if (laborCost > 0) {
                    cashTransactionDao.insert(
                        CashTransactionEntity(
                            type = CashTransactionType.EXPENSE,
                            amount = laborCost,
                            description = "Роботи з ремонту: $assetName",
                            sourceId = repairId,
                            createdAt = now
                        )
                    )
                }
                if (compensationPaid && compensationAmount > 0) {
                    cashTransactionDao.insert(
                        CashTransactionEntity(
                            type = CashTransactionType.REPAIR_INCOME,
                            amount = compensationAmount,
                            description = "Компенсація ремонту: $assetName",
                            sourceId = repairId,
                            createdAt = now
                        )
                    )
                }
                Result.success(repairId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Фіксує фактичне отримання компенсації за ремонт і зараховує гроші в касу. */
    suspend fun markRepairCompensated(repairId: Long) {
        db.withTransaction {
            val repair = repairDao.getRepairById(repairId) ?: return@withTransaction
            if (repair.isCompensated || repair.compensationAmount <= 0) return@withTransaction
            val now = System.currentTimeMillis()
            repairDao.markCompensated(repairId, now)
                        cashTransactionDao.insert(
                CashTransactionEntity(
                    type = CashTransactionType.REPAIR_INCOME,
                    amount = repair.compensationAmount,
                    description = "Компенсація ремонту: ${repair.assetName}",
                    sourceId = repair.id,
                    createdAt = now
                )
            )
        }
    }

    // --- РЕЦЕПТИ (CRUD складників продукту) ---

    /** Додає або оновлює складник у рецепті продукту (upsert за парою product+component). */
    suspend fun upsertRecipeItem(productId: Long, componentId: Long, quantityRequired: Int): Result<Unit> {
        return try {
            if (quantityRequired <= 0) {
                return Result.failure(Exception("Кількість складника має бути більшою за 0"))
            }
            val existing = productRecipeDao.getRecipeItem(productId, componentId)
            if (existing != null) {
                productRecipeDao.insertRecipeItem(existing.copy(quantityRequired = quantityRequired))
            } else {
                productRecipeDao.insertRecipeItem(
                    ProductRecipeEntity(
                        productId = productId,
                        componentId = componentId,
                        quantityRequired = quantityRequired
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Видаляє складник із рецепту продукту. */
    suspend fun removeRecipeItem(productId: Long, componentId: Long) {
        val existing = productRecipeDao.getRecipeItem(productId, componentId) ?: return
        productRecipeDao.deleteRecipeItem(existing)
    }

    // --- ВИРОБНИЦТВО ---

    /**
     * Виконує виробництво продукту за його рецептом.
     *
     * Кроки (усе в одній транзакції):
     *  1. Перевіряє, що рецепт заданий і на складі достатньо КОЖНОГО складника.
     *  2. Списує потрібну кількість кожного складника зі складу.
     *  3. Зараховує готову продукцію на склад (products.stock_quantity += quantityToProduce).
     *  4. Рахує собівартість партії = сума (unitCost складника × спожита кількість).
     *  5. Проводить фактичні витрати PRODUCTION_COST у касу (касовий метод).
     *  6. Пише по одному рядку ProductionOrder на кожен спожитий складник (аудит партії).
     *
     * Собівартість партії НЕ додається до ціни продажу автоматично — лише фіксується рух грошей.
     *
     * @param updateProductCost якщо true — оновлює costPrice продукту на питому собівартість партії
     *                          (собівартість партії / кількість), щоб маржа продажу була коректною.
     * @return Result з собівартістю партії (Double) при успіху.
     */
    suspend fun produceProduct(
        productId: Long,
        quantityToProduce: Int,
        note: String = "",
        updateProductCost: Boolean = false
    ): Result<Double> {
        return try {
            db.withTransaction {
                val product = productDao.getProductById(productId)
                    ?: return@withTransaction Result.failure(Exception("Продукт не знайдено"))
                if (quantityToProduce <= 0) {
                    return@withTransaction Result.failure(Exception("Кількість має бути більшою за 0"))
                }
                val recipe = productRecipeDao.getRecipeWithComponentsOnce(productId)
                if (recipe.isEmpty()) {
                    return@withTransaction Result.failure(
                        Exception("Для «${product.name}» не задано рецепт. Додайте складники у рецепт.")
                    )
                }

                // 1. Перевірка залишків
                                recipe.forEach { item ->
                    val required = item.quantityRequired * quantityToProduce
                    if (item.currentStock < required) {
                        return@withTransaction Result.failure(
                            Exception(
                                "Недостатньо «${item.componentName}»: потрібно $required, " +
                                    "є ${item.currentStock}"
                            )
                        )
                    }
                }

                val now = System.currentTimeMillis()
                var batchCost = 0.0

                // 2-6. Списання, аудит, накопичення собівартості
                recipe.forEach { item ->
                    val required = item.quantityRequired * quantityToProduce
                    val updated = warehouseComponentDao.decreaseStock(item.componentId, required)
                    if (updated == 0) {
                        // Хтось змінив залишок паралельно — відкат транзакції.
                        throw IllegalStateException("Не вдалося списати «${item.componentName}» (залишок змінено)")
                    }
                    val lineCost = item.unitCost * required
                    batchCost += lineCost
                    productionOrderDao.insertOrder(
                        ProductionOrderEntity(
                            productId = product.id,
                            productName = product.name,
                            quantityProduced = quantityToProduce,
                            componentId = item.componentId,
                            componentName = item.componentName,
                            componentsPerUnit = item.quantityRequired,
                            componentsConsumed = required,
                            totalComponentCost = lineCost,
                            productionDate = now,
                            note = note
                        )
                    )
                }

                // 3. Готова продукція на склад
                productDao.increaseStock(product.id, quantityToProduce)

                // Опційно — питома собівартість → ціна закупки продукту
                if (updateProductCost && quantityToProduce > 0) {
                    val unitCost = batchCost / quantityToProduce
                    productDao.updateProduct(product.copy(costPrice = unitCost))
                }

                // 5. Касовий метод: фактичний видаток на сировину
                if (batchCost > 0) {
                    cashTransactionDao.insert(
                        CashTransactionEntity(
                            type = CashTransactionType.PRODUCTION_COST,
                            amount = batchCost,
                            description = "Виробництво: ${product.name} (${quantityToProduce} шт.)",
                            sourceId = product.id,
                            createdAt = now
                        )
                    )
                }
                Result.success(batchCost)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
