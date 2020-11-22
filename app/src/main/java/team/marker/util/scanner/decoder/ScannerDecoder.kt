package team.marker.util.scanner.decoder

import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import team.marker.util.scanner.ScannerMultiFormatReader
import team.marker.util.scanner.reader.ScannerReader
import java.util.*

open class ScannerDecoder (protected val reader: ScannerReader) : ResultPointCallback {

    val possibleResultPoints = ArrayList<ResultPoint>()

    fun decode(source: LuminanceSource): Result? {
        return decode(toBitmap(source))
    }

    fun decodeMultiple(source: LuminanceSource): Array<Result?>? {
        return decodeMultiple(toBitmap(source))
    }

    protected open fun toBitmap(source: LuminanceSource): BinaryBitmap {
        return BinaryBitmap(HybridBinarizer(source))
    }

    private fun decode(bitmap: BinaryBitmap): Result? {
        possibleResultPoints.clear()
        return try { if (reader is ScannerMultiFormatReader) reader.decodeWithState(bitmap) else reader.decode(bitmap) }
        catch (e: Exception) { null }
        finally { reader.reset() }
    }

    private fun decodeMultiple(bitmap: BinaryBitmap): Array<Result?>? {
        possibleResultPoints.clear()
        return try { if (reader is ScannerMultiFormatReader) reader.decodeWithStateMultiple(bitmap) else reader.decodeMultiple(bitmap) }
        catch (e: Exception) { null }
        finally { reader.reset() }
    }

    fun getPossibleResultPoints(): List<ResultPoint> {
        return ArrayList(possibleResultPoints)
    }

    override fun foundPossibleResultPoint(point: ResultPoint) {
        possibleResultPoints.add(point)
    }

}