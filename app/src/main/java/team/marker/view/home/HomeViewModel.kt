package team.marker.view.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import team.marker.R
import team.marker.model.HomeButton
import team.marker.model.remote.ApiRepository
import team.marker.model.responses.ResponseMessage

class HomeViewModel(
    private val app: Application,
    private val repository: ApiRepository
) : AndroidViewModel(app) {

    val buttons = MutableLiveData<List<HomeButton>>()
    val response = MutableLiveData<ResponseMessage>()
    val error = MutableLiveData<String>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        error.postValue(throwable.message)
    }

    init {
        viewModelScope.launch {
            with(app.baseContext){
                buttons.postValue(listOf(
                    HomeButton(R.drawable.ic_qr_code, getString(R.string.recognize),getString(R.string.get_product_info)),
                    HomeButton(R.drawable.ic_box_home, getString(R.string.capitalize),getString(R.string.scan_codes_for_pick)),
                    HomeButton(R.drawable.ic_box_home, getString(R.string.create_ttn),getString(R.string.create_ttn_full)),
                    HomeButton(R.drawable.ic_breach, getString(R.string.breach),getString(R.string.send_breach_request))
                ))
            }
        }
    }

    fun logout() {
        viewModelScope.launch(exceptionHandler) {
            repository.logout().apply {
                response.postValue(this?.response)
                error.postValue(this?.error?.error_msg)
            }
        }
    }
}