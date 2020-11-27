package team.marker.util.scanner.detector

import com.google.zxing.NotFoundException
import com.google.zxing.ReaderException
import com.google.zxing.common.BitMatrix
import team.marker.util.scanner.common.ScannerResultPointCallback
import team.marker.util.scanner.decoder.ScannerDecodeHintType
import java.util.*

class ScannerMultiDetector(image: BitMatrix?) :
    ScannerDetector(image!!) {
    @Throws(NotFoundException::class)
    fun detectMulti(hints: MutableMap<ScannerDecodeHintType?, Any?>?): Array<ScannerDetectorResult?> {
        val image = image
        val resultPointCallback =
            if (hints == null) null else hints[ScannerDecodeHintType.NEED_RESULT_POINT_CALLBACK] as ScannerResultPointCallback?
        val finder = ScannerMultiFinderPatternFinder(image, resultPointCallback)
        val infos = finder.findMulti(hints)
        if (infos.isEmpty()) throw NotFoundException.getNotFoundInstance()
        val result: MutableList<ScannerDetectorResult> = ArrayList()
        for (info in infos) {
            try {
                result.add(processFinderPatternInfo(info!!))
            } catch (e: ReaderException) {
                // ignore
            }
        }
        return if (result.isEmpty()) EMPTY_DETECTOR_RESULTS else result.toTypedArray()
    }

    companion object {
        private val EMPTY_DETECTOR_RESULTS = arrayOfNulls<ScannerDetectorResult>(0)
    }
}