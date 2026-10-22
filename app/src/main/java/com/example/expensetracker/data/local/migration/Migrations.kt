package com.example.expensetracker.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Збірка всіх міграцій БД. Замість fallbackToDestructiveMigration() — явні, контрольовані міграції,
 * щоб не втрачати фінансові дані користувача.
 */
object Migrations {

    /**
     * v2 -> v3:
     *  - додано колонку paid_at у sales (касовий метод);
     *  - нові таблиці: repairs, repair_parts, cash_transactions.
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. sales.paid_at — nullable, для старих оплачених продажів залишаємо NULL.
            db.execSQL("ALTER TABLE sales ADD COLUMN paid_at INTEGER")

            // 2. repairs
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `repairs` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`asset_name` TEXT NOT NULL, " +
                    "`client_name` TEXT NOT NULL, " +
                    "`description` TEXT NOT NULL, " +
                    "`labor_cost` REAL NOT NULL, " +
                    "`parts_cost` REAL NOT NULL, " +
                    "`compensation_amount` REAL NOT NULL, " +
                    "`is_compensated` INTEGER NOT NULL, " +
                    "`repair_date` INTEGER NOT NULL, " +
                    "`compensated_at` INTEGER)"
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_repairs_repair_date` ON `repairs` (`repair_date`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_repairs_is_compensated` ON `repairs` (`is_compensated`)")

            // 3. repair_parts
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `repair_parts` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`repair_id` INTEGER NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`component_id` INTEGER, " +
                    "`quantity` INTEGER NOT NULL, " +
                    "`unit_cost` REAL NOT NULL, " +
                    "FOREIGN KEY(`repair_id`) REFERENCES `repairs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_repair_parts_repair_id` ON `repair_parts` (`repair_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_repair_parts_component_id` ON `repair_parts` (`component_id`)")

            // 4. cash_transactions
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `cash_transactions` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`type` TEXT NOT NULL, " +
                    "`amount` REAL NOT NULL, " +
                    "`description` TEXT NOT NULL, " +
                    "`source_id` INTEGER, " +
                    "`created_at` INTEGER NOT NULL)"
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_cash_transactions_created_at` ON `cash_transactions` (`created_at`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_cash_transactions_type` ON `cash_transactions` (`type`)")
        }
    }

    val ALL = arrayOf(MIGRATION_2_3)
}
