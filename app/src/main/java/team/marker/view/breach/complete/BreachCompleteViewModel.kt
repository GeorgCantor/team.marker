package team.marker.view.breach.complete

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import team.marker.model.remote.ApiRepository
import team.marker.model.requests.BreachRequest
import team.marker.model.responses.ResponseMessage

class BreachCompleteViewModel(private val repository: ApiRepository) : ViewModel() {

    val response = MutableLiveData<ResponseMessage>()
    val error = MutableLiveData<String>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        error.postValue(throwable.message)
    }

    fun breach(request: BreachRequest) {
        viewModelScope.launch(exceptionHandler) {
            repository.breach(request).apply {
                response.postValue(this?.response)
                error.postValue(this?.error?.error_msg)
            }
        }
    }
}