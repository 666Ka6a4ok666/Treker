package com.example.expensetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["product_id"])]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "product_id")
    val productId: Long,
    @ColumnInfo(name = "product_name")
    val productName: String,
    @ColumnInfo(name = "quantity")
    val quantity: Int,
    @ColumnInfo(name = "unit_price")
    val unitPrice: Double,
    @ColumnInfo(name = "total_amount")
    val totalAmount: Double,
    @ColumnInfo(name = "profit")
    val profit: Double,
    @ColumnInfo(name = "sale_date")
    val saleDate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "note")
    val note: String = "",
    @ColumnInfo(name = "is_credit")
    val isCredit: Boolean = false,
    @ColumnInfo(name = "debtor_name")
    val debtorName: String = "",
        @ColumnInfo(name = "is_paid")
    val isPaid: Boolean = true,
    @ColumnInfo(name = "paid_at")
    val paidAt: Long? = if (isPaid) System.currentTimeMillis() else null
)
