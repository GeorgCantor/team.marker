package team.marker.util.scanner.detector

import com.google.zxing.DecodeHintType
import com.google.zxing.NotFoundException
import com.google.zxing.ReaderException
import com.google.zxing.ResultPointCallback
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.DetectorResult
import com.google.zxing.qrcode.detector.Detector
import java.util.*

class ScannerMultiDetector(image: BitMatrix?) :
    Detector(image) {
    @Throws(NotFoundException::class)
    fun detectMulti(hints: MutableMap<DecodeHintType, Any?>?): Array<DetectorResult?> {
        val image = image
        val resultPointCallback =
            if (hints == null) null else hints[DecodeHintType.NEED_RESULT_POINT_CALLBACK] as ResultPointCallback?
        val finder = ScannerMultiFinderPatternFinder(image, resultPointCallback)
        val infos = finder.findMulti(hints)
        if (infos.isEmpty()) throw NotFoundException.getNotFoundInstance()
        val result: MutableList<DetectorResult> = ArrayList()
        for (info in infos) {
            try {
                result.add(processFinderPatternInfo(info))
            } catch (e: ReaderException) {
                // ignore
            }
        }
        return if (result.isEmpty()) EMPTY_DETECTOR_RESULTS else result.toTypedArray()
    }

    companion object {
        private val EMPTY_DETECTOR_RESULTS = arrayOfNulls<DetectorResult>(0)
    }
}