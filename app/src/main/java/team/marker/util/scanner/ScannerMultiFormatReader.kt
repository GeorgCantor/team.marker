package team.marker.util.scanner

import android.util.Log
import com.google.zxing.BinaryBitmap
import team.marker.util.scanner.common.ScannerNotFoundException
import team.marker.util.scanner.common.ScannerReaderException
import team.marker.util.scanner.common.ScannerResult
import team.marker.util.scanner.decoder.ScannerDecodeHintType
import team.marker.util.scanner.reader.ScannerQRCodeMultiReader
import team.marker.util.scanner.reader.ScannerReader
import java.util.*

class ScannerMultiFormatReader : ScannerReader {

    private var hints: MutableMap<ScannerDecodeHintType?, Any?>? = null
    private var readers: Array<ScannerReader>? = null

    @Throws(ScannerNotFoundException::class)
    override fun decode(image: BinaryBitmap): ScannerResult {
        setHints(null)
        return decodeInternal(image)
    }

    @Throws(ScannerNotFoundException::class)
    override fun decodeMultiple(image: BinaryBitmap): Array<ScannerResult?> {
        setHints(null)
        return decodeInternalMultiple(image)
    }

    @Throws(ScannerNotFoundException::class)
    override fun decode(image: BinaryBitmap, hints: MutableMap<ScannerDecodeHintType?, Any?>?): ScannerResult {
        setHints(hints)
        return decodeInternal(image)
    }

    override fun decodeMultiple(image: BinaryBitmap, hints: MutableMap<ScannerDecodeHintType?, Any?>?): Array<ScannerResult?> {
        setHints(hints)
        return decodeInternalMultiple(image)
    }

    @Throws(ScannerNotFoundException::class)
    fun decodeWithState(image: BinaryBitmap): ScannerResult {
        if (readers == null) setHints(null)
        return decodeInternal(image)
    }

    @Throws(ScannerNotFoundException::class)
    fun decodeWithStateMultiple(image: BinaryBitmap): Array<ScannerResult?> {
        if (readers == null) setHints(null)
        return decodeInternalMultiple(image)
    }

    fun setHints(hints: MutableMap<ScannerDecodeHintType?, Any?>?) {
        this.hints = hints
        val readers: MutableCollection<ScannerReader> = ArrayList()

        if (readers.isEmpty()) {
            readers.add(ScannerQRCodeMultiReader())
        }
        this.readers = readers.toTypedArray()
    }

    override fun reset() {
        if (readers != null) {
            for (reader in readers!!) reader.reset()
        }
    }

    @Throws(ScannerNotFoundException::class)
    private fun decodeInternal(image: BinaryBitmap): ScannerResult {
        if (readers != null) {
            for (reader in readers!!) {
                try {
                    return reader.decode(image, hints)
                } catch (re: ScannerReaderException) { }
            }
        }
        throw ScannerNotFoundException().getNotFoundInstance()!!
    }

    @Throws(ScannerNotFoundException::class)
    private fun decodeInternalMultiple(image: BinaryBitmap): Array<ScannerResult?> {
        if (readers != null) {
            for (reader in readers!!) {
                try {
                    val res = reader.decodeMultiple(image, hints)
                    if (res.size >= 1) Log.e("Res M1", res[0].toString())
                    if (res.size >= 2) Log.e("Res M2", res[1].toString())
                    if (res.size >= 3) Log.e("Res M3", res[2].toString())
                    if (res.size >= 4) Log.e("Res M4", res[3].toString())
                    return res
                } catch (re: ScannerReaderException) { }
            }
        }
        throw ScannerNotFoundException().getNotFoundInstance()!!
    }
}