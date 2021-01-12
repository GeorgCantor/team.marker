package team.marker.view.pick.camera.barcode

import android.graphics.*
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.vision.barcode.Barcode
import team.marker.view.pick.camera.GraphicOverlay
import team.marker.view.pick.camera.GraphicOverlay.Graphic
import team.marker.view.pick.complete.PickCompleteViewModel

/**
 * Graphic instance for rendering barcode position, size, and ID within an associated graphic
 * overlay view.
 */
class BarcodeGraphic internal constructor(
    overlay: GraphicOverlay<*>?,
    private val viewModel: PickCompleteViewModel,
    private val lifecycleOwner: LifecycleOwner
) : Graphic(overlay!!) {
    var id = 0
    private val rectPaint: Paint = Paint()
    private val textPaint: Paint
    private val backgroundPaint: Paint
    private val redPaint: Paint
    private val greenPaint: Paint
    private val whiteTextPaint: Paint
    private var prodName = ""
    private var isClick = false

    @Volatile
    var barcode: Barcode? = null
        private set

    /**
     * Updates the barcode instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    fun updateItem(barcode: Barcode?) {
        this.barcode = barcode
        postInvalidate()
    }

    override fun draw(canvas: Canvas?) {
        val barcode = barcode ?: return
        val rect = RectF(barcode.boundingBox)
        rect.left = translateX(rect.left)
        rect.top = translateY(rect.top)
        rect.right = translateX(rect.right)
        rect.bottom = translateY(rect.bottom)
        canvas!!.drawRect(rect, rectPaint)

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
            canvas.drawRect(background, backgroundPaint)
            val buttonRect: Rect = getTextBackgroundSize(rect.left, rect.bottom + 200, prodName, textPaint)
            val halfTextLength = textPaint.measureText(prodName) / 2 + 5
            if (isClick) {
                canvas.drawRect(buttonRect, redPaint)
                canvas.drawText("Удалить", (rect.left - halfTextLength), rect.bottom + 200, whiteTextPaint)
            } else {
                canvas.drawRect(buttonRect, greenPaint)
                canvas.drawText("Добавить", (rect.left - halfTextLength), rect.bottom + 200, whiteTextPaint)
            }

            viewModel.setRect(background, prodName)
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

    init {
        rectPaint.color = Color.GREEN
        rectPaint.style = Paint.Style.FILL
        rectPaint.alpha = 50
        rectPaint.strokeWidth = 4.0f

        textPaint = Paint()
        textPaint.color = Color.BLACK
        textPaint.textSize = 42.0f

        backgroundPaint = Paint()
        backgroundPaint.color = Color.WHITE
        backgroundPaint.textSize = 906.0f

        redPaint = Paint()
        redPaint.color = Color.RED
        redPaint.textSize = 906.0f

        greenPaint = Paint()
        greenPaint.color = Color.GREEN
        greenPaint.textSize = 906.0f

        whiteTextPaint = Paint()
        whiteTextPaint.color = Color.WHITE
        whiteTextPaint.textSize = 62.0f
    }
}