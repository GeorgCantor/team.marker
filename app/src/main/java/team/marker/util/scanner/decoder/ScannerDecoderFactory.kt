package team.marker.util.scanner.decoder

import com.google.zxing.DecodeHintType
import com.journeyapps.barcodescanner.Decoder
import team.marker.util.scanner.decoder.ScannerDecoder

interface ScannerDecoderFactory {
    fun createDecoder(baseHints: MutableMap<DecodeHintType, Any?>?): ScannerDecoder
}