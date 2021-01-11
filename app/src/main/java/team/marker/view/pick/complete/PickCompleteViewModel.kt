package team.marker.view.pick.complete

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import team.marker.model.Product
import team.marker.model.remote.ApiRepository
import team.marker.model.requests.PickProduct
import team.marker.model.requests.PickRequest
import team.marker.model.responses.ResponseMessage

class PickCompleteViewModel(private val repository: ApiRepository) : ViewModel() {

    val response = MutableLiveData<ResponseMessage>()
    val error = MutableLiveData<String>()
    val products = MutableLiveData<MutableList<Product>>()
    val currentProduct = MutableLiveData<PickProduct>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        error.postValue(throwable.message)
    }

    fun pick(request: PickRequest) {
        viewModelScope.launch(exceptionHandler) {
            repository.pick(request).apply {
                response.postValue(this?.response)
                error.postValue(this?.error?.error_msg)
            }
        }
    }

    fun getProduct(productId: String) {
        viewModelScope.launch(exceptionHandler) {
            if (products.value == null || products.value?.all { it.id != productId.toInt() } == true) {
                repository.products(productId).apply {
                    val prods = mutableListOf<Product>()
                    products.value?.let { prods.addAll(it) }
                    this?.response?.info?.firstOrNull()?.let {
                        prods.add(Product(it.id!!, it.title!!))
                    }
                    products.postValue(prods)
                }
            }
        }
    }

    fun addProduct(product: PickProduct) {
        viewModelScope.launch(exceptionHandler) {
            if (currentProduct.value != product) currentProduct.postValue(product)
        }
    }
}