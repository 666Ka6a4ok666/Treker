package com.example.expensetracker.data.local.entity
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "warehouse_components")
data class WarehouseComponentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "sku")
    val sku: String = "DET-KP-01",
    @ColumnInfo(name = "stock_quantity")
    val stockQuantity: Int,
    @ColumnInfo(name = "unit_cost")
    val unitCost: Double,
    @ColumnInfo(name = "min_stock_alert")
    val minStockAlert: Int = 30,
    @ColumnInfo(name = "unit")
    val unit: String = "шт."
)
