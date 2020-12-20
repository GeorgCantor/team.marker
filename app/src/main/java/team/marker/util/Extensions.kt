package team.marker.util

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper.getMainLooper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.bumptech.glide.Glide
import team.marker.R
import java.io.File
import java.util.concurrent.TimeUnit

fun Long.runDelayed(action: () -> Unit) {
    Handler(getMainLooper()).postDelayed(action, TimeUnit.MILLISECONDS.toMillis(this))
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

fun SharedPreferences.putAny(key: String, any: Any) {
    when (any) {
        is String -> edit().putString(key, any).apply()
        is Int -> edit().putInt(key, any).apply()
    }
}

fun SharedPreferences.getAny(type: Any, key: String): Any {
    return when (type) {
        is String -> getString(key, "") as Any
        else -> getInt(key, 0)
    }
}

fun Context.isNetworkAvailable() = (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?)
    ?.activeNetworkInfo?.isConnectedOrConnecting ?: false

fun Context.shortToast(message: String) = Toast.makeText(this, message, LENGTH_SHORT).show()

fun Context.loadPhoto(file: File, imageView: ImageView) = Glide.with(this)
    .load(file)
    .into(imageView)

fun Context.showError(textView: TextView, message: String?, hide: Int) {
    textView.text = message
    val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
    animation.reset()
    textView.clearAnimation()
    textView.startAnimation(animation)
    if (hide > 0) {
        3000L.runDelayed {
            val a = AnimationUtils.loadAnimation(this, R.anim.fade_out)
            a.reset()
            textView.clearAnimation()
            textView.startAnimation(a)
        }
        3350L.runDelayed { textView.text = "" }
    }
}