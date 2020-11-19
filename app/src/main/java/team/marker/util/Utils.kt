package team.marker.util

import android.content.Context
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import team.marker.R

fun showError(context: Context?, textView: TextView, message: String?, hide: Int, gone: Int) {
    //if (gone == 1) textView.visibility = ProgressBar.VISIBLE
    textView.text = message
    val animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
    animation.reset()
    textView.clearAnimation()
    textView.startAnimation(animation)
    if (hide > 0) {
        Handler().postDelayed(
            {
                val a = AnimationUtils.loadAnimation(context, R.anim.fade_out)
                a.reset()
                textView.clearAnimation()
                textView.startAnimation(a)
            },
            3000
        )
        Handler().postDelayed(
            {
                textView.text = ""
                //if (gone == 1) textView.visibility = ProgressBar.GONE
            },
            3350
        )
    }
}

fun hideKeyboard(view: View) {
    val inputManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}