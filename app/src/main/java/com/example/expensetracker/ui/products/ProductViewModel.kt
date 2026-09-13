package com.example.expensetracker.ui.products
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.entity.ProductEntity
import com.example.expensetracker.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
sealed interface ProductUiState {
    object Loading : ProductUiState
    data class Success(val products: List<ProductEntity>) : ProductUiState
    data class Error(val message: String) : ProductUiState
}
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()
    val uiState: StateFlow<ProductUiState> = repository.allProducts
        .map<List<ProductEntity>, ProductUiState> { ProductUiState.Success(it) }
        .catch { emit(ProductUiState.Error(it.localizedMessage ?: "Помилка завантаження")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProductUiState.Loading
        )
    fun addProduct(name: String, category: String, costPrice: Double, salePrice: Double, quantity: Int, sku: String) {
        if (name.isBlank() || costPrice <= 0 || salePrice <= 0 || quantity < 0) {
            viewModelScope.launch { _userMessage.emit("Заповніть коректно всі поля!") }
            return
        }
        viewModelScope.launch {
            val newProduct = ProductEntity(
                name = name.trim(),
                category = category,
                costPrice = costPrice,
                salePrice = salePrice,
                stockQuantity = quantity,
                sku = sku.trim()
            )
            repository.addProduct(newProduct)
            _userMessage.emit("Товар успішно додано!")
        }
    }
}
