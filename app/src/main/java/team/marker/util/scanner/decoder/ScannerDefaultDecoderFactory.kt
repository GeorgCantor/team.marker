package team.marker.util.scanner.decoder

import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.journeyapps.barcodescanner.Decoder
import team.marker.util.scanner.ScannerMultiFormatReader
import team.marker.util.scanner.decoder.ScannerDecoder
import team.marker.util.scanner.decoder.ScannerDecoderFactory
import team.marker.util.scanner.decoder.ScannerInvertedDecoder
import java.util.*

class ScannerDefaultDecoderFactory : ScannerDecoderFactory {
    private var decodeFormats: Collection<BarcodeFormat>? = null
    private var hints: Map<DecodeHintType, *>? = null
    private var characterSet: String? = null
    private var inverted = false

    constructor()
    constructor(decodeFormats: Collection<BarcodeFormat>?, hints: Map<DecodeHintType, *>?, characterSet: String?, inverted: Boolean) {
        this.decodeFormats = decodeFormats
        this.hints = hints
        this.characterSet = characterSet
        this.inverted = inverted
    }

    override fun createDecoder(baseHints: MutableMap<DecodeHintType, Any?>?): ScannerDecoder {
        val hints: MutableMap<DecodeHintType, Any?> = EnumMap(DecodeHintType::class.java)
        hints.putAll(baseHints!!)
        if (this.hints != null) hints.putAll(this.hints!!)
        if (decodeFormats != null) hints[DecodeHintType.POSSIBLE_FORMATS] = decodeFormats
        if (characterSet != null) hints[DecodeHintType.CHARACTER_SET] = characterSet
        val reader = ScannerMultiFormatReader()
        reader.setHints(hints)
        return if (inverted) ScannerInvertedDecoder(reader) else ScannerDecoder(reader)
    }
}