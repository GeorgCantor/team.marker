package team.marker.view.pick.camera.barcode

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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
    private val mRectPaint: Paint = Paint()
    private val mTextPaint: Paint
    private var prodName = ""

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

    /**
     * Draws the barcode annotations for position, size, and raw value on the supplied canvas.
     */
    override fun draw(canvas: Canvas?) {
        val barcode = barcode ?: return
        val rect = RectF(barcode.boundingBox)
        rect.left = translateX(rect.left)
        rect.top = translateY(rect.top)
        rect.right = translateX(rect.right)
        rect.bottom = translateY(rect.bottom)
        canvas!!.drawRect(rect, mRectPaint)

        viewModel.products.observe(lifecycleOwner) {
            viewModel.getProduct(barcode.rawValue?.takeLastWhile { it.isDigit() })
            viewModel.product.observe(lifecycleOwner) {
                if (prodName != it) prodName = it
            }
        }
        canvas.drawText(prodName, rect.right, rect.bottom, mTextPaint)
    }

    init {
        mRectPaint.color = Color.GREEN
        mRectPaint.style = Paint.Style.FILL
        mRectPaint.alpha = 50
        mRectPaint.strokeWidth = 4.0f
        mTextPaint = Paint()
        mTextPaint.color = Color.BLACK
        mTextPaint.textSize = 36.0f
    }
}