package com.example.expensetracker.data.local.dao
import androidx.room.*
import com.example.expensetracker.data.local.entity.ProductionOrderEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface ProductionOrderDao {
    @Query("SELECT * FROM production_orders ORDER BY production_date DESC")
    fun getAllOrders(): Flow<List<ProductionOrderEntity>>
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOrder(order: ProductionOrderEntity): Long
    @Query("DELETE FROM production_orders WHERE id = :orderId")
    suspend fun deleteOrderById(orderId: Long)
}
