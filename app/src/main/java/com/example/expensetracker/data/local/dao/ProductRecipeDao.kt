package com.example.expensetracker.data.local.dao
import androidx.room.*
import com.example.expensetracker.data.local.entity.ProductRecipeEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface ProductRecipeDao {
    @Query("SELECT * FROM product_recipes WHERE product_id = :productId")
    fun getRecipeForProduct(productId: Long): Flow<List<ProductRecipeEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeItem(item: ProductRecipeEntity): Long
    @Delete
    suspend fun deleteRecipeItem(item: ProductRecipeEntity)
    @Query("DELETE FROM product_recipes WHERE product_id = :productId")
    suspend fun deleteRecipeForProduct(productId: Long)
}
