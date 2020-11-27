package team.marker.util.scanner.decoder

import com.google.zxing.common.BitMatrix
import team.marker.util.scanner.common.ScannerFormatException

/**
 * @author Sean Owen
 */
internal class BitMatrixParser(bitMatrix: BitMatrix) {
    private val bitMatrix: BitMatrix
    private var parsedVersion: Version? = null
    private var parsedFormatInfo: FormatInformation? = null
    private var mirror = false

    @Throws(ScannerFormatException::class)
    fun readFormatInformation(): FormatInformation {
        if (parsedFormatInfo != null) {
            return parsedFormatInfo as FormatInformation
        }

        // Read top-left format info bits
        var formatInfoBits1 = 0
        for (i in 0..5) {
            formatInfoBits1 = copyBit(i, 8, formatInfoBits1)
        }
        // .. and skip a bit in the timing pattern ...
        formatInfoBits1 = copyBit(7, 8, formatInfoBits1)
        formatInfoBits1 = copyBit(8, 8, formatInfoBits1)
        formatInfoBits1 = copyBit(8, 7, formatInfoBits1)
        // .. and skip a bit in the timing pattern ...
        for (j in 5 downTo 0) {
            formatInfoBits1 = copyBit(8, j, formatInfoBits1)
        }

        // Read the top-right/bottom-left pattern too
        val dimension = bitMatrix.height
        var formatInfoBits2 = 0
        val jMin = dimension - 7
        for (j in dimension - 1 downTo jMin) {
            formatInfoBits2 = copyBit(8, j, formatInfoBits2)
        }
        for (i in dimension - 8 until dimension) {
            formatInfoBits2 = copyBit(i, 8, formatInfoBits2)
        }
        parsedFormatInfo =
            FormatInformation.decodeFormatInformation(formatInfoBits1, formatInfoBits2)
        if (parsedFormatInfo != null) {
            return parsedFormatInfo as FormatInformation
        }
        throw ScannerFormatException.formatInstance
    }

    @Throws(ScannerFormatException::class)
    fun readVersion(): Version {
        if (parsedVersion != null) {
            return parsedVersion as Version
        }
        val dimension = bitMatrix.height
        val provisionalVersion = (dimension - 17) / 4
        if (provisionalVersion <= 6) {
            return Version.getVersionForNumber(provisionalVersion)
        }

        // Read top-right version info: 3 wide by 6 tall
        var versionBits = 0
        val ijMin = dimension - 11
        for (j in 5 downTo 0) {
            for (i in dimension - 9 downTo ijMin) {
                versionBits = copyBit(i, j, versionBits)
            }
        }
        var theParsedVersion = Version.decodeVersionInformation(versionBits)
        if (theParsedVersion != null && theParsedVersion.dimensionForVersion == dimension) {
            parsedVersion = theParsedVersion
            return theParsedVersion
        }

        // Hmm, failed. Try bottom left: 6 wide by 3 tall
        versionBits = 0
        for (i in 5 downTo 0) {
            for (j in dimension - 9 downTo ijMin) {
                versionBits = copyBit(i, j, versionBits)
            }
        }
        theParsedVersion = Version.decodeVersionInformation(versionBits)
        if (theParsedVersion != null && theParsedVersion.dimensionForVersion == dimension) {
            parsedVersion = theParsedVersion
            return theParsedVersion
        }
        throw ScannerFormatException.formatInstance
    }

    private fun copyBit(i: Int, j: Int, versionBits: Int): Int {
        val bit = if (mirror) bitMatrix[j, i] else bitMatrix[i, j]
        return if (bit) versionBits shl 1 or 0x1 else versionBits shl 1
    }

    @Throws(ScannerFormatException::class)
    fun readCodewords(): ByteArray {
        val formatInfo = readFormatInformation()
        val version = readVersion()

        // Get the data mask for the format used in this QR Code. This will exclude
        // some bits from reading as we wind through the bit matrix.
        val dataMask = DataMask.values()[formatInfo.dataMask.toInt()]
        val dimension = bitMatrix.height
        dataMask.unmaskBitMatrix(bitMatrix, dimension)
        val functionPattern = version.buildFunctionPattern()
        var readingUp = true
        val result = ByteArray(version.totalCodewords)
        var resultOffset = 0
        var currentByte = 0
        var bitsRead = 0
        // Read columns in pairs, from right to left
        var j = dimension - 1
        while (j > 0) {
            if (j == 6) {
                // Skip whole column with vertical alignment pattern;
                // saves time and makes the other code proceed more cleanly
                j--
            }
            // Read alternatingly from bottom to top then top to bottom
            for (count in 0 until dimension) {
                val i = if (readingUp) dimension - 1 - count else count
                for (col in 0..1) {
                    // Ignore bits covered by the function pattern
                    if (!functionPattern[j - col, i]) {
                        // Read a bit
                        bitsRead++
                        currentByte = currentByte shl 1
                        if (bitMatrix[j - col, i]) {
                            currentByte = currentByte or 1
                        }
                        // If we've made a whole byte, save it off
                        if (bitsRead == 8) {
                            result[resultOffset++] = currentByte.toByte()
                            bitsRead = 0
                            currentByte = 0
                        }
                    }
                }
            }
            readingUp = readingUp xor true // readingUp = !readingUp; // switch directions
            j -= 2
        }
        if (resultOffset != version.totalCodewords) {
            throw ScannerFormatException.formatInstance
        }
        return result
    }

    /**
     * Revert the mask removal done while reading the code words. The bit matrix should revert to its original state.
     */
    fun remask() {
        if (parsedFormatInfo == null) {
            return  // We have no format information, and have no data mask
        }
        val dataMask = DataMask.values()[parsedFormatInfo!!.dataMask.toInt()]
        val dimension = bitMatrix.height
        dataMask.unmaskBitMatrix(bitMatrix, dimension)
    }

    /**
     * Prepare the parser for a mirrored operation.
     * This flag has effect only on the [.readFormatInformation] and the
     * [.readVersion]. Before proceeding with [.readCodewords] the
     * [.mirror] method should be called.
     *
     * @param mirror Whether to read version and format information mirrored.
     */
    fun setMirror(mirror: Boolean) {
        parsedVersion = null
        parsedFormatInfo = null
        this.mirror = mirror
    }

    /** Mirror the bit matrix in order to attempt a second reading.  */
    fun mirror() {
        for (x in 0 until bitMatrix.width) {
            for (y in x + 1 until bitMatrix.height) {
                if (bitMatrix[x, y] != bitMatrix[y, x]) {
                    bitMatrix.flip(y, x)
                    bitMatrix.flip(x, y)
                }
            }
        }
    }

    init {
        val dimension = bitMatrix.height
        if (dimension < 21 || dimension and 0x03 != 1) {
            throw ScannerFormatException.formatInstance
        }
        this.bitMatrix = bitMatrix
    }
}