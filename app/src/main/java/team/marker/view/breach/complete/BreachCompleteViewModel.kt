package team.marker.view.breach.complete

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import team.marker.model.remote.ApiRepository
import team.marker.model.requests.BreachRequest
import team.marker.model.responses.ResponseMessage

class BreachCompleteViewModel(private val repository: ApiRepository) : ViewModel() {

    private lateinit var disposable: Disposable

    val response = MutableLiveData<ResponseMessage>()
    val error = MutableLiveData<String>()

    fun breach(request: BreachRequest) {
        disposable = Observable.fromCallable {
            repository.breach(request)
                ?.subscribe({
                    Log.e("Message", request.toString())
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