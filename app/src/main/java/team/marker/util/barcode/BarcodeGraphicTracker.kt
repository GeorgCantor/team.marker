package team.marker.util.barcode

import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.barcode.Barcode
import team.marker.model.requests.PickProduct
import team.marker.util.camera.GraphicOverlay
import team.marker.view.pick.complete.PickCompleteViewModel
import java.util.*

/**
 * Generic tracker which is used for tracking or reading a barcode (and can really be used for
 * any type of item).  This is used to receive newly detected items, add a graphical representation
 * to an overlay, update the graphics as the item changes, and remove the graphics when the item
 * goes away.
 */
class BarcodeGraphicTracker internal constructor(
    private val mOverlay: GraphicOverlay<BarcodeGraphic?>?,
    private val mGraphic: BarcodeGraphic,
    private val viewModel: PickCompleteViewModel?
) : Tracker<Barcode?>() {

    private var lastTime: Date? = null
    private var lastProductId = 0

    /**
     * Start tracking the detected item instance within the item overlay.
     */
    override fun onNewItem(id: Int, item: Barcode?) {
        mGraphic.id = id
    }

    /**
     * Update the position/characteristics of the item within the overlay.
     */
    override fun onUpdate(detectionResults: Detections<Barcode?>, item: Barcode?) {
        mOverlay?.add(mGraphic)
        mGraphic.updateItem(item)
        val seconds: Long = if (lastTime != null) (Date().time - lastTime!!.time) / 800 else 300
        if (seconds < 5) return
        lastTime = Date()
        val productId = item?.rawValue?.takeLastWhile { it.isDigit() }?.toIntOrNull() ?: return
        if (productId == lastProductId) return
        viewModel?.addProduct(PickProduct(productId, 1.toDouble(), 0))
    }

    /**
     * Hide the graphic when the corresponding object was not detected.  This can happen for
     * intermediate frames temporarily, for example if the object was momentarily blocked from
     * view.
     */
    override fun onMissing(detectionResults: Detections<Barcode?>) {
        mOverlay?.remove(mGraphic)
        viewModel?.clearVisibility()
    }

    /**
     * Called when the item is assumed to be gone for good. Remove the graphic annotation from
     * the overlay.
     */
    override fun onDone() {
        mOverlay?.remove(mGraphic)
    }
}