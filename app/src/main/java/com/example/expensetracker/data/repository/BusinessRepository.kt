package com.example.expensetracker.data.repository
import androidx.room.withTransaction
import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.local.dao.ExpenseDao
import com.example.expensetracker.data.local.dao.ProductDao
import com.example.expensetracker.data.local.dao.SaleDao
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.data.local.entity.ProductEntity
import com.example.expensetracker.data.local.entity.SaleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class BusinessRepository @Inject constructor(
    private val database: AppDatabase,
    private val productDao: ProductDao,
    private val saleDao: SaleDao,
    private val expenseDao: ExpenseDao
) {
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    suspend fun registerSale(productId: Long, quantity: Int, note: String): Boolean {
        return database.withTransaction {
            val product = productDao.getProductById(productId) ?: return@withTransaction false
            if (product.stockQuantity < quantity) return@withTransaction false
            val updatedRows = productDao.reduceStock(productId, quantity)
            if (updatedRows == 0) return@withTransaction false
            val totalAmount = product.salePrice * quantity
            val profit = (product.salePrice - product.costPrice) * quantity
            val sale = SaleEntity(
                productId = productId,
                productName = product.name,
                quantity = quantity,
                unitPrice = product.salePrice,
                totalAmount = totalAmount,
                profit = profit,
                note = note
            )
            saleDao.insertSale(sale)
            true
        }
    }
    suspend fun addProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }
    suspend fun addExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.insertExpense(expense)
    }
}