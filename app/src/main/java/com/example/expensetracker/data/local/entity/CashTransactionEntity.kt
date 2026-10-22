package com.example.expensetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Єдиний журнал руху грошей (касовий метод обліку).
 * Кожен фактичний прихід/видаток грошей створює рядок тут.
 * Агрегати дашборда (виручка, витрати, баланс) рахуються ЛИШЕ з цієї таблиці.
 */
@Entity(
    tableName = "cash_transactions",
    indices = [Index(value = ["created_at"]), Index(value = ["type"])]
)
data class CashTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Тип руху грошей. */
    @ColumnInfo(name = "type")
    val type: CashTransactionType,
    /** Сума руху, завжди додатна. Напрямок визначається типом. */
    @ColumnInfo(name = "amount")
    val amount: Double,
    /** Довільний опис для користувача. */
    @ColumnInfo(name = "description")
    val description: String = "",
    /** Посилання на джерело операції (напр. id продажу, ремонту, витрати). */
    @ColumnInfo(name = "source_id")
    val sourceId: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Типи грошових операцій.
 * @property isInflow чи це прихід грошей (true) чи видаток (false).
 */
enum class CashTransactionType(val isInflow: Boolean) {
    /** Отримані гроші від продажу товару. */
    SALE_INCOME(isInflow = true),

    /** Отримані гроші при погашенні боргу за продаж. */
    DEBT_REPAYMENT(isInflow = true),

    /** Отримані гроші за виконаний ремонт (компенсація клієнта). */
    REPAIR_INCOME(isInflow = true),

    /** Отримані волонтерські/інвестиційні кошти, гранти. */
    OTHER_INCOME(isInflow = true),

    /** Сплачені гроші за довільною витратою (оренда, комунальні тощо). */
    EXPENSE(isInflow = false),

    /** Сплачені гроші на запчастини/матеріали для ремонту. */
    REPAIR_COST(isInflow = false),

    /** Сплачені гроші на комплектуючі/сировину для виробництва. */
    PRODUCTION_COST(isInflow = false)
}
