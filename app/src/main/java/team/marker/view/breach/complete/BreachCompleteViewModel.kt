package team.marker.view.breach.complete

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import team.marker.model.remote.ApiRepository
import team.marker.model.responses.ResponseMessage
import java.io.File

class BreachCompleteViewModel(private val repository: ApiRepository) : ViewModel() {

    val response = MutableLiveData<ResponseMessage>()
    val error = MutableLiveData<String>()
    val photos = MutableLiveData<MutableList<File>>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        error.postValue(throwable.message)
    }

    fun breach(productId: Int, reasonId: Int, userReason: String, comment: String) {
        viewModelScope.launch(exceptionHandler) {
            val files = mutableListOf<MultipartBody.Part>()
            photos.value?.map {
                val requestBody = it.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData(
                    "files", it.name, requestBody
                )
                files.add(filePart)
            }
            repository.breach(
                productId,
                reasonId,
                RequestBody.create("text/plain".toMediaTypeOrNull(), userReason),
                RequestBody.create("text/plain".toMediaTypeOrNull(), comment),
                files.toTypedArray()
            ).apply {
                response.postValue(this?.response)
                error.postValue(this?.error?.error_msg)
            }
        }
    }

    fun addPhoto(file: File) {
        viewModelScope.launch(exceptionHandler) {
            val list = mutableListOf<File>()
            photos.value?.map {
                list.add(it)
            }
            list.add(file)
            photos.postValue(list)
        }
    }
}