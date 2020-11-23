package team.marker.util.scanner.decoder

import com.google.zxing.DecodeHintType

interface ScannerDecoderFactory {
    fun createDecoder(baseHints: MutableMap<DecodeHintType, Any?>?): ScannerDecoder
}