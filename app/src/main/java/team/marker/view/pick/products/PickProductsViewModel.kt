package team.marker.view.pick.products

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import team.marker.model.remote.ApiRepository
import team.marker.model.responses.Products

class PickProductsViewModel(private val repository: ApiRepository) : ViewModel() {

    private lateinit var disposable: Disposable

    val progressIsVisible = MutableLiveData<Boolean>().apply { this.value = true }
    val response = MutableLiveData<Products>()
    val error = MutableLiveData<String>()

    fun getProducts(product_ids: String? = null) {
        disposable = Observable.fromCallable {
            repository.products(product_ids)
                ?.doFinally { progressIsVisible.postValue(false) }
                ?.subscribe({
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