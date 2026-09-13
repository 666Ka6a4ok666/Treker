package com.example.expensetracker.data.local
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.expensetracker.data.local.dao.ExpenseDao
import com.example.expensetracker.data.local.dao.ProductDao
import com.example.expensetracker.data.local.dao.ProductRecipeDao
import com.example.expensetracker.data.local.dao.ProductionOrderDao
import com.example.expensetracker.data.local.dao.SaleDao
import com.example.expensetracker.data.local.dao.WarehouseComponentDao
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.data.local.entity.ProductEntity
import com.example.expensetracker.data.local.entity.SaleEntity
import com.example.expensetracker.data.local.entity.ProductRecipeEntity
import com.example.expensetracker.data.local.entity.ProductionOrderEntity
import com.example.expensetracker.data.local.entity.WarehouseComponentEntity
@Database(
    entities = [
        ProductEntity::class,
        SaleEntity::class,
        ExpenseEntity::class,
        WarehouseComponentEntity::class,
        ProductRecipeEntity::class,
        ProductionOrderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun warehouseComponentDao(): WarehouseComponentDao
    abstract fun productRecipeDao(): ProductRecipeDao
    abstract fun productionOrderDao(): ProductionOrderDao
    companion object {
        const val DATABASE_NAME = "expense_tracker_db"
    }
}
