package com.example.expensetracker.data.repository
import com.example.expensetracker.data.local.dao.ExpenseDao
import com.example.expensetracker.data.local.dao.ProductDao
import com.example.expensetracker.data.local.dao.SaleDao
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.data.local.entity.ProductEntity
import com.example.expensetracker.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class AppRepository @Inject constructor(
    private val productDao: ProductDao,
    private val saleDao: SaleDao,
    private val expenseDao: ExpenseDao
) {
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val totalRevenue: Flow<Double> = saleDao.getTotalRevenue()
    val totalGrossProfit: Flow<Double> = saleDao.getTotalGrossProfit()
    val totalExpenses: Flow<Double> = expenseDao.getTotalExpenses()
    val netProfit: Flow<Double> = combine(
        totalGrossProfit,
        totalExpenses
    ) { profit, expenses ->
        profit - expenses
    }
    suspend fun addProduct(product: ProductEntity): Long {
        return productDao.insertProduct(product)
    }
    suspend fun addExpense(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }
    suspend fun sellProduct(productId: Long, quantity: Int, note: String = ""): Result<Unit> {
        val product = productDao.getProductById(productId)
            ?: return Result.failure(Exception("Товар не знайдено"))
        if (product.stockQuantity < quantity) {
            return Result.failure(Exception("Недостатня кількість на складі! В наявності: ${product.stockQuantity}"))
        }
        val updatedRows = productDao.decreaseStock(productId, quantity)
        if (updatedRows == 0) {
            return Result.failure(Exception("Не вдалося списати товар зі складу"))
        }
        val totalAmount = product.salePrice * quantity
        val profit = (product.salePrice - product.costPrice) * quantity
        val sale = SaleEntity(
            productId = product.id,
            productName = product.name,
            quantity = quantity,
            unitPrice = product.salePrice,
            totalAmount = totalAmount,
            profit = profit,
            note = note
        )
        saleDao.insertSale(sale)
        return Result.success(Unit)
    }
}
