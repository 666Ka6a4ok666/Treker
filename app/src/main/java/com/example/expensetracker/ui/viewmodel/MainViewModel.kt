package com.example.expensetracker.ui.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.data.local.entity.ProductEntity
import com.example.expensetracker.data.local.entity.SaleEntity
import com.example.expensetracker.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {
    val products: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val sales: StateFlow<List<SaleEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalRevenue: StateFlow<Double> = repository.totalRevenue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalGrossProfit: StateFlow<Double> = repository.totalGrossProfit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalExpenses: StateFlow<Double> = repository.totalExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val netProfit: StateFlow<Double> = repository.netProfit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    fun processSale(productId: Long, quantity: Int, note: String) {
        viewModelScope.launch {
            repository.sellProduct(productId, quantity, note)
        }
    }
    fun insertProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.addProduct(product)
        }
    }
    fun insertExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.addExpense(expense)
        }
    }
}
