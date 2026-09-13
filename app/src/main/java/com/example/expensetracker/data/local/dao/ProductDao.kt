package com.example.expensetracker.data.local.dao
import androidx.room.*
import com.example.expensetracker.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun getProductById(id: Long): ProductEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProduct(product: ProductEntity): Long
    @Update
    fun updateProduct(product: ProductEntity)
    @Delete
    fun deleteProduct(product: ProductEntity)
    @Query("UPDATE products SET stock_quantity = stock_quantity - :quantity WHERE id = :productId AND stock_quantity >= :quantity")
    fun reduceStock(productId: Long, quantity: Int): Int
}