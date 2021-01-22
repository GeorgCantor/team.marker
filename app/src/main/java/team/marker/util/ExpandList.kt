package team.marker.util

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator

class ExpandList(val view: View, private val icon: View) {

    var isCollapsed = true
    var inProgress = false

    fun toggleList(mode: String? = "default") {
        if (inProgress) return
        inProgress = true

        if (isCollapsed) {
            expand(view)
            isCollapsed = false
            icon.rotate(mode, 0F, 180F)
        } else {
            collapse(view)
            isCollapsed = true
            icon.rotate(mode, 180F, 0F)
        }
    }

    private fun expand(v: View) {
        v.measure(MATCH_PARENT, WRAP_CONTENT)
        val targetHeight = v.measuredHeight
        v.layoutParams.height = 1
        v.visibility = VISIBLE

        ValueAnimator.ofInt(1, targetHeight).apply {
            addUpdateListener {
                v.layoutParams.height = WRAP_CONTENT
                v.requestLayout()
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator) {
                    inProgress = false
                }

                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            duration = 500
            interpolator = OvershootInterpolator()
            start()
        }
    }

    private fun collapse(v: View) {
        val initialHeight = v.measuredHeight

        ValueAnimator.ofInt(initialHeight, 0).apply {
            addUpdateListener { animation ->
                v.layoutParams.height = (animation.animatedValue as Int)
                v.requestLayout()
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator) {
                    v.visibility = GONE
                    inProgress = false
                }

                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            duration = 500
            interpolator = DecelerateInterpolator()
            start()
        }
    }
}