package team.marker.view.ttn.cargo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import team.marker.model.remote.ApiRepository
import team.marker.model.responses.Product
import team.marker.model.responses.Products

class CargoPlacesViewModel(private val repository: ApiRepository) : ViewModel() {

    val progressIsVisible = MutableLiveData<Boolean>().apply { value = true }
    val productIds = MutableLiveData<String>().apply { value = "" }
    val products = MutableLiveData<Products>()
    val selectedItems = MutableLiveData<List<Product>>()
    val error = MutableLiveData<String>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        error.postValue(throwable.message)
        progressIsVisible.postValue(false)
    }

    fun getProducts() {
        viewModelScope.launch(exceptionHandler) {
            repository.products(productIds.value).apply {
                products.postValue(this?.response)
                error.postValue(this?.error?.error_msg)
            }
            progressIsVisible.postValue(false)
        }
    }

    fun addSelectedItem(prod: Product) {
        viewModelScope.launch {
            val items = mutableListOf<Product>()
            selectedItems.value?.let { items.addAll(it) }
            if (items.any { it == prod }) items.removeAll { it == prod } else items.add(prod)
            selectedItems.postValue(items)
        }
    }
}