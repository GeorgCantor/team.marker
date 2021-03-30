package team.marker.view.ttn.cargo.create

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import team.marker.model.remote.ApiRepository
import team.marker.model.requests.CargoRequest

class CreateTtnViewModel(private val repository: ApiRepository) : ViewModel() {

    val progressIsVisible = MutableLiveData<Boolean>().apply { value = false }
    val showSuccessScreen = MutableLiveData<Boolean>().apply { value = false }
    val error = MutableLiveData<String>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        error.postValue(throwable.message)
        progressIsVisible.postValue(false)
    }

    fun sendTtn(request: CargoRequest) {
        progressIsVisible.value = true
        viewModelScope.launch(exceptionHandler) {
            repository.cargo(request).apply {
                showSuccessScreen.postValue(this?.success)
                error.postValue(this?.error?.error_msg)
            }
            progressIsVisible.postValue(false)
        }
    }
}