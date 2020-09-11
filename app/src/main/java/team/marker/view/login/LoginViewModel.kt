package team.marker.view.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import team.marker.model.remote.ApiRepository
import team.marker.model.requests.LoginRequest
import team.marker.model.responses.Login
import team.marker.model.responses.ResponseError

class LoginViewModel(private val repository: ApiRepository) : ViewModel() {

    private lateinit var disposable: Disposable

    val isLoggedIn = MutableLiveData<Boolean>()
    val response = MutableLiveData<Login>()
    val error = MutableLiveData<ResponseError>()

    fun login(request: LoginRequest) {
        disposable = Observable.fromCallable {
            repository.login(request)
                ?.subscribe({
                    isLoggedIn.postValue(it?.success)
                    it?.response?.let { response.postValue(it) }
                    it?.error?.let { error.postValue(it) }
                }, {
                })
        }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    override fun onCleared() {
        super.onCleared()
        if (::disposable.isInitialized) disposable.dispose()
    }
}