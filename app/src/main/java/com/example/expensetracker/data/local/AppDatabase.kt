package com.example.expensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.expensetracker.data.local.dao.CashTransactionDao
import com.example.expensetracker.data.local.dao.ExpenseDao
import com.example.expensetracker.data.local.dao.IncomeDao
import com.example.expensetracker.data.local.dao.ProductDao
import com.example.expensetracker.data.local.dao.ProductRecipeDao
import com.example.expensetracker.data.local.dao.ProductionOrderDao
import com.example.expensetracker.data.local.dao.RepairDao
import com.example.expensetracker.data.local.dao.RepairPartDao
import com.example.expensetracker.data.local.dao.SaleDao
import com.example.expensetracker.data.local.dao.WarehouseComponentDao
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.data.local.entity.IncomeEntity
import com.example.expensetracker.data.local.entity.ProductEntity
import com.example.expensetracker.data.local.entity.ProductRecipeEntity
import com.example.expensetracker.data.local.entity.CashTransactionEntity
import com.example.expensetracker.data.local.entity.ProductionOrderEntity
import com.example.expensetracker.data.local.entity.RepairEntity
import com.example.expensetracker.data.local.entity.RepairPartEntity
import com.example.expensetracker.data.local.entity.SaleEntity
import com.example.expensetracker.data.local.entity.WarehouseComponentEntity

@Database(
    entities = [
        ProductEntity::class,
        SaleEntity::class,
        ExpenseEntity::class,
        IncomeEntity::class,
        WarehouseComponentEntity::class,
                ProductRecipeEntity::class,
        ProductionOrderEntity::class,
        RepairEntity::class,
        RepairPartEntity::class,
        CashTransactionEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun warehouseComponentDao(): WarehouseComponentDao
    abstract fun productRecipeDao(): ProductRecipeDao
        abstract fun productionOrderDao(): ProductionOrderDao
    abstract fun repairDao(): RepairDao
    abstract fun repairPartDao(): RepairPartDao
    abstract fun cashTransactionDao(): CashTransactionDao

    companion object {
        const val DATABASE_NAME = "expense_tracker_db"
    }
}
