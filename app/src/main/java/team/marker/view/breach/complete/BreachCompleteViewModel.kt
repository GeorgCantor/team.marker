package team.marker.view.breach.complete

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import team.marker.model.remote.ApiRepository
import team.marker.model.requests.BreachRequest
import team.marker.model.responses.ResponseMessage
import java.io.File

class BreachCompleteViewModel(private val repository: ApiRepository) : ViewModel() {

    val response = MutableLiveData<ResponseMessage>()
    val error = MutableLiveData<String>()
    val photos = MutableLiveData<MutableList<File>>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        error.postValue(throwable.message)
    }

    fun breach(request: BreachRequest) {
        viewModelScope.launch(exceptionHandler) {
            repository.breach(request).apply {
                response.postValue(this?.response)
                error.postValue(this?.error?.error_msg)
            }
        }
    }

    fun addPhoto(file: File) {
        viewModelScope.launch {
            val list = mutableListOf<File>()
            photos.value?.map {
                list.add(it)
            }
            list.add(file)
            photos.postValue(list)
        }
    }
}