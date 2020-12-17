package team.marker.util

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper.getMainLooper
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import team.marker.R
import java.io.File
import java.util.concurrent.TimeUnit

// common

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

fun AppCompatActivity.openFragment(fragment: Fragment) {
    val transaction = supportFragmentManager.beginTransaction()
    transaction.setCustomAnimations(
        R.anim.slide_in_right,
        R.anim.slide_out_left,
        R.anim.slide_in_left,
        R.anim.slide_out_right
    )
    transaction.add(R.id.frame_container, fragment)
    transaction.addToBackStack(null)
    transaction.commit()
}

fun View.hideKeyboard() = (context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
    .hideSoftInputFromWindow(windowToken, HIDE_NOT_ALWAYS)

// context

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