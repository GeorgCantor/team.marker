package team.marker.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.core.content.ContextCompat
import team.marker.R

class FrameView(
    context: Context,
    attributeSet: AttributeSet?
) : View(context, attributeSet), OnTouchListener {

    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.yellow)
        style = Paint.Style.STROKE
        strokeWidth = 4F
    }
    private var mX = 0F
    private var mY = 0F

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(mX - 200, mY + 200, mX + 200, mY - 200, paint)
        invalidate()
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            mX = event.x
            mY = event.y
        }
        return false
    }
}