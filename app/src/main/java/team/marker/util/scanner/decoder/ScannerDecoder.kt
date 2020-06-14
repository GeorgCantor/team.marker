package team.marker.util.scanner.decoder

import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import team.marker.util.scanner.ScannerMultiFormatReader
import team.marker.util.scanner.common.ScannerResultPoint
import team.marker.util.scanner.common.ScannerResultPointCallback
import team.marker.util.scanner.reader.ScannerReader
import java.util.*

open class ScannerDecoder (protected val reader: ScannerReader) : ResultPointCallback {

    fun decode(source: LuminanceSource): Result? {
        return decode(toBitmap(source))
    }

    protected open fun toBitmap(source: LuminanceSource): BinaryBitmap {
        return BinaryBitmap(HybridBinarizer(source))
    }

    protected fun decode(bitmap: BinaryBitmap): Result? {
        possibleResultPoints.clear()
        return try {
            if (reader is ScannerMultiFormatReader) {
                // Optimization - MultiFormatReader's normal decode() method is slow.
                reader.decodeWithState(bitmap)
            } else {
                reader.decode(bitmap)
            }
        } catch (e: Exception) {
            // Decode error, try again next frame
            null
        } finally {
            reader.reset()
        }
    }

    val possibleResultPoints = ArrayList<ResultPoint>()

    fun getPossibleResultPoints(): List<ResultPoint> {
        return ArrayList(possibleResultPoints)
    }

    override fun foundPossibleResultPoint(point: ResultPoint) {
        possibleResultPoints.add(point)
    }

}