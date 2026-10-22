package com.example.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.expensetracker.data.local.entity.RepairPartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepairPartDao {

    @Query("SELECT * FROM repair_parts WHERE repair_id = :repairId")
    fun getPartsForRepair(repairId: Long): Flow<List<RepairPartEntity>>

    @Insert
    suspend fun insertPart(part: RepairPartEntity): Long

    @Query("DELETE FROM repair_parts WHERE repair_id = :repairId")
    suspend fun deletePartsForRepair(repairId: Long)

    @Query("SELECT COALESCE(SUM(unit_cost * quantity), 0.0) FROM repair_parts WHERE repair_id = :repairId")
    suspend fun getPartsCost(repairId: Long): Double
}
