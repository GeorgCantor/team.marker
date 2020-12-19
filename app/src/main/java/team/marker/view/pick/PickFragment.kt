package team.marker.view.pick

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.Camera.Parameters.FLASH_MODE_OFF
import android.hardware.Camera.Parameters.FLASH_MODE_TORCH
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_pick.*
import org.koin.android.ext.android.inject
import team.marker.R
import team.marker.model.requests.PickProduct
import team.marker.util.Constants.MODE
import team.marker.util.Constants.PRODUCTS
import team.marker.util.PreferenceManager
import team.marker.util.runDelayed
import team.marker.view.pick.camera.CameraSource
import team.marker.view.pick.camera.GraphicOverlay
import team.marker.view.pick.camera.barcode.BarcodeGraphic
import team.marker.view.pick.camera.barcode.BarcodeTrackerFactory
import team.marker.view.pick.complete.PickCompleteViewModel
import java.io.IOException

class PickFragment : Fragment(R.layout.fragment_pick) {

    private val viewModel by inject<PickCompleteViewModel>()
    private val products = arrayListOf<PickProduct>()
    private val prefManager: PreferenceManager by lazy { PreferenceManager(requireActivity()) }
    private var cameraSource: CameraSource? = null
    private var pickMode = 0
    private var lastId = 0
    private var torchOn: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pickMode = prefManager.getInt(MODE) ?: 0

        btn_add.setOnClickListener { addProductQuantity() }
        btn_cancel.setOnClickListener { cancelProduct() }

        val rc = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource()
        } else {
            requestCameraPermission()
        }

        viewModel.products.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                products.clear()
                products.addAll(it)

                it.map {
                    if (pickMode != 0) {
                        pick_window.visibility = View.VISIBLE
                        when (pickMode) {
                            1 -> {
                                pick_note.text = getString(R.string.enter_number_accepted_units)
                                pick_quantity.inputType = InputType.TYPE_CLASS_NUMBER
                            }
                            2 -> pick_note.text = getString(R.string.enter_product_length)
                            3 -> pick_note.text = getString(R.string.enter_product_weight)
                            4 -> pick_note.text = getString(R.string.enter_product_volume)
                        }
                    }

                    lastId = it.id ?: 0
                    pick_success_text.visibility = View.VISIBLE
                    2000L.runDelayed { pick_success_text?.visibility = GONE }
                }
                ToneGenerator(AudioManager.STREAM_MUSIC, 100).startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
                pick_success_text.text = getString(R.string.recognized, it.size, it.size)
            }
        }

        btn_scan_flash.setOnClickListener {
            toggleTorch(torchOn)
            torchOn = !torchOn
            btn_scan_flash.setImageResource(if (torchOn) R.drawable.ic_flash_off_2 else R.drawable.ic_flash_2)
        }

        btn_settings.setOnClickListener {
            findNavController().navigate(R.id.action_pickFragment_to_pickSettingsFragment)
        }

        btn_scan_back.setOnClickListener { goToComplete() }
    }

    private fun addProductQuantity() {
        pick_window.visibility = GONE
        pick_bg.visibility = GONE

        val quantityRaw = pick_quantity.text.toString()
        val quantity = if (quantityRaw.isEmpty()) 0.0 else quantityRaw.toDouble()
        if (quantity <= 0) return

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            products.removeIf { it.id == lastId }
        }

        val product = PickProduct()
        product.id = lastId
        product.quantity = quantity
        product.type = pickMode
        products.add(product)

        goToComplete()
    }

    private fun cancelProduct() {
        prefManager.saveInt(MODE, 0)
        pick_window.visibility = GONE
    }

    private fun goToComplete() {
        findNavController().navigate(
            R.id.action_pickFragment_to_pickCompleteFragment,
            bundleOf(PRODUCTS to products)
        )
    }

    private fun requestCameraPermission() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
            requestPermissions(permissions, RC_HANDLE_CAMERA_PERM)
            return
        }
        val listener = OnClickListener {
            requestPermissions(permissions, RC_HANDLE_CAMERA_PERM)
        }
        topLayout.setOnClickListener(listener)
        Snackbar.make(graphicOverlay!!, R.string.permission_camera_rationale, LENGTH_INDEFINITE)
            .setAction(getString(R.string.ok), listener)
            .show()
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     *
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private fun createCameraSource() {
        val context = requireActivity().applicationContext
        val barcodeDetector = BarcodeDetector.Builder(context).build()
        val barcodeFactory = BarcodeTrackerFactory(
            graphicOverlay as GraphicOverlay<BarcodeGraphic?>,
            viewModel,
            this
        )
        barcodeDetector.setProcessor(MultiProcessor.Builder(barcodeFactory).build())

        var builder = CameraSource.Builder(requireActivity().applicationContext, barcodeDetector)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1600, 1024)
            .setRequestedFps(15.0f)

        builder = builder.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
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
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createCameraSource()
            return
        }
        val listener = DialogInterface.OnClickListener { _, _ ->
            requestPermissions(permissions, RC_HANDLE_CAMERA_PERM)
        }
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.app_name)
            .setMessage(R.string.permission_camera_rationale)
            .setPositiveButton(getString(R.string.ok), listener)
            .show()
    }

    @Throws(SecurityException::class)
    private fun startCameraSource() {
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
            requireActivity().applicationContext
        )
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg.show()
        }
        if (cameraSource != null) {
            try {
                preview?.start(cameraSource, graphicOverlay)
            } catch (e: IOException) {
                cameraSource?.release()
                cameraSource = null
            }
        }
    }

    private fun toggleTorch(torchOn: Boolean) {
        cameraSource?.setFlashMode(if (torchOn) FLASH_MODE_OFF else FLASH_MODE_TORCH)
    }

    companion object {
        // intent request code to handle updating play services if needed.
        private const val RC_HANDLE_GMS = 9001

        // permission request codes need to be < 256
        private const val RC_HANDLE_CAMERA_PERM = 2
    }
}