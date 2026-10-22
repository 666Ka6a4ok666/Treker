package com.example.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensetracker.data.local.entity.RepairEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepairDao {

    @Query("SELECT * FROM repairs ORDER BY repair_date DESC")
    fun getAllRepairs(): Flow<List<RepairEntity>>

    @Query("SELECT * FROM repairs WHERE id = :id LIMIT 1")
    suspend fun getRepairById(id: Long): RepairEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRepair(repair: RepairEntity): Long

    @Update
    suspend fun updateRepair(repair: RepairEntity)

    @Delete
    suspend fun deleteRepair(repair: RepairEntity): Int

    @Query("UPDATE repairs SET is_compensated = 1, compensated_at = :timestamp WHERE id = :repairId")
    suspend fun markCompensated(repairId: Long, timestamp: Long)

    @Query("UPDATE repairs SET parts_cost = :partsCost WHERE id = :repairId")
    suspend fun updatePartsCost(repairId: Long, partsCost: Double)

    /** Сума компенсацій, які ще НЕ отримано (дебіторка по ремонтах). */
    @Query("SELECT COALESCE(SUM(compensation_amount), 0.0) FROM repairs WHERE is_compensated = 0 AND compensation_amount > 0")
    fun getUncompensatedDebt(): Flow<Double>
}
