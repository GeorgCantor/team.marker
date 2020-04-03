package team.marker.view.scan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import team.marker.model.remote.ApiRepository
import team.marker.model.requests.LoginRequest
import team.marker.model.responses.Login

class ScanViewModel(private val repository: ApiRepository) : ViewModel() {

    private lateinit var disposable: Disposable

    //val isLoggedIn = MutableLiveData<Boolean>()
    //val success = MutableLiveData<Login>()
    //val error = MutableLiveData<String>()

    /*fun login(request: LoginRequest) {
        disposable = Observable.fromCallable {
            repository.login(request)
                ?.subscribe({
                    success.postValue(it?.response)
                    error.postValue(it?.error.toString())
                }, {
                })
        }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }*/

    /*override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }*/
}