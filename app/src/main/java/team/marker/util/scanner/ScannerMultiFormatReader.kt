package team.marker.util.scanner

import android.util.Log
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.NotFoundException
import team.marker.util.scanner.common.ScannerReaderException
import team.marker.util.scanner.common.ScannerResult
import team.marker.util.scanner.reader.ScannerQRCodeMultiReader
import team.marker.util.scanner.reader.ScannerReader
import java.util.*

class ScannerMultiFormatReader : ScannerReader {

    private var hints: MutableMap<DecodeHintType, Any?>? = null
    private var readers: Array<ScannerReader>? = null

    @Throws(NotFoundException::class)
    override fun decode(image: BinaryBitmap): ScannerResult {
        setHints(null)
        return decodeInternal(image)
    }

    @Throws(NotFoundException::class)
    override fun decodeMultiple(image: BinaryBitmap): Array<ScannerResult?> {
        setHints(null)
        return decodeInternalMultiple(image)
    }

    @Throws(NotFoundException::class)
    override fun decode(image: BinaryBitmap, hints: MutableMap<DecodeHintType, Any?>?): ScannerResult {
        setHints(hints)
        return decodeInternal(image)
    }

    override fun decodeMultiple(image: BinaryBitmap, hints: MutableMap<DecodeHintType, Any?>?): Array<ScannerResult?> {
        setHints(hints)
        return decodeInternalMultiple(image)
    }

    @Throws(NotFoundException::class)
    fun decodeWithState(image: BinaryBitmap): ScannerResult {
        if (readers == null) setHints(null)
        return decodeInternal(image)
    }

    @Throws(NotFoundException::class)
    fun decodeWithStateMultiple(image: BinaryBitmap): Array<ScannerResult?> {
        if (readers == null) setHints(null)
        return decodeInternalMultiple(image)
    }

    fun setHints(hints: MutableMap<DecodeHintType, Any?>?) {
        this.hints = hints
        /*val tryHarder = hints != null && hints.containsKey(DecodeHintType.TRY_HARDER)
        val formats: Collection<BarcodeFormat>? = if (hints == null) null else hints[DecodeHintType.POSSIBLE_FORMATS] as Collection<BarcodeFormat>?*/
        val readers: MutableCollection<ScannerReader> = ArrayList()
        /*if (formats != null) {
            val addOneDReader = formats.contains(BarcodeFormat.UPC_A) ||
                    formats.contains(BarcodeFormat.UPC_E) ||
                    formats.contains(BarcodeFormat.EAN_13) ||
                    formats.contains(BarcodeFormat.EAN_8) ||
                    formats.contains(BarcodeFormat.CODABAR) ||
                    formats.contains(BarcodeFormat.CODE_39) ||
                    formats.contains(BarcodeFormat.CODE_93) ||
                    formats.contains(BarcodeFormat.CODE_128) ||
                    formats.contains(BarcodeFormat.ITF) ||
                    formats.contains(BarcodeFormat.RSS_14) ||
                    formats.contains(BarcodeFormat.RSS_EXPANDED)
            // Put 1D readers upfront in "normal" mode
            if (addOneDReader && !tryHarder) readers.add(ScannerMultiFormatOneDReader(hints))
            //if (formats.contains(BarcodeFormat.QR_CODE)) readers.add(QRCodeReader())
            if (formats.contains(BarcodeFormat.QR_CODE)) readers.add(ScannerQRCodeMultiReader())
            if (formats.contains(BarcodeFormat.DATA_MATRIX)) readers.add(DataMatrixReader())
            if (formats.contains(BarcodeFormat.AZTEC)) readers.add(AztecReader())
            if (formats.contains(BarcodeFormat.PDF_417)) readers.add(PDF417Reader())
            if (formats.contains(BarcodeFormat.MAXICODE)) readers.add(MaxiCodeReader())
            // At end in "try harder" mode
            if (addOneDReader && tryHarder) readers.add(MultiFormatOneDReader(hints))
        }*/
        if (readers.isEmpty()) {
            //if (!tryHarder) readers.add(ScannerMultiFormatOneDReader(hints))
            //readers.add(QRCodeReader())
            readers.add(ScannerQRCodeMultiReader())
            //readers.add(DataMatrixReader())
            //readers.add(AztecReader())
            //readers.add(PDF417Reader())
            //readers.add(MaxiCodeReader())
            //if (tryHarder) readers.add(MultiFormatOneDReader(hints))
        }
        this.readers = readers.toTypedArray()
    }

    override fun reset() {
        if (readers != null) {
            for (reader in readers!!) reader.reset()
        }
    }

    @Throws(NotFoundException::class)
    private fun decodeInternal(image: BinaryBitmap): ScannerResult {
        if (readers != null) {
            for (reader in readers!!) {
                try {
                    return reader.decode(image, hints)
                } catch (re: ScannerReaderException) { }
            }
        }
        throw NotFoundException.getNotFoundInstance()
    }

    @Throws(NotFoundException::class)
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
        throw NotFoundException.getNotFoundInstance()
    }
}