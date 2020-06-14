package team.marker.util

import android.content.Context
import android.net.ConnectivityManager
import android.os.Handler
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
//import kotlinx.android.synthetic.main.dialog_accounts.*
import team.marker.R
import java.util.concurrent.TimeUnit

// common

fun runDelayed(delay: Long, action: () -> Unit) {
    Handler().postDelayed(action, TimeUnit.MILLISECONDS.toMillis(delay))
}

// context

fun Context.isNetworkAvailable(): Boolean {
    val manager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    manager?.let {
        val networkInfo = it.activeNetworkInfo
        networkInfo?.let { info ->
            if (info.isConnected) return true
        }
    }

    return false
}

fun Context.shortToast(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()