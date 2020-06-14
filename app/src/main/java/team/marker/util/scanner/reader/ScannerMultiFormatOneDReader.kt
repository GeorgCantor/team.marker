package team.marker.util.scanner.reader

import com.google.zxing.*
import com.google.zxing.common.BitArray
import com.google.zxing.oned.*
import com.google.zxing.oned.rss.RSS14Reader
import com.google.zxing.oned.rss.expanded.RSSExpandedReader
import java.util.*

class ScannerMultiFormatOneDReader(hints: MutableMap<DecodeHintType, Any?>?) : OneDReader() {
    private val readers: Array<OneDReader>

    @Throws(NotFoundException::class)
    override fun decodeRow(rowNumber: Int, row: BitArray, hints: Map<DecodeHintType?, *>?): Result {
        for (reader in readers) {
            try {
                return reader.decodeRow(rowNumber, row, hints)
            } catch (re: ReaderException) {
                // continue
            }
        }
        throw NotFoundException.getNotFoundInstance()
    }

    override fun reset() {
        for (reader in readers) {
            reader.reset()
        }
    }

    init {
        val possibleFormats: Collection<BarcodeFormat?>? =
            if (hints == null) null else hints[DecodeHintType.POSSIBLE_FORMATS] as Collection<BarcodeFormat?>?
        val useCode39CheckDigit = hints != null &&
                hints[DecodeHintType.ASSUME_CODE_39_CHECK_DIGIT] != null
        val readers: MutableCollection<OneDReader> =
            ArrayList()
        if (possibleFormats != null) {
            if (possibleFormats.contains(BarcodeFormat.EAN_13) ||
                possibleFormats.contains(BarcodeFormat.UPC_A) ||
                possibleFormats.contains(BarcodeFormat.EAN_8) ||
                possibleFormats.contains(BarcodeFormat.UPC_E)
            ) {
                readers.add(MultiFormatUPCEANReader(hints))
            }
            if (possibleFormats.contains(BarcodeFormat.CODE_39)) {
                readers.add(Code39Reader(useCode39CheckDigit))
            }
            if (possibleFormats.contains(BarcodeFormat.CODE_93)) {
                readers.add(Code93Reader())
            }
            if (possibleFormats.contains(BarcodeFormat.CODE_128)) {
                readers.add(Code128Reader())
            }
            if (possibleFormats.contains(BarcodeFormat.ITF)) {
                readers.add(ITFReader())
            }
            if (possibleFormats.contains(BarcodeFormat.CODABAR)) {
                readers.add(CodaBarReader())
            }
            if (possibleFormats.contains(BarcodeFormat.RSS_14)) {
                readers.add(RSS14Reader())
            }
            if (possibleFormats.contains(BarcodeFormat.RSS_EXPANDED)) {
                readers.add(RSSExpandedReader())
            }
        }
        if (readers.isEmpty()) {
            readers.add(MultiFormatUPCEANReader(hints))
            readers.add(Code39Reader())
            readers.add(CodaBarReader())
            readers.add(Code93Reader())
            readers.add(Code128Reader())
            readers.add(ITFReader())
            readers.add(RSS14Reader())
            readers.add(RSSExpandedReader())
        }
        this.readers = readers.toTypedArray()
    }
}