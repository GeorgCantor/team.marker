package team.marker.util.scanner.common

import com.google.zxing.Result
import com.journeyapps.barcodescanner.SourceData

class ScannerBarcodeResultMultiple(var result: Array<Result?>, protected var sourceData: SourceData) {

    /*val bitmapScaleFactor = 2
    val bitmap: Bitmap get() = sourceData.getBitmap(bitmapScaleFactor)

    fun getBitmapWithResultPoints(color: Int): Bitmap? {
        val bitmap: Bitmap? = bitmap
        var barcode = bitmap
        val points = result[0]?.resultPoints
        if (points != null && points.size > 0 && bitmap != null) {
            barcode = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(barcode)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            val paint = Paint()
            paint.color = color
            if (points.size == 2) {
                paint.strokeWidth = PREVIEW_LINE_WIDTH
                drawLine(canvas, paint, points[0], points[1], bitmapScaleFactor)
            } else if (points.size == 4 && (result[0]?.barcodeFormat == BarcodeFormat.UPC_A || result[0]?.barcodeFormat == BarcodeFormat.EAN_13)) {
                drawLine(canvas, paint, points[0], points[1], bitmapScaleFactor)
                drawLine(canvas, paint, points[2], points[3], bitmapScaleFactor)
            } else {
                paint.strokeWidth = PREVIEW_DOT_WIDTH
                for (point in points) {
                    if (point != null) canvas.drawPoint(point.x / bitmapScaleFactor, point.y / bitmapScaleFactor, paint)
                }
            }
        }
        return barcode
    }

    val text: String get() = result[0]?.text!!
    val rawBytes: ByteArray get() = result[0]?.rawBytes!!
    val resultPoints: Array<ResultPoint> get() = result[0]?.resultPoints!!
    val barcodeFormat: BarcodeFormat get() = result[0]?.barcodeFormat!!
    val resultMetadata: Map<ResultMetadataType, Any> get() = result[0]?.resultMetadata!!
    val timestamp: Long get() = result[0]?.timestamp!!*/

    override fun toString(): String {
        var res = ""
        for (item in result) res += item?.text + ";eot;"
        return res
    }

    /*companion object {
        private const val PREVIEW_LINE_WIDTH = 4.0f
        private const val PREVIEW_DOT_WIDTH = 10.0f
        private fun drawLine(canvas: Canvas, paint: Paint, a: ResultPoint?, b: ResultPoint?, scaleFactor: Int) {
            if (a != null && b != null) {
                canvas.drawLine(a.x / scaleFactor, a.y / scaleFactor, b.x / scaleFactor, b.y / scaleFactor, paint)
            }
        }
    }*/

}