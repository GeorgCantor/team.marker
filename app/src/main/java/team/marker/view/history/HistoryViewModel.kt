package team.marker.view.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import team.marker.model.remote.ApiRepository
import team.marker.model.responses.History

class HistoryViewModel(private val repository: ApiRepository) : ViewModel() {

    private lateinit var disposable: Disposable

    val progressIsVisible = MutableLiveData<Boolean>().apply { this.value = true }
    val response = MutableLiveData<History>()
    val error = MutableLiveData<String>()

    fun getHistory(offset: Int? = 0) {
        disposable = Observable.fromCallable {
            repository.history(offset)
                ?.doFinally { progressIsVisible.postValue(false) }
                ?.subscribe({
                    response.postValue(it?.response)
                    error.postValue(it?.error.toString())
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