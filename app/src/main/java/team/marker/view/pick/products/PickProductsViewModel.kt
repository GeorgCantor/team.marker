package team.marker.view.pick.products

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import team.marker.model.remote.ApiRepository
import team.marker.model.responses.Products

class PickProductsViewModel(private val repository: ApiRepository) : ViewModel() {

    val progressIsVisible = MutableLiveData<Boolean>().apply { value = true }
    val response = MutableLiveData<Products>()
    val error = MutableLiveData<String>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        error.postValue(throwable.message)
        progressIsVisible.postValue(false)
    }

    fun getProducts(productIds: String? = null) {
        viewModelScope.launch(exceptionHandler) {
            repository.products(productIds).apply {
                response.postValue(this?.response)
                error.postValue(this?.error?.error_msg)
            }
            progressIsVisible.postValue(false)
        }
    }
}