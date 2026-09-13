package com.example.expensetracker.data.local.entity
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(
    tableName = "production_orders",
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
data class ProductionOrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "product_id")
    val productId: Long,
    @ColumnInfo(name = "product_name")
    val productName: String,
    @ColumnInfo(name = "quantity_produced")
    val quantityProduced: Int,
    @ColumnInfo(name = "component_id")
    val componentId: Long,
    @ColumnInfo(name = "component_name")
    val componentName: String,
    @ColumnInfo(name = "components_per_unit")
    val componentsPerUnit: Int,
    @ColumnInfo(name = "components_consumed")
    val componentsConsumed: Int,
    @ColumnInfo(name = "total_component_cost")
    val totalComponentCost: Double,
    @ColumnInfo(name = "production_date")
    val productionDate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "note")
    val note: String = ""
)
