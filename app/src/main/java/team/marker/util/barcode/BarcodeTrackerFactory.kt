package team.marker.util.barcode

import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.Barcode
import team.marker.util.camera.GraphicOverlay
import team.marker.view.pick.complete.PickCompleteViewModel

/**
 * Factory for creating a tracker and associated graphic to be associated with a new barcode.  The
 * multi-processor uses this factory to create barcode trackers as needed -- one for each barcode.
 */
internal class BarcodeTrackerFactory(
    private val graphicOverlay: GraphicOverlay<BarcodeGraphic?>?,
    private val viewModel: PickCompleteViewModel?,
    private val lifecycleOwner: LifecycleOwner
) : MultiProcessor.Factory<Barcode> {

    override fun create(barcode: Barcode): BarcodeGraphicTracker {
        val graphic = BarcodeGraphic(graphicOverlay, viewModel, lifecycleOwner)

        return BarcodeGraphicTracker(graphicOverlay, graphic, viewModel)
    }
}