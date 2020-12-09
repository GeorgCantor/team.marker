package team.marker.view.pick

import android.content.Context
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.Barcode
import team.marker.view.pick.camera.GraphicOverlay
import team.marker.view.pick.complete.PickCompleteViewModel

/**
 * Factory for creating a tracker and associated graphic to be associated with a new barcode.  The
 * multi-processor uses this factory to create barcode trackers as needed -- one for each barcode.
 */
internal class BarcodeTrackerFactory(
    private val mGraphicOverlay: GraphicOverlay<BarcodeGraphic?>,
    private val mContext: Context,
    private val viewModel: PickCompleteViewModel
) : MultiProcessor.Factory<Barcode> {

    override fun create(barcode: Barcode): BarcodeGraphicTracker {
        val graphic = BarcodeGraphic(mGraphicOverlay)

        return BarcodeGraphicTracker(mGraphicOverlay, graphic, mContext, viewModel)
    }
}