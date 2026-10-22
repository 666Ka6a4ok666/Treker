package com.example.expensetracker.ui.repairs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.entity.RepairEntity
import com.example.expensetracker.data.local.entity.RepairPartEntity
import com.example.expensetracker.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RepairFormPart(
    val name: String,
    val componentId: Long? = null,
    val quantity: Int,
    val unitCost: Double
)

@HiltViewModel
class RepairsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()

    val repairs: StateFlow<List<RepairEntity>> = repository.allRepairs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRepairDebt: StateFlow<Double> = repository.totalRepairDebt
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun createRepair(
        assetName: String,
        clientName: String,
        description: String,
        laborCost: Double,
        parts: List<RepairFormPart>,
        compensationAmount: Double,
        compensationPaid: Boolean
    ) {
        if (assetName.isBlank()) {
            viewModelScope.launch { _userMessage.emit("Вкажіть, що ремонтуємо") }
            return
        }
        viewModelScope.launch {
            val partEntities = parts.map {
                RepairPartEntity(
                    repairId = 0,
                    name = it.name,
                    componentId = it.componentId,
                    quantity = it.quantity,
                    unitCost = it.unitCost
                )
            }
            repository.createRepair(
                assetName = assetName.trim(),
                clientName = clientName.trim(),
                description = description.trim(),
                laborCost = laborCost,
                parts = partEntities,
                compensationAmount = compensationAmount,
                compensationPaid = compensationPaid
            ).onSuccess {
                _userMessage.emit("Ремонт збережено")
            }.onFailure {
                _userMessage.emit(it.message ?: "Помилка створення ремонту")
            }
        }
    }

    fun markCompensated(repairId: Long) {
        viewModelScope.launch {
            repository.markRepairCompensated(repairId)
            _userMessage.emit("Компенсацію отримано, гроші зараховано в касу")
        }
    }
}
