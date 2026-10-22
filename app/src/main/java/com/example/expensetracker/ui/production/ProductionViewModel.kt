package com.example.expensetracker.ui.production

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.entity.ProductEntity
import com.example.expensetracker.data.local.entity.ProductionOrderEntity
import com.example.expensetracker.data.local.entity.RecipeItemWithComponent
import com.example.expensetracker.data.local.entity.WarehouseComponentEntity
import com.example.expensetracker.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProductionViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()

    val products: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val components: StateFlow<List<WarehouseComponentEntity>> = repository.allComponents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<ProductionOrderEntity>> = repository.allProductionOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val selectedProductId = kotlinx.coroutines.flow.MutableStateFlow<Long?>(null)

    /** Рецепт обраного продукту (реактивно оновлюється при CRUD). */
    @OptIn(ExperimentalCoroutinesApi::class)
    val recipe: StateFlow<List<RecipeItemWithComponent>> = selectedProductId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.recipeForProduct(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectProduct(productId: Long?) {
        selectedProductId.value = productId
    }

    fun addOrUpdateRecipeItem(productId: Long, componentId: Long, quantityRequired: Int) {
        viewModelScope.launch {
            repository.upsertRecipeItem(productId, componentId, quantityRequired)
                .onSuccess { _userMessage.emit("Складник рецепту збережено") }
                .onFailure { _userMessage.emit(it.message ?: "Помилка збереження складника") }
        }
    }

    fun removeRecipeItem(productId: Long, componentId: Long) {
        viewModelScope.launch {
            repository.removeRecipeItem(productId, componentId)
            _userMessage.emit("Складник видалено з рецепту")
        }
    }

    fun produce(productId: Long, quantityToProduce: Int, updateProductCost: Boolean) {
        viewModelScope.launch {
            repository.produceProduct(
                productId = productId,
                quantityToProduce = quantityToProduce,
                updateProductCost = updateProductCost
            ).onSuccess { cost ->
                _userMessage.emit(
                    "Виготовлено $quantityToProduce шт. Собівартість партії: ₴" +
                        String.format(java.util.Locale.US, "%,.2f", cost)
                )
            }.onFailure { _userMessage.emit(it.message ?: "Помилка виробництва") }
        }
    }
}
