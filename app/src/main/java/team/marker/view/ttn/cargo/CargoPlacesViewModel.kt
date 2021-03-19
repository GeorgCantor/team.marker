package team.marker.view.ttn.cargo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import team.marker.model.remote.ApiRepository
import team.marker.model.responses.Product
import team.marker.model.ttn.ProductPlace

class CargoPlacesViewModel(private val repository: ApiRepository) : ViewModel() {

    private val selectedItems = MutableLiveData<List<Product>>()
    val progressIsVisible = MutableLiveData<Boolean>().apply { value = true }
    val productIds = MutableLiveData<String>().apply { value = "" }
    val products = MutableLiveData<List<Product>>()
    val buttonClickable = MutableLiveData<Boolean>().apply { value = false }
    val places = MutableLiveData<List<ProductPlace>>()
    val error = MutableLiveData<String>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        error.postValue(throwable.message)
        progressIsVisible.postValue(false)
    }

    fun getProducts() {
        viewModelScope.launch(exceptionHandler) {
            repository.products(productIds.value).apply {
                products.postValue(this?.response?.info)
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
            buttonClickable.postValue(items.isNotEmpty())
        }
    }

    fun createProductPlace() {
        viewModelScope.launch {
            val list = mutableListOf<ProductPlace>()
            places.value?.let { list.addAll(it) }
            list.add((ProductPlace(selectedItems.value ?: emptyList())))
            places.postValue(list)
            selectedItems.postValue(emptyList())

            val prods = mutableListOf<Product>()
            products.value?.forEach { if (selectedItems.value?.contains(it) == false) prods.add(it) }
            products.postValue(prods)
        }
    }
}