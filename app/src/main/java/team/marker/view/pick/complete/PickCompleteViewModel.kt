package team.marker.view.pick.complete

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import team.marker.model.remote.ApiRepository
import team.marker.model.requests.PickProduct
import team.marker.model.requests.PickRequest
import team.marker.model.responses.ResponseMessage
import java.util.*

class PickCompleteViewModel(private val repository: ApiRepository) : ViewModel() {

    val response = MutableLiveData<ResponseMessage>()
    val error = MutableLiveData<String>()
    val products = MutableLiveData<ArrayList<PickProduct>>()
    val product = MutableLiveData<String>()
    val productIds = ArrayList<String>()
    val currentProduct = MutableLiveData<PickProduct>()
    private var lastTime: Date? = null

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

    fun getProduct(productId: String? = null) {
        viewModelScope.launch(exceptionHandler) {
            repository.products(productId).apply {
                product.postValue(this?.response?.info?.firstOrNull()?.title)
            }
        }
    }

    fun addProduct(product: PickProduct) {
        viewModelScope.launch {
            getProduct(product.id.toString())

            val prods = mutableListOf<PickProduct>()
            products.value?.forEach { prods.add(it) }

            if (!prods.contains(product)) {
                lastTime = Date()
                prods.add(product)
                productIds.add(product.id.toString())
                currentProduct.postValue(product)
                products.postValue(prods as ArrayList<PickProduct>?)
            } /*else {
                val seconds: Long = (Date().time - lastTime!!.time) / 1000
                if (seconds > 3) {
                    lastTime = Date()
                    prods.add(product)
                    products.postValue(prods as ArrayList<PickProduct>?)
                }
            }*/
        }
    }

    fun clearCurrentProduct() {
        viewModelScope.launch {
            currentProduct.postValue(null)
        }
    }
}