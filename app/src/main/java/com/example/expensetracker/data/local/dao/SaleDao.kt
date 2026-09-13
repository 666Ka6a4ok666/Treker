package com.example.expensetracker.data.local.dao
import androidx.room.*
import com.example.expensetracker.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY sale_date DESC")
    fun getAllSales(): Flow<List<SaleEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSale(sale: SaleEntity): Long
    @Query("SELECT SUM(total_amount) FROM sales")
    fun getTotalRevenue(): Flow<Double?>
    @Query("SELECT SUM(profit) FROM sales")
    fun getTotalProfit(): Flow<Double?>
}