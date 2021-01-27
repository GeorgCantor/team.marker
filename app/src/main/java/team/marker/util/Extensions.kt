package team.marker.util

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper.getMainLooper
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import team.marker.R
import team.marker.util.Constants.FORCE
import team.marker.view.pick.camera.CameraSourcePreview
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.abs

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

fun CameraSourcePreview.calculateFocusArea(x: Float, y: Float, areaSize: Int): Rect {
    val left = java.lang.Float.valueOf(x / width * 2000 - areaSize).toInt().clamp(areaSize)
    val top = java.lang.Float.valueOf(y / height * 2000 - areaSize).toInt().clamp(areaSize)

    return Rect(left, top, left + 1000, top + 1000)
}

private fun Int.clamp(areaSize: Int): Int {
    return if (abs(this) + (areaSize / 2) > 1000) {
        if (this > 0) 1000 - (areaSize / 2) else -1000 + (areaSize / 2)
    } else {
        this - (areaSize / 2)
    }
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

fun View.setVisibility(visible: Boolean) {
    visibility = if (visible) VISIBLE else GONE
}

fun View.visible() { visibility = VISIBLE }

fun View.gone() { visibility = GONE }

fun View.hideKeyboard() = (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        .hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

fun View.rotate(mode: String?, from: Float, to: Float) {
    RotateAnimation(from, to, RELATIVE_TO_SELF, 0.5F, RELATIVE_TO_SELF, 0.5F).apply {
        duration = if (mode == FORCE) 0 else 300
        interpolator = DecelerateInterpolator()
        fillAfter = true
        startAnimation(this)
    }
}

fun Context.isNetworkAvailable() = (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?)
    ?.activeNetworkInfo?.isConnectedOrConnecting ?: false

fun Context.shortToast(message: String) = Toast.makeText(this, message, LENGTH_SHORT).show()

fun Context.loadPhoto(file: File, imageView: ImageView) = Glide.with(this)
    .load(file)
    .into(imageView)

fun Context.showError(textView: TextView, message: String?) {
    textView.text = message
    val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
    animation.reset()
    textView.clearAnimation()
    textView.startAnimation(animation)
    3000L.runDelayed {
        val a = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        a.reset()
        textView.clearAnimation()
        textView.startAnimation(a)
    }
    3350L.runDelayed { textView.text = "" }
}

fun Context.showDialog(action: () -> (Unit)) = AlertDialog.Builder(this).apply {
    setTitle(getString(R.string.logout_dialog_title))
    setPositiveButton(getString(R.string.yes)) { _, _ -> action() }
    setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
    create().show()
}

fun Context.hasInternetBeforeAction() = isNetworkAvailable().apply {
    if (!this) shortToast(getString(R.string.internet_unavailable))
}

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}