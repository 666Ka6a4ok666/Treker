package com.example.expensetracker.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.data.local.entity.IncomeEntity
import com.example.expensetracker.data.local.entity.ProductEntity
import com.example.expensetracker.data.local.entity.RepairPartEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Тести касового методу та логіки ремонту на реальній (in-memory) Room-базі.
 * Використовує Robolectric, щоб мати Context без емулятора.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AppRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: AppRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AppRepository(
            db = db,
            productDao = db.productDao(),
            saleDao = db.saleDao(),
            expenseDao = db.expenseDao(),
            incomeDao = db.incomeDao(),
            warehouseComponentDao = db.warehouseComponentDao(),
            repairDao = db.repairDao(),
            repairPartDao = db.repairPartDao(),
            cashTransactionDao = db.cashTransactionDao(),
            productRecipeDao = db.productRecipeDao(),
            productionOrderDao = db.productionOrderDao()
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    private suspend fun seedProduct(): ProductEntity {
        val product = ProductEntity(
            name = "Тест-товар",
            category = "Тест",
            costPrice = 10.0,
            salePrice = 25.0,
            stockQuantity = 10,
            sku = "T-1"
        )
        repository.addProduct(product)
        return repository.allProducts.first().first { it.sku == "T-1" }
    }

    @Test
    fun `paid sale is counted in cash revenue but credit sale is not`() = runTest {
        val product = seedProduct()

        // Оплачений продаж 2 шт.
        repository.sellProduct(product.id, 2, isCredit = false).getOrThrow()
        // Продаж у борг 3 шт. — гроші ще не надійшли.
        repository.sellProduct(product.id, 3, isCredit = true, debtorName = "Боржник").getOrThrow()

        val paidRevenue = repository.totalRevenue.first()
        // За касовим методом: лише оплачені 2 шт * 25 = 50.
        assertEquals(50.0, paidRevenue, 0.001)

        val grossProfit = repository.totalGrossProfit.first()
        // 2 шт * (25 - 10) = 30.
        assertEquals(30.0, grossProfit, 0.001)

        // Дебіторка = 3 шт * 25 = 75.
        val debt = repository.totalUnpaidDebt.first()
        assertEquals(75.0, debt, 0.001)
    }

    @Test
    fun `repaying credit sale adds money to cash and clears debt`() = runTest {
        val product = seedProduct()
        repository.sellProduct(product.id, 2, isCredit = true, debtorName = "Клієнт").getOrThrow()

        assertEquals(0.0, repository.totalRevenue.first(), 0.001)
        assertEquals(50.0, repository.totalUnpaidDebt.first(), 0.001)

        val creditSale = repository.allSales.first().first { it.isCredit }
        repository.markSaleAsPaid(creditSale.id)

        // Після погашення боргу гроші надійшли -> виручка 50, борг 0.
        assertEquals(50.0, repository.totalRevenue.first(), 0.001)
        assertEquals(0.0, repository.totalUnpaidDebt.first(), 0.001)
        assertEquals(0.0, repository.netProfit.first() - 50.0, 0.001)
    }

    @Test
    fun `expense reduces net cash by its amount`() = runTest {
        repository.addExpense(ExpenseEntity(title = "Оренда", category = "Оренда", amount = 200.0))
        assertEquals(-200.0, repository.netProfit.first(), 0.001)
    }

    @Test
    fun `uncompensated repair creates cost but no income until compensated`() = runTest {
        val parts = listOf(
            RepairPartEntity(
                repairId = 0,
                name = "Запчастина",
                componentId = null,
                quantity = 2,
                unitCost = 15.0
            )
        )
        val result = repository.createRepair(
            assetName = "Насос",
            clientName = "Клієнт",
            description = "Заміна ущільнювача",
            laborCost = 40.0,
            parts = parts,
            compensationAmount = 100.0,
            compensationPaid = false
        )
        val repairId = result.getOrThrow()

        // Каса: витрати на запчастини(30) + роботи(40) = -70, компенсації ще немає.
        assertEquals(-70.0, repository.netProfit.first(), 0.001)
        assertEquals(100.0, repository.totalRepairDebt.first(), 0.001)

        repository.markRepairCompensated(repairId)

        // Тепер надійшла компенсація 100 -> каса = 30.
        assertEquals(30.0, repository.netProfit.first(), 0.001)
        assertEquals(0.0, repository.totalRepairDebt.first(), 0.001)
    }

    @Test
    fun `deleting expense removes its cash impact`() = runTest {
        repository.addExpense(ExpenseEntity(title = "Оренда", category = "Оренда", amount = 200.0))
        assertEquals(-200.0, repository.netProfit.first(), 0.001)

        val expense = repository.allExpenses.first().first()
        repository.deleteExpense(expense.id)

        // Після видалення витрати каса має повернутись до нуля.
        assertEquals(0.0, repository.netProfit.first(), 0.001)
    }

    @Test
    fun `deleting income removes its cash impact`() = runTest {
        repository.addIncome(IncomeEntity(title = "Грант", category = "Гранти", amount = 500.0))
        assertEquals(500.0, repository.netProfit.first(), 0.001)

        val income = repository.allIncomes.first().first()
        repository.deleteIncome(income.id)

        assertEquals(0.0, repository.netProfit.first(), 0.001)
    }

    @Test
    fun `income adds cash inflow`() = runTest {
        repository.addIncome(IncomeEntity(title = "Грант", category = "Гранти", amount = 500.0))
        assertEquals(500.0, repository.netProfit.first(), 0.001)
    }

    // --- ВИРОБНИЦТВО ---

    private suspend fun seedComponent(name: String, stock: Int, unitCost: Double) =
        repository.addComponent(name = name, stockQuantity = stock, unitCost = unitCost)

    @Test
    fun `production consumes components adds finished goods and records cost`() = runTest {
        val product = seedProduct()
        val compA = seedComponent("Гвинт", stock = 20, unitCost = 2.0)
        val compB = seedComponent("Корпус", stock = 10, unitCost = 5.0)

        // Рецепт: 2 гвинти + 1 корпус на 1 шт продукту.
        repository.upsertRecipeItem(product.id, compA, 2).getOrThrow()
        repository.upsertRecipeItem(product.id, compB, 1).getOrThrow()

        val startStock = repository.allProducts.first().first { it.id == product.id }.stockQuantity

        // Виробляємо 3 шт.
        val batchCost = repository.produceProduct(product.id, 3).getOrThrow()

        // Собівартість партії: 3*(2*2 + 1*5) = 3*9 = 27.
        assertEquals(27.0, batchCost, 0.001)

        // Готова продукція +3.
        val updated = repository.allProducts.first().first { it.id == product.id }
        assertEquals(startStock + 3, updated.stockQuantity)

        // Складники списано.
        val comps = repository.allComponents.first()
        assertEquals(20 - 6, comps.first { it.id == compA }.stockQuantity)
        assertEquals(10 - 3, comps.first { it.id == compB }.stockQuantity)

        // Каса: видаток на виробництво 27.
        assertEquals(-27.0, repository.netProfit.first(), 0.001)
    }

    @Test
    fun `production fails when component stock insufficient`() = runTest {
        val product = seedProduct()
        val comp = seedComponent("Гвинт", stock = 1, unitCost = 2.0)
        repository.upsertRecipeItem(product.id, comp, 2).getOrThrow()

        val result = repository.produceProduct(product.id, 5)
        assertTrue(result.isFailure)

        // Нічого не списано, грошей не рухали.
        assertEquals(0.0, repository.netProfit.first(), 0.001)
        assertEquals(1, repository.allComponents.first().first { it.id == comp }.stockQuantity)
    }

    @Test
    fun `production fails when recipe empty`() = runTest {
        val product = seedProduct()
        val result = repository.produceProduct(product.id, 1)
        assertTrue(result.isFailure)
        assertEquals(0.0, repository.netProfit.first(), 0.001)
    }

    @Test
    fun `recipe upsert updates existing component quantity`() = runTest {
        val product = seedProduct()
        val comp = seedComponent("Гвинт", stock = 100, unitCost = 1.0)

        repository.upsertRecipeItem(product.id, comp, 2).getOrThrow()
        repository.upsertRecipeItem(product.id, comp, 5).getOrThrow()

        val recipe = repository.recipeForProduct(product.id).first()
        assertEquals(1, recipe.size)
        assertEquals(5, recipe.first().quantityRequired)
    }

    @Test
    fun `removing recipe item deletes it`() = runTest {
        val product = seedProduct()
        val comp = seedComponent("Гвинт", stock = 100, unitCost = 1.0)
        repository.upsertRecipeItem(product.id, comp, 2).getOrThrow()
        assertEquals(1, repository.recipeForProduct(product.id).first().size)

        repository.removeRecipeItem(product.id, comp)
        assertEquals(0, repository.recipeForProduct(product.id).first().size)
    }
}
