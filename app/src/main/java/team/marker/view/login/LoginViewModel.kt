package team.marker.view.login

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import team.marker.model.remote.ApiRepository
import team.marker.model.requests.LoginRequest
import team.marker.util.Constants.SID
import team.marker.util.Constants.TOKEN
import team.marker.util.Constants.accessSid
import team.marker.util.Constants.accessToken
import team.marker.util.putAny

class LoginViewModel(
    private val repository: ApiRepository,
    private val preferences: SharedPreferences
) : ViewModel() {

    val loginSuccess = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        error.postValue(throwable.message)
    }

    fun login(request: LoginRequest) {
        viewModelScope.launch(exceptionHandler) {
            repository.login(request).apply {
                val sid = this?.response?.sid
                val token = this?.response?.token
                if (sid != null && token != null) {
                    preferences.putAny(SID, sid)
                    preferences.putAny(TOKEN, token)
                    accessSid = sid
                    accessToken = token
                }
                loginSuccess.postValue(this?.success)
                error.postValue(this?.error?.error_msg)
            }
        }
    }
}