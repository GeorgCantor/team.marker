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
    val products = MutableLiveData<ArrayList<PickProduct>>()

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

    fun addProduct(product: PickProduct) {
        viewModelScope.launch {
            val prods = mutableListOf<PickProduct>()
            products.value?.map {
                prods.add(it)
            }
            if (!prods.contains(product)) prods.add(product)
            products.postValue(prods as ArrayList<PickProduct>?)
        }
    }
}