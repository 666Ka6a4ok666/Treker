package com.example.expensetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "category")
    val category: String, // e.g., "Волонтерські", "Інвестиції", "Гранти", "Інше"
    @ColumnInfo(name = "amount")
    val amount: Double,
    @ColumnInfo(name = "income_date")
    val incomeDate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "note")
    val note: String = ""
)
