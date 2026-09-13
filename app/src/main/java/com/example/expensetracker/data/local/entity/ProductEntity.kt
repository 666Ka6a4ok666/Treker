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
    val costPrice: Double, // Собівартість за одиницю

    @ColumnInfo(name = "sale_price")
    val salePrice: Double, // Ціна реалізації

    @ColumnInfo(name = "stock_quantity")
    val stockQuantity: Int, // Кількість на складі

    @ColumnInfo(name = "sku")
    val sku: String = "",

    @ColumnInfo(name = "component_id")
    val componentId: Long? = 1L, // ID "Короткоплече"

    @ColumnInfo(name = "components_required_per_unit")
    val componentsRequiredPerUnit: Int = 2, // На 1 виріб витрачається 2 короткоплечих

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    // Обчислюваний маржинальний прибуток на 1 шт
    val marginPerUnit: Double get() = salePrice - costPrice
}