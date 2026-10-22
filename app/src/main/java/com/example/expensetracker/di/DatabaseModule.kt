package com.example.expensetracker.di

import android.content.Context
import androidx.room.Room
import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.local.migration.Migrations
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
                        AppDatabase.DATABASE_NAME
        )
        .addMigrations(*Migrations.ALL)
        .build()
    }

    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()

    @Provides
    fun provideSaleDao(db: AppDatabase): SaleDao = db.saleDao()

    @Provides
    fun provideExpenseDao(db: AppDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideIncomeDao(db: AppDatabase): IncomeDao = db.incomeDao()

    @Provides
    fun provideWarehouseComponentDao(db: AppDatabase): WarehouseComponentDao = db.warehouseComponentDao()

    @Provides
    fun provideProductRecipeDao(db: AppDatabase): ProductRecipeDao = db.productRecipeDao()

    @Provides
    fun provideProductionOrderDao(db: AppDatabase): ProductionOrderDao = db.productionOrderDao()

    @Provides
    fun provideRepairDao(db: AppDatabase): RepairDao = db.repairDao()

    @Provides
    fun provideRepairPartDao(db: AppDatabase): RepairPartDao = db.repairPartDao()

    @Provides
    fun provideCashTransactionDao(db: AppDatabase): CashTransactionDao = db.cashTransactionDao()
}
