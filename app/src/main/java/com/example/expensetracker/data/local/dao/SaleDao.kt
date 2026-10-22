package com.example.expensetracker.data.local.dao

import androidx.room.*
import com.example.expensetracker.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
        @Query("SELECT * FROM sales ORDER BY sale_date DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getSaleById(id: Long): SaleEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSale(sale: SaleEntity): Long

    @Query("SELECT SUM(total_amount) FROM sales")
    fun getTotalRevenue(): Flow<Double?>

            @Query("SELECT SUM(profit) FROM sales")
    fun getTotalGrossProfit(): Flow<Double?>

    /** Касовий метод: виручка лише за фактично оплаченими продажами. */
    @Query("SELECT COALESCE(SUM(total_amount), 0.0) FROM sales WHERE is_paid = 1")
    fun getPaidRevenue(): Flow<Double>

    /** Касовий метод: валовий прибуток лише за фактично оплаченими продажами. */
    @Query("SELECT COALESCE(SUM(profit), 0.0) FROM sales WHERE is_paid = 1")
    fun getPaidGrossProfit(): Flow<Double>

    @Query("SELECT SUM(total_amount) FROM sales WHERE is_credit = 1 AND is_paid = 0")
    fun getTotalUnpaidDebt(): Flow<Double?>

    @Query("UPDATE sales SET is_paid = 1, paid_at = :timestamp WHERE id = :saleId")
    suspend fun markSaleAsPaid(saleId: Long, timestamp: Long)

    @Query("DELETE FROM sales WHERE id = :saleId")
    suspend fun deleteSaleById(saleId: Long)
}
