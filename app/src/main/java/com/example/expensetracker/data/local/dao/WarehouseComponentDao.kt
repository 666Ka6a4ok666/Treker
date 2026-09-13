package com.example.expensetracker.data.local.dao
import androidx.room.*
import com.example.expensetracker.data.local.entity.WarehouseComponentEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface WarehouseComponentDao {
    @Query("SELECT * FROM warehouse_components ORDER BY name ASC")
    fun getAllComponents(): Flow<List<WarehouseComponentEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponent(component: WarehouseComponentEntity): Long
    @Update
    suspend fun updateComponent(component: WarehouseComponentEntity)
    @Delete
    suspend fun deleteComponent(component: WarehouseComponentEntity)
    @Query("SELECT * FROM warehouse_components WHERE id = :id LIMIT 1")
    suspend fun getComponentById(id: Long): WarehouseComponentEntity?
    @Query("UPDATE warehouse_components SET stock_quantity = stock_quantity - :qty WHERE id = :componentId AND stock_quantity >= :qty")
    suspend fun decreaseStock(componentId: Long, qty: Int): Int
}
