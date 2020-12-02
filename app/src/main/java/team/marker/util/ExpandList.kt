package team.marker.util

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.view.animation.RotateAnimation

class ExpandList(val view: View, val icon: View) {

    var isCollapsed = true
    var inProgress = false

    fun toggleList(mode: String? = "default") {
        if (inProgress) return
        inProgress = true
        if (isCollapsed) {
            expand(view)
            isCollapsed = false

            val rotate = RotateAnimation(0F, 180F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            rotate.duration = if (mode == "force") 0 else 300
            rotate.interpolator = DecelerateInterpolator()
            rotate.fillAfter = true
            icon.startAnimation(rotate)
        } else {
            collapse(view)
            isCollapsed = true

            val rotate = RotateAnimation(180F, 0F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            rotate.duration = if (mode == "force") 0 else 300
            rotate.interpolator = DecelerateInterpolator()
            rotate.fillAfter = true
            icon.startAnimation(rotate)
        }
    }

    private fun expand(v: View) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val targetHeight = v.measuredHeight
        v.layoutParams.height = 1
        v.visibility = View.VISIBLE

        val va = ValueAnimator.ofInt(1, targetHeight)
        va.addUpdateListener {
            //v.layoutParams.height = (animation.animatedValue as Int)
            v.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            v.requestLayout()
        }
        va.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                //v.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                //v.visibility = View.VISIBLE
                inProgress = false
            }
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        //v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        //va.duration = (v.measuredHeight / v.context.resources.displayMetrics.density).roundToLong()
        //Log.e("Message", va.duration.toString())
        va.duration = 500
        va.interpolator = OvershootInterpolator()
        va.start()
    }

    private fun collapse(v: View) {
        val initialHeight = v.measuredHeight

        val va = ValueAnimator.ofInt(initialHeight, 0)
        va.addUpdateListener { animation ->
            v.layoutParams.height = (animation.animatedValue as Int)
            v.requestLayout()
        }
        va.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                v.visibility = View.GONE
                inProgress = false
            }
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        //v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        //v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //va.duration = ((initialHeight) / v.context.resources.displayMetrics.density).roundToLong()
        //Log.e("Message", va.duration.toString())
        va.duration = 500
        va.interpolator = DecelerateInterpolator()
        va.start()
    }
}