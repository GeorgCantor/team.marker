package team.marker.util.scanner.decoder

import team.marker.util.scanner.ScannerMultiFormatReader
import team.marker.util.scanner.common.ScannerBarcodeFormat
import java.util.*

class ScannerDefaultDecoderFactory : ScannerDecoderFactory {
    private var decodeFormats: Collection<ScannerBarcodeFormat>? = null
    private var hints: Map<ScannerDecodeHintType, *>? = null
    private var characterSet: String? = null
    private var inverted = false

    constructor()
    constructor(decodeFormats: Collection<ScannerBarcodeFormat>?, hints: Map<ScannerDecodeHintType, *>?, characterSet: String?, inverted: Boolean) {
        this.decodeFormats = decodeFormats
        this.hints = hints
        this.characterSet = characterSet
        this.inverted = inverted
    }

    override fun createDecoder(baseHints: MutableMap<ScannerDecodeHintType?, Any?>?): ScannerDecoder {
        val hints: MutableMap<ScannerDecodeHintType?, Any?> = EnumMap(ScannerDecodeHintType::class.java)
        if (baseHints != null) {
            hints.putAll(baseHints)
        }
        if (this.hints != null) hints.putAll(this.hints!!)
        if (decodeFormats != null) hints[ScannerDecodeHintType.POSSIBLE_FORMATS] = decodeFormats
        if (characterSet != null) hints[ScannerDecodeHintType.CHARACTER_SET] = characterSet
        val reader = ScannerMultiFormatReader()
        reader.setHints(hints)
        return if (inverted) ScannerInvertedDecoder(reader) else ScannerDecoder(reader)
    }
}