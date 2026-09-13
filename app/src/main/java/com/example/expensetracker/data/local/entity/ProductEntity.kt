package com.example.expensetracker.data.local.entity
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "cost_price")
    val costPrice: Double,
    @ColumnInfo(name = "sale_price")
    val salePrice: Double,
    @ColumnInfo(name = "stock_quantity")
    val stockQuantity: Int,
    @ColumnInfo(name = "sku")
    val sku: String = "",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)