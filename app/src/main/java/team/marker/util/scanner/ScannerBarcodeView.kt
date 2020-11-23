package team.marker.util.scanner

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import com.google.zxing.DecodeHintType
import com.journeyapps.barcodescanner.Util
import team.marker.R
import team.marker.util.scanner.common.ScannerBarcodeCallback
import team.marker.util.scanner.common.ScannerBarcodeResultMultiple
import team.marker.util.scanner.common.ScannerResultPoint
import team.marker.util.scanner.decoder.*
import java.util.*

class ScannerBarcodeView : ScannerCameraPreview {
    private enum class DecodeMode {
        NONE, SINGLE, CONTINUOUS
    }

    private var decodeMode = DecodeMode.NONE
    private var callback: ScannerBarcodeCallback? = null
    private var decoderThread: ScannerDecoderThread? = null
    private var decoderFactory: ScannerDecoderFactory? = null
    private var resultHandler: Handler? = null
    private val resultCallback = Handler.Callback { message ->
        when (message.what) {
            R.id.zxing_decode_succeeded -> {
                val result = message.obj as ScannerBarcodeResultMultiple
                if (result != null) {
                    if (callback != null && decodeMode != DecodeMode.NONE) {
                        callback!!.barcodeResult(result)
                        if (decodeMode == DecodeMode.SINGLE) stopDecoding()
                    }
                }
                return@Callback true
            }
            R.id.zxing_decode_failed -> {
                return@Callback true
            }
            R.id.zxing_possible_result_points -> {
                val resultPoints = message.obj as List<ScannerResultPoint>
                if (callback != null && decodeMode != DecodeMode.NONE) callback!!.possibleResultPoints(resultPoints)
                return@Callback true
            }
            else -> false
        }
    }

    constructor(context: Context?) : super(context!!) {
        initialize()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        initialize()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr) {
        initialize()
    }

    private fun initialize() {
        decoderFactory = ScannerDefaultDecoderFactory()
        resultHandler = Handler(resultCallback)
    }

    fun setDecoderFactory(decoderFactory: ScannerDecoderFactory?) {
        Util.validateMainThread()
        this.decoderFactory = decoderFactory
        if (decoderThread != null) decoderThread!!.decoder = createDecoder()
    }

    private fun createDecoder(): ScannerDecoder {
        if (decoderFactory == null) decoderFactory = createDefaultDecoderFactory()
        val callback = ScannerDecoderResultPointCallback()
        val hints: MutableMap<DecodeHintType, Any?> = HashMap()
        hints[DecodeHintType.NEED_RESULT_POINT_CALLBACK] = callback
        val decoder = decoderFactory!!.createDecoder(hints)
        callback.decoder = decoder
        return decoder
    }

    fun getDecoderFactory(): ScannerDecoderFactory? {
        return decoderFactory
    }

    fun decodeSingle(callback: ScannerBarcodeCallback?) {
        decodeMode = DecodeMode.SINGLE
        this.callback = callback
        startDecoderThread()
    }

    fun decodeContinuous(callback: ScannerBarcodeCallback?) {
        decodeMode = DecodeMode.CONTINUOUS
        this.callback = callback
        startDecoderThread()
    }

    fun stopDecoding() {
        decodeMode = DecodeMode.NONE
        callback = null
        stopDecoderThread()
    }

    protected fun createDefaultDecoderFactory(): ScannerDecoderFactory {
        return ScannerDefaultDecoderFactory()
    }

    private fun startDecoderThread() {
        stopDecoderThread() // To be safe
        if (decodeMode != DecodeMode.NONE && isPreviewActive) {
            // We only start the thread if both:
            // 1. decoding was requested
            // 2. the preview is active
            decoderThread =
                ScannerDecoderThread(
                    cameraInstance,
                    createDecoder(),
                    resultHandler
                )
            decoderThread!!.cropRect = previewFramingRect
            decoderThread!!.start()
        }
    }

    override fun previewStarted() {
        super.previewStarted()
        startDecoderThread()
    }

    private fun stopDecoderThread() {
        if (decoderThread != null) {
            decoderThread!!.stop()
            decoderThread = null
        }
    }

    override fun pause() {
        stopDecoderThread()
        super.pause()
    }
}