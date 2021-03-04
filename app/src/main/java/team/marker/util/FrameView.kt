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

    private val paint = Paint()
    private var mX: Float
    private var mY: Float

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = ContextCompat.getColor(context, R.color.yellow)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4F
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

    init {
        mY = -100F
        mX = mY
    }
}