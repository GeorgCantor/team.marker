package team.marker.util.scanner.detector

import com.google.zxing.common.BitMatrix
import team.marker.util.scanner.common.ScannerResultPoint

open class ScannerDetectorResult(val bits: BitMatrix, val points: Array<ScannerResultPoint?>?)