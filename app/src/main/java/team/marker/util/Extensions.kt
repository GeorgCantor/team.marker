package team.marker.util

import android.content.Context
import android.content.SharedPreferences
import android.graphics.PorterDuff.Mode.SRC_IN
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
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.edit
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import team.marker.R
import team.marker.model.Dialog
import team.marker.util.Constants.FORCE
import java.io.File
import java.util.concurrent.TimeUnit.MILLISECONDS

fun Long.runDelayed(action: () -> Unit) {
    Handler(getMainLooper()).postDelayed(action, MILLISECONDS.toMillis(this))
}

fun Int.nameCase(names: Array<String>): String {
    val count = kotlin.math.abs(this)
    val a1 = count % 10
    val a2 = count % 100
    if (a1 == 1 && (a2 <= 10 || a2 > 20)) return names[0]
    if (a1 in 2..4 && (a2 <= 10 || a2 > 20)) return names[1]

    return names[2]
}

private fun Int.clamp(min: Int, max: Int): Int {
    if (this > max) return max
    return if (this < min) min else this
}

fun SharedPreferences.putAny(key: String, any: Any) {
    when (any) {
        is String -> edit { putString(key, any) }
        is Int -> edit { putInt(key, any) }
    }
}

fun SharedPreferences.getAny(type: Any, key: String) = when (type) {
    is String -> getString(key, "") as Any
    else -> getInt(key, 0)
}

fun View.calculateTapArea(oldX: Float, oldY: Float, coefficient: Float): Rect {
    val y = height - oldX
    val focusAreaSize = 300F
    val areaSize = java.lang.Float.valueOf(focusAreaSize * coefficient).toInt()
    val centerX = (oldY / width * 2000 - 1000).toInt()
    val centerY = (y / height * 2000 - 1000).toInt()
    val left = (centerX - areaSize / 2).clamp(-1000, 1000)
    val right = (left + areaSize).clamp(-1000, 1000)
    val top = (centerY - areaSize / 2).clamp(-1000, 1000)
    val bottom = (top + areaSize).clamp(-1000, 1000)

    return Rect(left, top, right, bottom)
}

fun View.visible() { visibility = VISIBLE }

fun View.gone() { visibility = GONE }

fun View.slideUp() {
    startAnimation(
        TranslateAnimation(0F, 0F, height.toFloat(), 0F).apply {
            duration = 500
            fillAfter = true
        }
    )
    visible()
}

fun View.slideDown() {
    startAnimation(
        TranslateAnimation(0F, 0F, 0F, height.toFloat()).apply {
            duration = 500
            fillAfter = true
        }
    )
    gone()
}

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

fun View.showPermissionSnackbar(listener: View.OnClickListener) = Snackbar
    .make(this, R.string.permission_rationale, LENGTH_INDEFINITE)
    .setAction(context.getString(R.string.ok), listener)
    .show()

fun ImageView.setBlueColor() = setColorFilter(getColor(context, R.color.dark_blue), SRC_IN)

fun Context.isNetworkAvailable() = (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?)
    ?.activeNetworkInfo?.isConnectedOrConnecting ?: false

fun Context.shortToast(message: String) = Toast.makeText(this, message, LENGTH_SHORT).show()

fun Context.longToast(message: String) = Toast.makeText(this, message, LENGTH_LONG).show()

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

fun Context.showDialog(dialog: Dialog) = AlertDialog.Builder(this).apply {
    setTitle(dialog.title)
    dialog.message?.let { setMessage(it) }
    dialog.posText?.let { setPositiveButton(it) { _, _ -> dialog.action() } }
    dialog.negText?.let { setNegativeButton(it) { dialog, _ -> dialog.dismiss() } }
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

inline fun <reified T> String.toObject(): T = Gson().fromJson(this, object : TypeToken<T>() {}.type)