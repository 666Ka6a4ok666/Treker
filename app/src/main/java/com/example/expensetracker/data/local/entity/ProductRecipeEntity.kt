package com.example.expensetracker.data.local.entity
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(
    tableName = "product_recipes",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WarehouseComponentEntity::class,
            parentColumns = ["id"],
            childColumns = ["component_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["product_id"]),
        Index(value = ["component_id"])
    ]
)
data class ProductRecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "product_id")
    val productId: Long,
    @ColumnInfo(name = "component_id")
    val componentId: Long,
    @ColumnInfo(name = "quantity_required")
    val quantityRequired: Int
)
data class RecipeItemWithComponent(
    val componentId: Long,
    val componentName: String,
    val componentSku: String,
    val unitCost: Double,
    val currentStock: Int,
    val quantityRequired: Int
)
