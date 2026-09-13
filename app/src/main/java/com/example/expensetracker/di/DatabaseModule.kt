package com.example.expensetracker.di
import android.content.Context
import androidx.room.Room
import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.local.dao.ExpenseDao
import com.example.expensetracker.data.local.dao.ProductDao
import com.example.expensetracker.data.local.dao.SaleDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "expense_tracker_db"
        ).fallbackToDestructiveMigration().build()
    }
    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao = database.productDao()
    @Provides
    fun provideSaleDao(database: AppDatabase): SaleDao = database.saleDao()
    @Provides
    fun provideExpenseDao(database: AppDatabase): ExpenseDao = database.expenseDao()
}