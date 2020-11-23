package team.marker.util.scanner.reader

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.ResultMetadataType
import team.marker.util.scanner.common.ScannerNotFoundException
import team.marker.util.scanner.common.ScannerReaderException
import team.marker.util.scanner.common.ScannerResult
import team.marker.util.scanner.common.ScannerResultPoint
import team.marker.util.scanner.decoder.ScannerQRCodeDecoderMetaData
import team.marker.util.scanner.detector.ScannerMultiDetector
import team.marker.util.scanner.multi.ScannerMultipleBarcodeReader
import java.io.Serializable
import java.util.*

class ScannerQRCodeMultiReader : ScannerQRCodeReader(), ScannerMultipleBarcodeReader {

    @Throws(ScannerNotFoundException::class)
    override fun decodeMultiple(image: BinaryBitmap): Array<ScannerResult?> {
        return decodeMultiple(image, null)
    }

    @Throws(ScannerNotFoundException::class)
    override fun decodeMultiple(image: BinaryBitmap, hints: MutableMap<DecodeHintType, Any?>?): Array<ScannerResult?> {
        var results: MutableList<ScannerResult?> = ArrayList()
        val detectorResults = ScannerMultiDetector(image.blackMatrix).detectMulti(hints)
        for (detectorResult in detectorResults) {
            try {
                val decoderResult = decoder.decode(detectorResult?.bits, hints)
                val points = detectorResult?.points
                // If the code was mirrored: swap the bottom-left and the top-right points.
                if (decoderResult.other is ScannerQRCodeDecoderMetaData) {
                    (decoderResult.other as ScannerQRCodeDecoderMetaData).applyMirroredCorrection(points)
                }
                val result = ScannerResult(decoderResult.text, decoderResult.rawBytes, points, BarcodeFormat.QR_CODE)
                val byteSegments = decoderResult.byteSegments
                if (byteSegments != null) result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, byteSegments)
                val ecLevel = decoderResult.ecLevel
                if (ecLevel != null) result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, ecLevel)
                if (decoderResult.hasStructuredAppend()) {
                    result.putMetadata(ResultMetadataType.STRUCTURED_APPEND_SEQUENCE, decoderResult.structuredAppendSequenceNumber)
                    result.putMetadata(ResultMetadataType.STRUCTURED_APPEND_PARITY, decoderResult.structuredAppendParity)
                }
                results.add(result)
            } catch (re: ScannerReaderException) { }
        }
        return if (results.isEmpty()) {
            EMPTY_RESULT_ARRAY
        } else {
            results = processStructuredAppend(results).toMutableList()
            results.toTypedArray()
        }
    }

    private class SAComparator : Comparator<ScannerResult>, Serializable {
        override fun compare(a: ScannerResult, b: ScannerResult): Int {
            val aNumber = a.resultMetadata!![ResultMetadataType.STRUCTURED_APPEND_SEQUENCE] as Int
            val bNumber = b.resultMetadata!![ResultMetadataType.STRUCTURED_APPEND_SEQUENCE] as Int
            return aNumber.compareTo(bNumber)
        }
    }

    companion object {
        private val EMPTY_RESULT_ARRAY = arrayOfNulls<ScannerResult>(0)
        private val NO_POINTS = arrayOfNulls<ScannerResultPoint>(0)
        private fun processStructuredAppend(results: List<ScannerResult?>): List<ScannerResult?> {
            var hasSA = false
            // first, check, if there is at least on SA result in the list
            for (result in results) {
                if (result!!.resultMetadata!!.containsKey(ResultMetadataType.STRUCTURED_APPEND_SEQUENCE)) {
                    hasSA = true
                    break
                }
            }
            if (!hasSA) return results
            // it is, second, split the lists and built a new result list
            val newResults: MutableList<ScannerResult> = ArrayList()
            val saResults: MutableList<ScannerResult> = ArrayList()
            for (result in results) {
                newResults.add(result!!)
                if (result.resultMetadata!!.containsKey(ResultMetadataType.STRUCTURED_APPEND_SEQUENCE)) saResults.add(result)
            }
            // sort and concatenate the SA list items
            Collections.sort(saResults, SAComparator())
            val concatedText = StringBuilder()
            var rawBytesLen = 0
            var byteSegmentLength = 0
            for (saResult in saResults) {
                concatedText.append(saResult.text)
                rawBytesLen += saResult.rawBytes!!.size
                if (saResult.resultMetadata!!.containsKey(ResultMetadataType.BYTE_SEGMENTS)) {
                    val byteSegments = saResult.resultMetadata?.get(ResultMetadataType.BYTE_SEGMENTS) as Iterable<ByteArray>?
                    for (segment in byteSegments!!) {
                        byteSegmentLength += segment.size
                    }
                }
            }
            val newRawBytes = ByteArray(rawBytesLen)
            val newByteSegment = ByteArray(byteSegmentLength)
            var newRawBytesIndex = 0
            var byteSegmentIndex = 0
            for (saResult in saResults) {
                System.arraycopy(saResult.rawBytes!!, 0, newRawBytes, newRawBytesIndex, saResult.rawBytes.size)
                newRawBytesIndex += saResult.rawBytes.size
                if (saResult.resultMetadata!!.containsKey(ResultMetadataType.BYTE_SEGMENTS)) {
                    val byteSegments = saResult.resultMetadata?.get(ResultMetadataType.BYTE_SEGMENTS) as Iterable<ByteArray>?
                    for (segment in byteSegments!!) {
                        System.arraycopy(segment, 0, newByteSegment, byteSegmentIndex, segment.size)
                        byteSegmentIndex += segment.size
                    }
                }
            }
            val newResult = ScannerResult(concatedText.toString(), newRawBytes,
                NO_POINTS, BarcodeFormat.QR_CODE)
            if (byteSegmentLength > 0) {
                val byteSegmentList: MutableCollection<ByteArray> = ArrayList()
                byteSegmentList.add(newByteSegment)
                newResult.putMetadata(ResultMetadataType.BYTE_SEGMENTS, byteSegmentList)
            }
            newResults.add(newResult)
            return newResults
        }
    }
}