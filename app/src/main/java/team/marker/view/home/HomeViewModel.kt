package team.marker.view.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import team.marker.model.remote.ApiRepository
import team.marker.model.responses.ResponseMessage

class HomeViewModel(private val repository: ApiRepository) : ViewModel() {

    private lateinit var disposable: Disposable

    val success = MutableLiveData<Boolean>()
    val response = MutableLiveData<ResponseMessage>()
    val error = MutableLiveData<String>()

    fun logout() {
        disposable = Observable.fromCallable {
            repository.logout()
                ?.subscribe({
                    success.postValue(it?.success)
                    it?.response?.let { response.postValue(it) }
                    it?.error?.let { error.postValue(it.toString()) }
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