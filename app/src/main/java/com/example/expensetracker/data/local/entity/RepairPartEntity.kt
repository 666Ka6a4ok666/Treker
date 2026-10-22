package com.example.expensetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Запчастина, витрачена на конкретний ремонт.
 * Може посилатися на комплектуючу зі складу [componentId] (тоді кількість списується),
 * або задаватися вручну назвою/ціною (componentId = null).
 */
@Entity(
    tableName = "repair_parts",
    foreignKeys = [
        ForeignKey(
            entity = RepairEntity::class,
            parentColumns = ["id"],
            childColumns = ["repair_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["repair_id"]), Index(value = ["component_id"])]
)
data class RepairPartEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "repair_id")
    val repairId: Long,
    /** Назва запчастини (кешується для історії). */
    @ColumnInfo(name = "name")
    val name: String,
    /** Посилання на склад комплектуючих (опційно). */
    @ColumnInfo(name = "component_id")
    val componentId: Long? = null,
    @ColumnInfo(name = "quantity")
    val quantity: Int,
    /** Ціна за одиницю на момент ремонту. */
    @ColumnInfo(name = "unit_cost")
    val unitCost: Double
) {
    val totalCost: Double get() = unitCost * quantity
}
