package com.example.expensetracker.data.local.dao
import androidx.room.*
import com.example.expensetracker.data.local.entity.ProductRecipeEntity
import com.example.expensetracker.data.local.entity.RecipeItemWithComponent
import kotlinx.coroutines.flow.Flow
@Dao
interface ProductRecipeDao {
    @Query("SELECT * FROM product_recipes WHERE product_id = :productId")
    fun getRecipeForProduct(productId: Long): Flow<List<ProductRecipeEntity>>

    /** Рецепт із підтягнутими даними складників (назва, SKU, собівартість, залишок). */
    @Query(
        """
        SELECT c.id AS componentId,
               c.name AS componentName,
               c.sku AS componentSku,
               c.unit_cost AS unitCost,
               c.stock_quantity AS currentStock,
               r.quantity_required AS quantityRequired
        FROM product_recipes r
        INNER JOIN warehouse_components c ON c.id = r.component_id
        WHERE r.product_id = :productId
        ORDER BY c.name ASC
        """
    )
    fun getRecipeWithComponents(productId: Long): Flow<List<RecipeItemWithComponent>>

    /** Одноразове (suspend) читання рецепта із деталями — для операції виробництва в транзакції. */
    @Query(
        """
        SELECT c.id AS componentId,
               c.name AS componentName,
               c.sku AS componentSku,
               c.unit_cost AS unitCost,
               c.stock_quantity AS currentStock,
               r.quantity_required AS quantityRequired
        FROM product_recipes r
        INNER JOIN warehouse_components c ON c.id = r.component_id
        WHERE r.product_id = :productId
        ORDER BY c.name ASC
        """
    )
    suspend fun getRecipeWithComponentsOnce(productId: Long): List<RecipeItemWithComponent>

    @Query("SELECT * FROM product_recipes WHERE product_id = :productId AND component_id = :componentId LIMIT 1")
    suspend fun getRecipeItem(productId: Long, componentId: Long): ProductRecipeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeItem(item: ProductRecipeEntity): Long

    @Delete
    suspend fun deleteRecipeItem(item: ProductRecipeEntity)

    @Query("DELETE FROM product_recipes WHERE id = :recipeId")
    suspend fun deleteRecipeItemById(recipeId: Long)

    @Query("DELETE FROM product_recipes WHERE product_id = :productId")
    suspend fun deleteRecipeForProduct(productId: Long)
}
