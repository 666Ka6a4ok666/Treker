package com.example.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.expensetracker.data.local.entity.CashTransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Журнал руху грошей. Усі агрегати — касовим методом (лише реальні надходження/видатки).
 */
@Dao
interface CashTransactionDao {

    @Query("SELECT * FROM cash_transactions ORDER BY created_at DESC")
    fun getAll(): Flow<List<CashTransactionEntity>>

    @Insert
    suspend fun insert(transaction: CashTransactionEntity): Long

    /** Сума всіх надходжень (прихід грошей). */
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM cash_transactions WHERE type IN (:inflowTypes)")
    fun getTotalInflow(inflowTypes: List<String>): Flow<Double>

    /** Сума всіх видатків (видаток грошей). */
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM cash_transactions WHERE type IN (:outflowTypes)")
    fun getTotalOutflow(outflowTypes: List<String>): Flow<Double>

    /** Надходження за проміжок часу. */
    @Query(
        "SELECT COALESCE(SUM(amount), 0.0) FROM cash_transactions " +
            "WHERE type IN (:inflowTypes) AND created_at BETWEEN :from AND :to"
    )
    fun getInflowBetween(inflowTypes: List<String>, from: Long, to: Long): Flow<Double>

    /** Видатки за проміжок часу. */
    @Query(
        "SELECT COALESCE(SUM(amount), 0.0) FROM cash_transactions " +
            "WHERE type IN (:outflowTypes) AND created_at BETWEEN :from AND :to"
    )
    fun getOutflowBetween(outflowTypes: List<String>, from: Long, to: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM cash_transactions WHERE type = :type")
    fun getTotalByType(type: String): Flow<Double>

    @Query("DELETE FROM cash_transactions WHERE source_id = :sourceId AND type = :type")
    suspend fun deleteBySource(sourceId: Long, type: String)
}
