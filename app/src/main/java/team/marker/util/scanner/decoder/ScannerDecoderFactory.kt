package team.marker.util.scanner.decoder

interface ScannerDecoderFactory {
    fun createDecoder(baseHints: MutableMap<ScannerDecodeHintType?, Any?>?): ScannerDecoder
}