package com.example.expensetracker.data.local.entity
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "amount")
    val amount: Double,
    @ColumnInfo(name = "expense_date")
    val expenseDate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "note")
    val note: String = ""
)