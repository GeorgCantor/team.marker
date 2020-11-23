package team.marker.util.scanner.decoder

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import com.google.zxing.LuminanceSource
import com.journeyapps.barcodescanner.SourceData
import com.journeyapps.barcodescanner.Util
import com.journeyapps.barcodescanner.camera.CameraInstance
import com.journeyapps.barcodescanner.camera.PreviewCallback
import team.marker.R
import team.marker.util.scanner.common.ScannerBarcodeResultMultiple
import team.marker.util.scanner.common.ScannerResult

class ScannerDecoderThread(cameraInstance: CameraInstance?, decoder: ScannerDecoder, resultHandler: Handler?) {
    private var cameraInstance: CameraInstance? = null
    private var thread: HandlerThread? = null
    private var handler: Handler? = null
    var decoder: ScannerDecoder
    private val resultHandler: Handler?
    var cropRect: Rect? = null
    private var running = false
    private val LOCK = Any()
    private val callback = Handler.Callback { message ->
        //if (message.what == R.id.zxing_decode) decode(message.obj as SourceData)
        if (message.what == R.id.zxing_decode) decodeMultiple(message.obj as SourceData)
        else if (message.what == R.id.zxing_preview_failed) requestNextPreview()
        true
    }

    fun start() {
        Util.validateMainThread()
        thread = HandlerThread(TAG)
        thread!!.start()
        handler = Handler(thread!!.looper, callback)
        running = true
        requestNextPreview()
    }

    fun stop() {
        Util.validateMainThread()
        synchronized(LOCK) {
            running = false
            handler!!.removeCallbacksAndMessages(null)
            thread!!.quit()
        }
    }

    private val previewCallback: PreviewCallback =
        object : PreviewCallback {
            override fun onPreview(sourceData: SourceData) {
                synchronized(LOCK) {
                    if (running) handler!!.obtainMessage(R.id.zxing_decode, sourceData).sendToTarget()
                }
            }

            override fun onPreviewError(e: Exception) {
                synchronized(LOCK) {
                    if (running) handler!!.obtainMessage(R.id.zxing_preview_failed).sendToTarget()
                }
            }
        }

    private fun requestNextPreview() {
        if (cameraInstance!!.isOpen) cameraInstance!!.requestPreview(previewCallback)
    }

    protected fun createSource(sourceData: SourceData): LuminanceSource? {
        return if (cropRect == null) null else sourceData.createSource()
    }

    private fun decode(sourceData: SourceData) {
        val start = System.currentTimeMillis()
        var rawResult: ScannerResult? = null
        sourceData.cropRect = cropRect
        val source = createSource(sourceData)
        if (source != null) rawResult = decoder.decode(source)
        if (rawResult != null) {
            val end = System.currentTimeMillis()
            Log.d(TAG, "Found barcode in " + (end - start) + " ms")
//            if (resultHandler != null) {
//                val barcodeResult = ScannerBarcodeResult(rawResult, sourceData)
//                val message = Message.obtain(resultHandler, R.id.zxing_decode_succeeded, barcodeResult)
//                val bundle = Bundle()
//                message.data = bundle
//                message.sendToTarget()
//            }
        } else {
            if (resultHandler != null) {
                val message = Message.obtain(resultHandler, R.id.zxing_decode_failed)
                message.sendToTarget()
            }
        }
        if (resultHandler != null) {
            val resultPoints = decoder.possibleResultPoints
            val message = Message.obtain(resultHandler, R.id.zxing_possible_result_points, resultPoints)
            message.sendToTarget()
        }
        requestNextPreview()
    }

    private fun decodeMultiple(sourceData: SourceData) {
        val start = System.currentTimeMillis()
        var rawResult: Array<ScannerResult?>? = null
        sourceData.cropRect = cropRect
        val source = createSource(sourceData)
        if (source != null) rawResult = decoder.decodeMultiple(source)
        if (rawResult != null) {
            val end = System.currentTimeMillis()
            Log.d(TAG, "Found barcode in " + (end - start) + " ms")
            if (resultHandler != null) {
                val barcodeResult = ScannerBarcodeResultMultiple(rawResult)
                val message = Message.obtain(resultHandler, R.id.zxing_decode_succeeded, barcodeResult)
                val bundle = Bundle()
                message.data = bundle
                message.sendToTarget()
            }
        } else {
            if (resultHandler != null) {
                val message = Message.obtain(resultHandler, R.id.zxing_decode_failed)
                message.sendToTarget()
            }
        }
        if (resultHandler != null) {
            val resultPoints = decoder.possibleResultPoints
            val message = Message.obtain(resultHandler, R.id.zxing_possible_result_points, resultPoints)
            message.sendToTarget()
        }
        requestNextPreview()
    }

    companion object {
        private val TAG = ScannerDecoderThread::class.java.simpleName
    }

    init {
        Util.validateMainThread()
        this.cameraInstance = cameraInstance
        this.decoder = decoder
        this.resultHandler = resultHandler
    }
}