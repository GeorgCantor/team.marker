package team.marker.view.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import team.marker.model.remote.ApiRepository
import team.marker.model.requests.LoginRequest
import team.marker.model.responses.Login

class LoginViewModel(private val repository: ApiRepository) : ViewModel() {

    val isLoggedIn = MutableLiveData<Boolean>()
    val response = MutableLiveData<Login>()
    val error = MutableLiveData<String>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        error.postValue(throwable.message)
    }

    fun login(request: LoginRequest) {
        viewModelScope.launch(exceptionHandler) {
            repository.login(request).apply {
                isLoggedIn.postValue(this?.success)
                response.postValue(this?.response)
                error.postValue(this?.error?.error_msg)
            }
        }
    }
}