package team.marker.view.pick.camera.barcode

import android.R.attr.x
import android.R.attr.y
import android.graphics.*
import android.text.TextPaint
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.vision.barcode.Barcode
import kotlinx.android.synthetic.main.fragment_pick.*
import team.marker.view.pick.camera.GraphicOverlay
import team.marker.view.pick.camera.GraphicOverlay.Graphic
import team.marker.view.pick.complete.PickCompleteViewModel
import java.util.*


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
    private var prodName = ""
    private var lastTime: Date? = null
    private var lastProductId = "0"

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
        val seconds: Long = if (lastTime != null) (Date().time - lastTime!!.time) / 1000 else 100
        //Log.e("seconds 2", seconds.toString())
        val productId = barcode.rawValue.takeLastWhile { it.isDigit() }
        viewModel.products.observe(lifecycleOwner) {
            if (seconds > 3 && productId != lastProductId) {
                //Log.e("new product 2", productId + ":" + lastProductId)
                //Log.e("time", lastTime?.time.toString())
                lastTime = Date()
                lastProductId = productId
                viewModel.getProduct(productId)
            }
            viewModel.product.observe(lifecycleOwner) {
                //val newProdName = if (!viewModel.productIds.contains(productId)) it else "[добавлен] " + it
                if (prodName != it) prodName = it
            }
        }

        //canvas.drawText("_", rect.left, rect.bottom + 50, backgroundPaint)
        if (prodName.isNotEmpty()) {
            val background: Rect = getTextBackgroundSize(rect.left, rect.bottom + 100, prodName, textPaint)
            canvas.drawRect(background, backgroundPaint)
            val halfTextLength = textPaint.measureText(prodName) / 2 + 5
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
    }
}