package team.marker.util.scanner

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.scanner.view.*
import team.marker.R
import team.marker.util.scanner.common.ScannerBarcodeCallback
import team.marker.util.scanner.common.ScannerBarcodeResultMultiple

@Suppress("CAST_NEVER_SUCCEEDS")
open class ScannerDecoratedBarcodeView : FrameLayout {

    var barcodeView: ScannerBarcodeView? = null
    private var viewFinder: ScannerViewfinderView? = null
    private var statusView: TextView? = null
    private var torchListener: TorchListener? = null

    private class WrappedCallback(private val delegate: ScannerBarcodeCallback) : ScannerBarcodeCallback {
        override fun barcodeResult(result: ScannerBarcodeResultMultiple) {
            delegate.barcodeResult(result)
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {
            delegate.possibleResultPoints(resultPoints)
        }
    }

    constructor(context: Context?) : super(context!!) {
        initialize()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        initialize(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr) {
        initialize(attrs)
    }

    private fun initialize(attrs: AttributeSet? = null) {
        // Get attributes set on view
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.zxing_view)
        val scannerLayout = attributes.getResourceId(R.styleable.zxing_view_zxing_scanner_layout, R.layout.scanner)
        attributes.recycle()
        View.inflate(context, scannerLayout, this)
        barcodeView = zxing_barcode_surface
        requireNotNull(barcodeView) {
            "There is no a com.journeyapps.barcodescanner.BarcodeView on provided layout " +
                    "with the id \"zxing_barcode_surface\"."
        }

        barcodeView!!.initializeAttributes(attrs)
        viewFinder = zxing_viewfinder_view as ScannerViewfinderView
        requireNotNull(viewFinder) {
            "There is no a com.journeyapps.barcodescanner.ViewfinderView on provided layout " +
                    "with the id \"zxing_viewfinder_view\"."
        }
        viewFinder!!.setCameraPreview(barcodeView!!)
        statusView = zxing_status_view
    }

    fun pause() {
        barcodeView!!.pause()
    }

    fun pauseAndWait() {
        barcodeView!!.pauseAndWait()
    }

    fun resume() {
        barcodeView!!.resume()
    }

    internal fun getBarcodeView(): ScannerBarcodeView {
        return zxing_barcode_surface
    }

    fun decodeSingle(callback: ScannerBarcodeCallback) {
        barcodeView!!.decodeSingle(WrappedCallback(callback))
    }

    fun decodeContinuous(callback: ScannerBarcodeCallback) {
        barcodeView!!.decodeContinuous(WrappedCallback(callback))
    }

    fun setTorchOn() {
        barcodeView!!.setTorch(true)
        if (torchListener != null) torchListener!!.onTorchOn()
    }

    fun setTorchOff() {
        barcodeView!!.setTorch(false)
        if (torchListener != null) torchListener!!.onTorchOff()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_FOCUS, KeyEvent.KEYCODE_CAMERA -> return true
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                setTorchOff()
                return true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                setTorchOn()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    fun setTorchListener(listener: TorchListener?) {
        torchListener = listener
    }

    interface TorchListener {
        fun onTorchOn()
        fun onTorchOff()
    }
}