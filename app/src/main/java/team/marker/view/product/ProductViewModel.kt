package team.marker.view.product

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import team.marker.model.remote.ApiRepository
import team.marker.model.responses.Product

class ProductViewModel(private val repository: ApiRepository) : ViewModel() {

    private lateinit var disposable: Disposable

    val success = MutableLiveData<Boolean>()
    val response = MutableLiveData<Product>()
    val error = MutableLiveData<String>()

    fun getProduct(product_id: String) {
        disposable = Observable.fromCallable {
            repository.product(product_id)
                ?.subscribe({
                    success.postValue(it?.success)
                    response.postValue(it?.response)
                    it?.error?.let { error.postValue(it.toString()) }
                }, {
                })
        }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}