package team.marker.view.breach.complete

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import team.marker.model.DeferredFiles
import team.marker.model.remote.ApiRepository
import team.marker.model.responses.ResponseMessage
import team.marker.util.Constants.DEFERRED_FILES
import team.marker.util.Constants.IMAGE_DIR
import team.marker.util.putAny
import java.io.File

class BreachCompleteViewModel(
    private val app: Application,
    private val repository: ApiRepository,
    private val preferences: SharedPreferences
) : AndroidViewModel(app) {

    val response = MutableLiveData<ResponseMessage>()
    val error = MutableLiveData<String>()
    val photos = MutableLiveData<MutableList<File>>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        error.postValue(throwable.message)
    }

    fun breach(productId: Int, reasonId: Int, userReason: String, comment: String) {
        viewModelScope.launch(exceptionHandler) {
            val files = mutableListOf<MultipartBody.Part>()
            photos.value?.forEach {
                val requestBody = it.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("files[]", it.name, requestBody)
                files.add(filePart)
            }
            repository.breach(
                productId,
                reasonId,
                userReason.toRequestBody("text/plain".toMediaTypeOrNull()),
                comment.toRequestBody("text/plain".toMediaTypeOrNull()),
                files.toTypedArray()
            ).apply {
                response.postValue(this?.response)
                error.postValue(this?.error?.error_msg)
                removeFiles()

                preferences.putAny(DEFERRED_FILES, "")
                val cw = ContextWrapper(app.baseContext)
                val directory = cw.getDir(IMAGE_DIR, MODE_PRIVATE)
                directory.deleteRecursively()
            }
        }
    }

    fun addPhoto(file: File) {
        viewModelScope.launch(exceptionHandler) {
            val list = mutableListOf<File>()
            photos.value?.forEach { list.add(it) }
            list.add(file)
            photos.postValue(list)
        }
    }

    fun removePhoto(file: File) {
        viewModelScope.launch(exceptionHandler) {
            val list = photos.value
            list?.remove(file)
            photos.postValue(list)
        }
    }

    fun removeFiles() {
        photos.value = mutableListOf()
    }

    fun saveFilePathsForDeferredSending(productId: Int, comment: String) {
        val files = DeferredFiles(productId, 0, "", comment, photos.value?.map { it.path } ?: emptyList())
        val gson = Gson()
        val json = gson.toJson(files)
        preferences.putAny(DEFERRED_FILES, json)
    }

    fun sendDeferredFiles(files: DeferredFiles) {
            val photoFiles = mutableListOf<File>()
            files.filePaths.forEach { photoFiles.add(File(it)) }
            photos.value = photoFiles
            breach(files.productId, files.reasonId, files.userReason, files.comment)
    }
}