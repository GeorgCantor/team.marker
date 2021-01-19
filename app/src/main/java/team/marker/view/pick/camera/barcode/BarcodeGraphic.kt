package team.marker.view.pick.camera.barcode

import android.graphics.Canvas
import android.graphics.Color.*
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Rect
import android.graphics.RectF
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.vision.barcode.Barcode
import team.marker.view.pick.camera.GraphicOverlay
import team.marker.view.pick.camera.GraphicOverlay.Graphic
import team.marker.view.pick.complete.PickCompleteViewModel

class BarcodeGraphic internal constructor(
    overlay: GraphicOverlay<*>?,
    private val viewModel: PickCompleteViewModel,
    private val lifecycleOwner: LifecycleOwner
) : Graphic(overlay!!) {

    var id = 0
    private val rectPaint = Paint().apply {
        color = GREEN
        style = Paint.Style.FILL
        alpha = 50
    }
    private val textPaint = Paint().apply {
        color = BLACK
        textSize = 42.0f
    }
    private val backgroundPaint = Paint().apply { color = WHITE }
    private val redPaint = Paint().apply { color = RED }
    private val greenPaint = Paint().apply { color = GREEN }
    private val whiteTextPaint = Paint().apply {
        color = WHITE
        textSize = 48.0f
    }
    private var prodName = ""
    private var isClick = false

    @Volatile
    var barcode: Barcode? = null
        private set

    fun updateItem(barcode: Barcode?) {
        this.barcode = barcode
        postInvalidate()
    }

    override fun draw(canvas: Canvas?) {
        val barcode = barcode ?: return

        val rect = RectF(barcode.boundingBox).apply {
            left = translateX(left)
            top = translateY(top)
            right = translateX(right)
            bottom = translateY(bottom)
            canvas?.drawRect(this, rectPaint)
        }

        val productId = barcode.rawValue.takeLastWhile { it.isDigit() }

        viewModel.getProduct(productId)
        viewModel.products.observe(lifecycleOwner) {
            it?.let {
                it.forEach {
                    try {
                        if (it.id == productId.toInt()) {
                            prodName = it.name
                            isClick = it.clickStatus == 1
                        }
                    } catch (e: NumberFormatException) {
                    }
                }
            }
        }

        if (prodName.isNotEmpty()) {
            val background: Rect = getTextBackgroundSize(rect.left, rect.bottom + 100, prodName, textPaint)
            canvas?.drawRect(background, backgroundPaint)
            val buttonRect: Rect = getButtonBackground(rect.left, rect.bottom + 200, prodName, textPaint)
            val halfTextLength = textPaint.measureText(prodName) / 2 + 5
            if (isClick) {
                canvas?.drawRect(buttonRect, redPaint)
                drawRectText("УДАЛИТЬ", canvas!!, buttonRect)
            } else {
                canvas?.drawRect(buttonRect, greenPaint)
                drawRectText("ДОБАВИТЬ", canvas!!, buttonRect)
            }

            viewModel.setRect(buttonRect, prodName)
            canvas.drawText(prodName, (rect.left - halfTextLength), rect.bottom + 100, textPaint)
        }
    }

    private fun getTextBackgroundSize(x: Float, y: Float, text: String, paint: Paint): Rect {
        val fontMetrics = paint.fontMetrics
        val halfTextLength = paint.measureText(text) / 2 + 5
        val margin = 20
        return Rect(
            (x - halfTextLength - margin).toInt(),
            (y + fontMetrics.top - margin).toInt(),
            (x + halfTextLength + margin).toInt(),
            (y + fontMetrics.bottom + margin).toInt()
        )
    }

    private fun getButtonBackground(x: Float, y: Float, text: String, paint: Paint): Rect {
        val fontMetrics = paint.fontMetrics
        val halfTextLength = paint.measureText(text) / 2 + 5
        val margin = 20
        return if (halfTextLength > 200) {
            Rect(
                (x - (halfTextLength / 2) - margin).toInt(),
                (y + fontMetrics.top - margin).toInt(),
                (x + (halfTextLength / 2) + margin).toInt(),
                (y + fontMetrics.bottom + margin).toInt()
            )
        } else {
            Rect(
                (x - halfTextLength - margin).toInt(),
                (y + fontMetrics.top - margin).toInt(),
                (x + halfTextLength + margin).toInt(),
                (y + fontMetrics.bottom + margin).toInt()
            )
        }
    }

    private fun drawRectText(text: String, canvas: Canvas, r: Rect) {
        whiteTextPaint.textAlign = Align.CENTER
        val width = r.width()
        val numOfChars = whiteTextPaint.breakText(text, true, width.toFloat(), null)
        val start = (text.length - numOfChars) / 2
        canvas.drawText(text, start, start + numOfChars, r.exactCenterX(), r.exactCenterY() + 15, whiteTextPaint)
    }
}