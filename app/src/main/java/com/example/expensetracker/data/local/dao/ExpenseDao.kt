package com.example.expensetracker.data.local.dao
import androidx.room.*
import com.example.expensetracker.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY expense_date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long
    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalExpenses(): Flow<Double?>
    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity): Int
}
