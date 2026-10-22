package com.example.expensetracker.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.entity.ProductEntity
import com.example.expensetracker.data.local.entity.SaleEntity
import com.example.expensetracker.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {
    val products: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val salesList: StateFlow<List<SaleEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalRevenue: StateFlow<Double> = repository.totalRevenue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalGrossProfit: StateFlow<Double> = repository.totalGrossProfit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalUnpaidDebt: StateFlow<Double> = repository.totalUnpaidDebt
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _eventMessage = MutableSharedFlow<String>()
    val eventMessage = _eventMessage.asSharedFlow()

    fun sellProduct(
        productId: Long,
        quantity: Int,
        note: String,
        isCredit: Boolean = false,
        debtorName: String = ""
    ) {
        viewModelScope.launch {
            val result = repository.sellProduct(productId, quantity, note, isCredit, debtorName)
            result.onSuccess {
                val statusText = if (isCredit) "під реалізацію / в борг ($debtorName)" else "оплачено"
                _eventMessage.emit("Успішно реалізовано $quantity шт. ($statusText)!")
            }.onFailure { exception ->
                _eventMessage.emit(exception.message ?: "Помилка при продажу")
            }
        }
    }

    fun markSaleAsPaid(saleId: Long) {
        viewModelScope.launch {
            repository.markSaleAsPaid(saleId)
            _eventMessage.emit("Борг успішно погашено (позначено як оплачено)!")
        }
    }
}
