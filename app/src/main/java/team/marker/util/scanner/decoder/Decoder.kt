package team.marker.util.scanner.decoder

import com.google.zxing.common.BitMatrix
import com.google.zxing.common.reedsolomon.GenericGF
import com.google.zxing.common.reedsolomon.ReedSolomonDecoder
import com.google.zxing.common.reedsolomon.ReedSolomonException
import okhttp3.internal.and
import team.marker.util.scanner.common.ScannerChecksumException
import team.marker.util.scanner.common.ScannerFormatException

/**
 *
 * The main class which implements QR Code decoding -- as opposed to locating and extracting
 * the QR Code from an image.
 *
 * @author Sean Owen
 */
class Decoder {
    private val rsDecoder: ReedSolomonDecoder

    @JvmOverloads
    @Throws(ScannerChecksumException::class, ScannerFormatException::class)
    fun decode(
        image: Array<BooleanArray?>?,
        hints: Map<ScannerDecodeHintType?, Any?>?
    ): DecoderResult {
        return decode(BitMatrix.parse(image), hints)
    }

    @JvmOverloads
    @Throws(ScannerFormatException::class, ScannerChecksumException::class)
    fun decode(bits: BitMatrix?, hints: Map<ScannerDecodeHintType?, Any?>?): DecoderResult {

        // Construct a parser and read version, error-correction level
        val parser = BitMatrixParser(bits!!)
        var fe: ScannerFormatException? = null
        var ce: ScannerChecksumException? = null
        try {
            return decode(parser, hints!!)
        } catch (e: ScannerFormatException) {
            fe = e
        } catch (e: ScannerChecksumException) {
            ce = e
        }
        return try {

            // Revert the bit matrix
            parser.remask()

            // Will be attempting a mirrored reading of the version and format info.
            parser.setMirror(true)

            // Preemptively read the version.
            parser.readVersion()

            // Preemptively read the format information.
            parser.readFormatInformation()

            /*
            * Since we're here, this means we have successfully detected some kind
            * of version and format information when mirrored. This is a good sign,
            * that the QR code may be mirrored, and we should try once more with a
            * mirrored content.
            */
            // Prepare for a mirrored reading.
            parser.mirror()
            val result = decode(parser, hints!!)

            // Success! Notify the caller that the code was mirrored.
            result.other = QRCodeDecoderMetaData(true)
            result
        } catch (e: ScannerFormatException) {
            // Throw the exception from the original reading
            if (fe != null) {
                throw fe
            }
            throw ce!! // If fe is null, this can't be
        } catch (e: ScannerChecksumException) {
            if (fe != null) {
                throw fe
            }
            throw ce!!
        }
    }

    @Throws(ScannerFormatException::class, ScannerChecksumException::class)
    private fun decode(parser: BitMatrixParser, hints: Map<ScannerDecodeHintType?, Any?>?): DecoderResult {
        val version = parser.readVersion()
        val ecLevel = parser.readFormatInformation().errorCorrectionLevel

        // Read codewords
        val codewords = parser.readCodewords()
        // Separate into data blocks
        val dataBlocks = DataBlock.getDataBlocks(codewords, version, ecLevel)

        // Count total number of data bytes
        var totalBytes = 0
        for (dataBlock in dataBlocks) {
            totalBytes += dataBlock!!.numDataCodewords
        }
        val resultBytes = ByteArray(totalBytes)
        var resultOffset = 0

        // Error-correct and copy data blocks together into a stream of bytes
        for (dataBlock in dataBlocks) {
            val codewordBytes = dataBlock!!.codewords
            val numDataCodewords = dataBlock.numDataCodewords
            correctErrors(codewordBytes, numDataCodewords)
            for (i in 0 until numDataCodewords) {
                resultBytes[resultOffset++] = codewordBytes[i]
            }
        }

        // Decode the contents of that stream of bytes
        return DecodedBitStreamParser.decode(resultBytes, version, ecLevel, hints)
    }

    @Throws(ScannerChecksumException::class)
    private fun correctErrors(codewordBytes: ByteArray, numDataCodewords: Int) {
        val numCodewords = codewordBytes.size
        // First read into an array of ints
        val codewordsInts = IntArray(numCodewords)
        for (i in 0 until numCodewords) {
            codewordsInts[i] = codewordBytes[i] and 0xFF
        }
        try {
            rsDecoder.decode(codewordsInts, codewordBytes.size - numDataCodewords)
        } catch (ignored: ReedSolomonException) {
            throw ScannerChecksumException.checksumInstance
        }
        // Copy back into array of bytes -- only need to worry about the bytes that were data
        // We don't care about errors in the error-correction codewords
        for (i in 0 until numDataCodewords) {
            codewordBytes[i] = codewordsInts[i].toByte()
        }
    }

    init {
        rsDecoder = ReedSolomonDecoder(GenericGF.QR_CODE_FIELD_256)
    }
}
