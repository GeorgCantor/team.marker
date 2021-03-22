package team.marker.view.ttn.cargo.create

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import team.marker.model.remote.ApiRepository

class CreateTtnViewModel(private val repository: ApiRepository) : ViewModel() {

    val progressIsVisible = MutableLiveData<Boolean>().apply { value = false }
    val showSuccessScreen = MutableLiveData<Boolean>().apply { value = false }

    fun sendTtn() {
        showSuccessScreen.value = true
    }
}