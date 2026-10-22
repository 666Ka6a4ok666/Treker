package com.example.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.data.local.entity.IncomeEntity
import com.example.expensetracker.data.local.entity.ProductEntity
import com.example.expensetracker.data.local.entity.RepairEntity
import com.example.expensetracker.data.local.entity.RepairPartEntity
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
        val incomes: StateFlow<List<IncomeEntity>> = repository.allIncomes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val repairs: StateFlow<List<RepairEntity>> = repository.allRepairs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Касовий метод
    val totalCashInflow: StateFlow<Double> = repository.totalCashInflow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalCashOutflow: StateFlow<Double> = repository.totalCashOutflow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalRevenue: StateFlow<Double> = repository.totalRevenue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalGrossProfit: StateFlow<Double> = repository.totalGrossProfit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalExpenses: StateFlow<Double> = repository.totalExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalIncome: StateFlow<Double> = repository.totalIncome
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalUnpaidDebt: StateFlow<Double> = repository.totalUnpaidDebt
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalRepairDebt: StateFlow<Double> = repository.totalRepairDebt
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val netProfit: StateFlow<Double> = repository.netProfit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun processSale(
        productId: Long,
        quantity: Int,
        note: String,
        isCredit: Boolean = false,
        debtorName: String = ""
    ) {
        viewModelScope.launch {
            repository.sellProduct(productId, quantity, note, isCredit, debtorName)
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

        fun insertIncome(income: IncomeEntity) {
        viewModelScope.launch {
            repository.addIncome(income)
        }
    }

    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch {
            repository.deleteExpense(expenseId)
        }
    }

    fun deleteIncome(incomeId: Long) {
        viewModelScope.launch {
            repository.deleteIncome(incomeId)
        }
    }

    fun addProductStock(productId: Long, quantity: Int) {
        viewModelScope.launch {
            repository.addProductStock(productId, quantity)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }


        fun markSaleAsPaid(saleId: Long) {
        viewModelScope.launch {
            repository.markSaleAsPaid(saleId)
        }
    }

    fun createRepair(
        assetName: String,
        clientName: String,
        description: String,
        laborCost: Double,
        parts: List<RepairPartEntity>,
        compensationAmount: Double,
        compensationPaid: Boolean,
        onResult: (Result<Long>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.createRepair(
                assetName = assetName,
                clientName = clientName,
                description = description,
                laborCost = laborCost,
                parts = parts,
                compensationAmount = compensationAmount,
                compensationPaid = compensationPaid
            )
            onResult(result)
        }
    }

    fun markRepairCompensated(repairId: Long) {
        viewModelScope.launch {
            repository.markRepairCompensated(repairId)
        }
    }
}
