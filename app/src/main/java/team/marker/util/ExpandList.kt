package team.marker.util

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation.RELATIVE_TO_SELF
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

            val rotate = RotateAnimation(0F, 180F, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f)
            rotate.duration = if (mode == "force") 0 else 300
            rotate.interpolator = DecelerateInterpolator()
            rotate.fillAfter = true
            icon.startAnimation(rotate)
        } else {
            collapse(view)
            isCollapsed = true

            val rotate = RotateAnimation(180F, 0F, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f)
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
            v.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            v.requestLayout()
        }
        va.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                inProgress = false
            }
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

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
        va.duration = 500
        va.interpolator = DecelerateInterpolator()
        va.start()
    }
}