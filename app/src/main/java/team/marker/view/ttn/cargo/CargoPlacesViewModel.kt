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
    val products = MutableLiveData<Set<Product>>()
    val createClickable = MutableLiveData<Boolean>().apply { value = false }
    val nextClickable = MutableLiveData<Boolean>().apply { value = false }
    val places = MutableLiveData<List<ProductPlace>>()
    val selectedPlace = MutableLiveData<ProductPlace>()
    val error = MutableLiveData<String>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        error.postValue(throwable.message)
        progressIsVisible.postValue(false)
    }

    fun getProducts() {
        viewModelScope.launch(exceptionHandler) {
            repository.products(productIds.value).apply {
                products.postValue(this?.response?.info?.toSet())
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
            createClickable.postValue(items.isNotEmpty())

            val list = products.value
            list?.forEach { if (it == prod) it.isSelected = !prod.isSelected!! }
            products.postValue(list)
        }
    }

    fun createProductPlace() {
        viewModelScope.launch {
            val prods = mutableSetOf<Product>()
            products.value?.forEach { if (selectedItems.value?.contains(it) == false) prods.add(it) }
            products.postValue(prods)
            createClickable.postValue(false)

            val list = mutableListOf<ProductPlace>()
            places.value?.let { list.addAll(it) }
            list.add((ProductPlace(selectedItems.value ?: emptyList())))
            places.postValue(list)
            selectedItems.postValue(emptyList())
            nextClickable.postValue(list.isNotEmpty())
        }
    }

    fun removeProduct(position: Int) {
        viewModelScope.launch {
            val items = mutableSetOf<Product>()
            val removed = mutableListOf<Product>()
            products.value?.forEachIndexed { i, prod -> if (i != position) items.add(prod) else removed.add(prod) }
            products.postValue(items)

            val selected = selectedItems.value?.filter { !removed.contains(it)}
            selectedItems.postValue(selected)
            createClickable.postValue(selected?.isNotEmpty())
        }
    }

    fun removePlace(position: Int) {
        viewModelScope.launch {
            val items = mutableListOf<ProductPlace>()
            val removed = mutableListOf<Product>()
            products.value?.let { removed.addAll(it) }
            places.value?.forEachIndexed { i, place ->
                if (i != position) items.add(place)
                else removed.addAll(place.products.map { it.isSelected = false; it })
            }
            places.postValue(items)
            products.postValue(removed.toSet())
            nextClickable.postValue(items.isNotEmpty())
        }
    }

    fun removeProductFromPlace(position: Int) {
        viewModelScope.launch {
            val items = mutableListOf<Product>()
            selectedPlace.value?.products?.forEachIndexed { i, prod ->
                if (i != position) items.add(prod)
                else {
                    val list = mutableListOf<Product>()
                    products.value?.let { list.addAll(it) }
                    list.add(prod)
                    products.postValue(list.map { it.isSelected = false; it }.toSet())
                }
            }
            selectedPlace.postValue(ProductPlace(items))
        }
    }

    fun setPlaces(list: List<ProductPlace>) {
        viewModelScope.launch {
            val newProducts = mutableListOf<Product>()
            val oldProducts = mutableListOf<Product>()
            list.forEach { it.products.forEach { newProducts.add(it) } }
            places.value?.forEach { it.products.forEach { oldProducts.add(it) } }
            if (newProducts != oldProducts) places.postValue(list)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            selectedItems.postValue(emptyList())
            progressIsVisible.postValue(false)
            products.postValue(emptySet())
            createClickable.postValue(false)
            nextClickable.postValue(false)
            places.postValue(emptyList())
        }
    }
}