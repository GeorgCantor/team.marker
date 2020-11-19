package team.marker.view.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import team.marker.model.remote.ApiRepository
import team.marker.model.responses.ResponseMessage

class HomeViewModel(private val repository: ApiRepository) : ViewModel() {

    val success = MutableLiveData<Boolean>()
    val response = MutableLiveData<ResponseMessage>()
    val error = MutableLiveData<String>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        error.postValue(throwable.message)
    }

    fun logout() {
        viewModelScope.launch(exceptionHandler) {
            repository.logout().apply {
                success.postValue(this?.success)
                response.postValue(this?.response)
                error.postValue(this?.error?.error_msg)
            }
        }
    }
}