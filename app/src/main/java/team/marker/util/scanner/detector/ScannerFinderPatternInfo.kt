package team.marker.util.scanner.detector

class ScannerFinderPatternInfo(patternCenters: Array<ScannerFinderPattern>) {
    val bottomLeft: ScannerFinderPattern
    val topLeft: ScannerFinderPattern
    val topRight: ScannerFinderPattern

    init {
        bottomLeft = patternCenters[0]
        topLeft = patternCenters[1]
        topRight = patternCenters[2]
    }
}