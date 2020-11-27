package team.marker.util

import android.content.Context
import android.net.ConnectivityManager
import android.os.Handler
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import java.util.concurrent.TimeUnit

// common

fun runDelayed(delay: Long, action: () -> Unit) {
    Handler().postDelayed(action, TimeUnit.MILLISECONDS.toMillis(delay))
}

fun Int.nameCase(names: Array<String>): String {
    // calculate
    val count = kotlin.math.abs(this)
    val a1 = count % 10
    val a2 = count % 100
    // output
    if (a1 == 1 && (a2 <= 10 || a2 > 20)) return names[0]
    if (a1 in 2..4 && (a2 <= 10 || a2 > 20)) return names[1]
    return names[2]
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

fun Context.shortToast(message: String) = Toast.makeText(this, message, LENGTH_SHORT).show()