package team.marker.view.product

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import team.marker.model.remote.ApiRepository
import team.marker.model.responses.Product

class ProductViewModel(private val repository: ApiRepository) : ViewModel() {

    val success = MutableLiveData<Boolean>()
    val response = MutableLiveData<Product>()
    val error = MutableLiveData<String>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        error.postValue(throwable.message)
    }

    fun getProduct(product_id: String, lat: String, lng: String) {
        viewModelScope.launch(exceptionHandler) {
            repository.product(product_id, lat, lng)?.apply {
                response?.let { this@ProductViewModel.response.postValue(it) }
                error?.let { this@ProductViewModel.error.postValue(it.error_msg) }
            }
        }
    }
}