package com.example.expensetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Замовлення на ремонт засобу/обладнання.
 *
 * Ремонт може бути:
 *  - зовнішнім (клієнтським): вартість компенсується клієнтом -> дохід [compensationAmount].
 *  - внутрішнім: компенсації немає (compensationAmount = 0), лише витрати.
 *
 * Фінансовий результат ремонту = compensationAmount − (repairCost + partsCost).
 * Витрати на запчастини (partsCost) фіксуються окремими рядками [RepairPartEntity]
 * і як рух грошей REPAIR_COST у касі.
 */
@Entity(
    tableName = "repairs",
    indices = [Index(value = ["repair_date"]), Index(value = ["is_compensated"])]
)
data class RepairEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Що ремонтували (напр. «Ноутбук Samsung», «Редуктор насоса»). */
    @ColumnInfo(name = "asset_name")
    val assetName: String,
    /** Контрагент / власник / клієнт. Порожнє для внутрішніх ремонтів. */
    @ColumnInfo(name = "client_name")
    val clientName: String = "",
    /** Опис несправності / виконаних робіт. */
    @ColumnInfo(name = "description")
    val description: String = "",
    /** Вартість робіт без запчастин (скільки коштували роботи). */
    @ColumnInfo(name = "labor_cost")
    val laborCost: Double = 0.0,
    /** Сума витрат на запчастини (кешується з repair_parts, узгоджується транзакційно). */
    @ColumnInfo(name = "parts_cost")
    val partsCost: Double = 0.0,
    /** Скільки клієнт компенсував за ремонт (0 = внутрішній ремонт). */
    @ColumnInfo(name = "compensation_amount")
    val compensationAmount: Double = 0.0,
    /** Чи компенсацію вже фактично отримано (касовий метод). */
    @ColumnInfo(name = "is_compensated")
    val isCompensated: Boolean = false,
    @ColumnInfo(name = "repair_date")
    val repairDate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "compensated_at")
    val compensatedAt: Long? = if (isCompensated) System.currentTimeMillis() else null
) {
    /** Повна собівартість ремонту для компанії. */
    val totalCost: Double get() = laborCost + partsCost

    /** Фінансовий результат ремонту (компенсація мінус собівартість). */
    val result: Double get() = compensationAmount - totalCost
}
