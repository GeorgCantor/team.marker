package team.marker.util

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object NetworkUtils : ConnectivityManager.NetworkCallback() {

    private val networkLiveData: MutableLiveData<Boolean> = MutableLiveData()

    fun getNetworkLiveData(context: Context): LiveData<Boolean> {
        val manager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            manager.registerDefaultNetworkCallback(this)
        } else {
            val builder = NetworkRequest.Builder()
            manager.registerNetworkCallback(builder.build(), this)
        }

        var isConnected = false

        manager.allNetworks.forEach { network ->
            val networkCapability = manager.getNetworkCapabilities(network)

            networkCapability?.let {
                if (it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    isConnected = true
                    return@forEach
                }
            }
        }

        networkLiveData.postValue(isConnected)

        return networkLiveData
    }

    override fun onAvailable(network: Network) {
        5000L.runDelayed { networkLiveData.postValue(true) }
    }

    override fun onLost(network: Network) {
        5000L.runDelayed { networkLiveData.postValue(false) }
    }
}
