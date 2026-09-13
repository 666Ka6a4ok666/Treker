package com.example.expensetracker.data.local.dao
import androidx.room.*
import com.example.expensetracker.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long
    @Update
    suspend fun updateProduct(product: ProductEntity)
    @Delete
    suspend fun deleteProduct(product: ProductEntity)
    //@Query("UPDATE products SET stock_quantity = stock_quantity - :soldQuantity WHERE id = :productId AND stock_quantity >= :soldQuantity")
    //suspend fun decreaseStock(productId: Long, soldQuantity: Int): Int
    @Query("SELECT * FROM products WHERE stock_quantity <= :minStock")
    fun getLowStockProducts(minStock: Int): Flow<List<ProductEntity>>
}
