package team.marker.util.scanner.decoder

import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.common.HybridBinarizer
import team.marker.util.scanner.reader.ScannerReader

class ScannerInvertedDecoder(reader: ScannerReader) : ScannerDecoder(reader) {

    override fun toBitmap(source: LuminanceSource): BinaryBitmap {
        return BinaryBitmap(HybridBinarizer(source.invert()))
    }
}