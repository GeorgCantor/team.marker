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

class PickCompleteViewModel(private val repository: ApiRepository) : ViewModel() {

    val response = MutableLiveData<ResponseMessage>()
    val error = MutableLiveData<String>()
    val product = MutableLiveData<String>()
    val currentProduct = MutableLiveData<PickProduct>()
    private var lastProdId = "0"

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
            if (productId != lastProdId) {
                lastProdId = productId
                repository.products(productId).apply {
                    product.postValue(this?.response?.info?.firstOrNull()?.title)
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