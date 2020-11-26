package team.marker.util.scanner

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import team.marker.R
import team.marker.util.scanner.common.ScannerResultPoint
import java.util.*

class ScannerViewfinderView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var resultBitmap: Bitmap? = null
    private val maskColor: Int
    private val resultColor: Int
    private val laserColor: Int
    private val resultPointColor: Int
    private var scannerAlpha: Int
    private var possibleResultPoints: MutableList<ScannerResultPoint>
    private var lastPossibleResultPoints: List<ScannerResultPoint>?
    private var cameraPreview: ScannerCameraPreview? = null
    private var framingRect: Rect? = null
    private var previewFramingRect: Rect? = null

    fun setCameraPreview(view: ScannerCameraPreview) {
        cameraPreview = view
        view.addStateListener(object : ScannerCameraPreview.StateListener {
            override fun previewSized() {
                refreshSizes()
                invalidate()
            }
            override fun previewStarted() {}
            override fun previewStopped() {}
            override fun cameraError(error: Exception?) { }
            override fun cameraClosed() {}
        })
    }

    private fun refreshSizes() {
        if (cameraPreview == null) return
        val framingRect = cameraPreview!!.framingRect
        val previewFramingRect = cameraPreview!!.previewFramingRect
        if (framingRect != null && previewFramingRect != null) {
            this.framingRect = framingRect
            this.previewFramingRect = previewFramingRect
        }
    }

    @SuppressLint("DrawAllocation")
    public override fun onDraw(canvas: Canvas) {
        refreshSizes()
        if (framingRect == null || previewFramingRect == null) return
        val frame: Rect = framingRect!!
        val previewFrame: Rect = previewFramingRect!!
        val width = width
        val height = height

        paint.color = if (resultBitmap != null) resultColor else maskColor
        canvas.drawRect(0f, 0f, width.toFloat(), frame.top.toFloat(), paint)
        canvas.drawRect(0f, frame.top.toFloat(), frame.left.toFloat(), frame.bottom + 1.toFloat(), paint)
        canvas.drawRect(frame.right + 1.toFloat(), frame.top.toFloat(), width.toFloat(), frame.bottom + 1.toFloat(), paint)
        canvas.drawRect(0f, frame.bottom + 1.toFloat(), width.toFloat(), height.toFloat(), paint)
        if (resultBitmap != null) {
            paint.alpha = CURRENT_POINT_OPACITY
            canvas.drawBitmap(resultBitmap!!, null, frame, paint)
        } else {
            paint.color = laserColor
            paint.alpha = SCANNER_ALPHA[scannerAlpha]
            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.size
            val middle = frame.height() / 2 + frame.top
            canvas.drawRect(frame.left + 2.toFloat(), middle - 1.toFloat(), frame.right - 1.toFloat(), middle + 2.toFloat(), paint)
            val scaleX = frame.width() / previewFrame.width().toFloat()
            val scaleY = frame.height() / previewFrame.height().toFloat()
            val currentPossible: List<ScannerResultPoint> = possibleResultPoints
            val currentLast = lastPossibleResultPoints
            val frameLeft = frame.left
            val frameTop = frame.top
            if (currentPossible.isEmpty()) {
                lastPossibleResultPoints = null
            } else {
                possibleResultPoints = ArrayList(5)
                lastPossibleResultPoints = currentPossible
                paint.alpha = CURRENT_POINT_OPACITY
                paint.color = resultPointColor
                for (point in currentPossible) {
                    canvas.drawCircle(frameLeft + (point.x * scaleX), frameTop + (point.y * scaleY), POINT_SIZE.toFloat(), paint)
                }
            }
            if (currentLast != null) {
                paint.alpha = CURRENT_POINT_OPACITY / 2
                paint.color = resultPointColor
                val radius: Float = POINT_SIZE / 2.0f
                for (point in currentLast) canvas.drawCircle(frameLeft + (point.x * scaleX), frameTop + (point.y * scaleY), radius, paint)
            }

            postInvalidateDelayed(ANIMATION_DELAY, frame.left - POINT_SIZE, frame.top - POINT_SIZE, frame.right + POINT_SIZE, frame.bottom + POINT_SIZE)
        }
    }

    fun drawViewfinder() {
        val resultBitmap = resultBitmap
        this.resultBitmap = null
        resultBitmap?.recycle()
        invalidate()
    }

    fun drawResultBitmap(result: Bitmap?) {
        resultBitmap = result
        invalidate()
    }

    fun addPossibleResultPoint(point: ScannerResultPoint) {
        val points = possibleResultPoints
        points.add(point)
        val size = points.size
        if (size > MAX_RESULT_POINTS) points.subList(0, size - MAX_RESULT_POINTS / 2).clear()
    }

    companion object {
        private val SCANNER_ALPHA = intArrayOf(128, 160, 192, 224, 255, 224, 192, 160)
        //private val SCANNER_ALPHA = intArrayOf(0, 64, 128, 192, 255, 192, 128, 64)
        private const val ANIMATION_DELAY = 200L
        private const val CURRENT_POINT_OPACITY = 0xA0
        private const val MAX_RESULT_POINTS = 20
        private const val POINT_SIZE = 6
    }

    init {
        val resources = resources
        val attributes = getContext().obtainStyledAttributes(attrs, R.styleable.zxing_finder)
        maskColor = attributes.getColor(R.styleable.zxing_finder_zxing_viewfinder_mask, resources.getColor(R.color.zxing_viewfinder_mask))
        resultColor = attributes.getColor(R.styleable.zxing_finder_zxing_result_view, resources.getColor(R.color.zxing_result_view))
        laserColor = attributes.getColor(R.styleable.zxing_finder_zxing_viewfinder_laser, resources.getColor(R.color.scan_viewfinder_laser))
        resultPointColor = attributes.getColor(R.styleable.zxing_finder_zxing_possible_result_points, resources.getColor(R.color.zxing_possible_result_points))
        attributes.recycle()
        scannerAlpha = 0
        possibleResultPoints = ArrayList(5)
        lastPossibleResultPoints = null
    }
}