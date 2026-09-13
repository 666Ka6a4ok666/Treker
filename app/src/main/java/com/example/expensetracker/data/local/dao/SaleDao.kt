package com.example.expensetracker.data.local.dao
import androidx.room.*
import com.example.expensetracker.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY sale_date DESC")
    fun getAllSales(): Flow<List<SaleEntity>>
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSale(sale: SaleEntity): Long
    @Query("SELECT COALESCE(SUM(total_amount), 0.0) FROM sales")
    fun getTotalRevenue(): Flow<Double>
    @Query("SELECT COALESCE(SUM(profit), 0.0) FROM sales")
    fun getTotalGrossProfit(): Flow<Double>
    @Query("DELETE FROM sales WHERE id = :saleId")
    suspend fun deleteSaleById(saleId: Long)
}
