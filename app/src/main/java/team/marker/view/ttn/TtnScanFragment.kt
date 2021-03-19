package team.marker.view.ttn

import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.hardware.Camera.Parameters.*
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.MultiDetector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.fragment_ttn_scan.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import team.marker.R
import team.marker.model.Dialog
import team.marker.model.Product
import team.marker.model.requests.PickProduct
import team.marker.util.Constants.RC_HANDLE_CAMERA_PERM
import team.marker.util.Constants.RC_HANDLE_GMS
import team.marker.util.barcode.BarcodeGraphic
import team.marker.util.barcode.BarcodeTrackerFactory
import team.marker.util.camera.CameraSource
import team.marker.util.camera.CameraSource.Companion.CAMERA_FACING_BACK
import team.marker.util.camera.GraphicOverlay
import team.marker.util.longToast
import team.marker.util.observeOnce
import team.marker.util.showDialog
import team.marker.util.showPermissionSnackbar
import team.marker.view.pick.complete.PickCompleteViewModel
import team.marker.view.ttn.cargo.CargoPlacesViewModel
import java.io.IOException

class TtnScanFragment : Fragment(R.layout.fragment_ttn_scan) {

    private val viewModel by inject<PickCompleteViewModel>()
    private val placesViewModel by sharedViewModel<CargoPlacesViewModel>()
    private var cameraSource: CameraSource? = null
    private var torchOn: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setOnTouchListener { _, event ->
            val x = event.x.toInt() + 200
            val y = event.y.toInt()
            if (event.action == MotionEvent.ACTION_DOWN) {
                viewModel.products.observeOnce(viewLifecycleOwner) { products ->
                    products.forEach {
                        if (it.rectName?.contains(x, y) == true && it.isVisible) {
                            context?.longToast(it.name)
//                            findNavController().navigate(
//                                R.id.action_pickFragment_to_productFragment,
//                                bundleOf(Constants.PRODUCT_ID to it.id.toString())
//                            )
                        }
                        if (it.rectButton?.contains(x, y) == true && it.isVisible) {
                            viewModel.setClickStatus(
                                Product(it.id, it.name, it.rectName, it.rectButton, if (it.clickStatus == 0) 1 else 0)
                            )
                        }
                    }
                }
            }
            true
        }

        val rc = ActivityCompat.checkSelfPermission(requireContext(), CAMERA)
        if (rc == PERMISSION_GRANTED) createCameraSource() else requestCameraPermission()

        btn_scan_flash.setOnClickListener {
            toggleTorch(torchOn)
            torchOn = !torchOn
            btn_scan_flash.setImageResource(if (torchOn) R.drawable.ic_flash_off else R.drawable.ic_flash)
        }

        btn_complete.setOnClickListener { goToComplete() }
    }

    private fun goToComplete() {
        if (viewModel.products.value == null) viewModel.products.value = mutableSetOf()
        viewModel.products.observeOnce(viewLifecycleOwner) {
            val list = mutableListOf<PickProduct>()
            it.forEach {
                if (it.clickStatus == 1) list.add(PickProduct(it.id, 1.toDouble(), 0))
            }

            val productIds = arrayListOf<String>()
            for (product in list) productIds.add(product.id.toString())
            val productIdsStr = productIds.joinToString(",")
            placesViewModel.productIds.value = productIdsStr

            findNavController().navigate(R.id.action_ttnScanFragment_to_cargoPlacesFragment)
        }
    }

    private fun requestCameraPermission() {
        val permissions = arrayOf(CAMERA)
        if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), CAMERA)) {
            requestPermissions(permissions, RC_HANDLE_CAMERA_PERM)
            return
        }
        val listener = View.OnClickListener {
            requestPermissions(permissions, RC_HANDLE_CAMERA_PERM)
        }
        preview.setOnClickListener(listener)
        graphic_overlay?.showPermissionSnackbar(listener)
    }

    @SuppressLint("InlinedApi")
    private fun createCameraSource() {
        val context = requireActivity().applicationContext
        val barcodeDetector = BarcodeDetector.Builder(context).build()
        val barcodeFactory = BarcodeTrackerFactory(
            graphic_overlay as GraphicOverlay<BarcodeGraphic?>,
            viewModel,
            viewLifecycleOwner
        )
        barcodeDetector.setProcessor(MultiProcessor.Builder(barcodeFactory).build())

        val multiDetector = MultiDetector.Builder()
            .add(barcodeDetector)
            .build()

        val builder = CameraSource.Builder(requireContext(), multiDetector)
            .setFacing(CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1600, 1024)
            .setRequestedFps(15.0F)
            .setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE)

        cameraSource = builder
            .setFlashMode(null)
            .build()
    }

    override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    override fun onPause() {
        super.onPause()
        if (preview != null) preview?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (preview != null) preview?.release()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
            createCameraSource()
            return
        }
        context?.showDialog(
            Dialog(getString(R.string.app_name), getString(R.string.permission_rationale), getString(R.string.ok))
            { requestPermissions(permissions, RC_HANDLE_CAMERA_PERM) }
        )
    }

    @Throws(SecurityException::class)
    private fun startCameraSource() {
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
            requireActivity().applicationContext
        )
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg?.show()
        }
        if (cameraSource != null) {
            try {
                preview?.start(cameraSource, graphic_overlay)
            } catch (e: IOException) {
                cameraSource?.release()
                cameraSource = null
            }
        }
    }

    private fun toggleTorch(torchOn: Boolean) {
        cameraSource?.setFlashMode(if (torchOn) FLASH_MODE_OFF else FLASH_MODE_TORCH)
    }
}