package team.marker.view.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import team.marker.model.remote.ApiRepository
import team.marker.model.responses.History

class HistoryViewModel(private val repository: ApiRepository) : ViewModel() {

    val progressIsVisible = MutableLiveData<Boolean>().apply { value = true }
    val response = MutableLiveData<History>()
    val error = MutableLiveData<String>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        error.postValue(throwable.message)
        progressIsVisible.postValue(false)
    }

    fun getHistory(offset: Int? = 0) {
        viewModelScope.launch(exceptionHandler) {
            repository.history(offset).apply {
                response.postValue(this?.response)
                error.postValue(this?.error?.error_msg)
            }
            progressIsVisible.postValue(false)
        }
    }
}