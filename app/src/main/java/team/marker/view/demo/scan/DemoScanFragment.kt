package team.marker.view.demo.scan

import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.Camera.Parameters.*
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.core.text.isDigitsOnly
import androidx.core.util.forEach
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiDetector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.Barcode.ALL_FORMATS
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.android.synthetic.main.fragment_scan.*
import team.marker.R
import team.marker.model.Dialog
import team.marker.util.Constants.FOCUS_MODE
import team.marker.util.Constants.PARTNER
import team.marker.util.Constants.PRODUCTS_URL
import team.marker.util.Constants.PRODUCT_ID
import team.marker.util.Constants.PRODUCT_IDS
import team.marker.util.Constants.RC_HANDLE_CAMERA_PERM
import team.marker.util.Constants.RC_HANDLE_GMS
import team.marker.util.beepSound
import team.marker.util.calculateTapArea
import team.marker.util.camera.CameraSource
import team.marker.util.showDialog
import team.marker.util.showPermissionSnackbar
import java.io.IOException
import kotlin.properties.Delegates

class DemoScanFragment : Fragment(R.layout.fragment_scan) {

    private var products = mutableListOf<Pair<String, String?>>()
    private var partner: String? = null
    private var textRecognizer by Delegates.notNull<TextRecognizer>()
    private var cameraSource: CameraSource? = null
    private var torchOn = false
    private var isFocusManual = false
    private var lastTimestamp = System.currentTimeMillis()

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { isFocusManual = it.get(FOCUS_MODE) as Boolean }

        val rc = ActivityCompat.checkSelfPermission(requireContext(), CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) createCameraSource() else requestCameraPermission()

        btn_manual_focus.setImageResource(if (isFocusManual) R.drawable.ic_manual_focus else R.drawable.ic_auto_focus)

        btn_scan_flash.setOnClickListener {
            toggleTorch(torchOn)
            torchOn = !torchOn
            btn_scan_flash.setImageResource(if (torchOn) R.drawable.ic_flash_off else R.drawable.ic_flash)
        }

        btn_manual_focus.setOnClickListener {
            findNavController().navigate(
                R.id.action_demoScanFragment_self,
                bundleOf(FOCUS_MODE to !isFocusManual)
            )
        }

        btn_cancel.setOnClickListener { activity?.onBackPressed() }

        if (cameraSource != null && isFocusManual) {
            preview.setOnTouchListener { _, event ->
                val rect = preview.calculateTapArea(event.x, event.y, 1000F)
                cameraSource?.doTouchFocus(rect)
                false
            }

            frame_view.setOnTouchListener(frame_view)
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
        val barcodeDetector = BarcodeDetector.Builder(requireContext()).setBarcodeFormats(ALL_FORMATS).build()
        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
                if (System.currentTimeMillis() - lastTimestamp > 1000) {
                    detections?.detectedItems?.forEach { _, value ->
                        val product = value.rawValue.replace(PRODUCTS_URL, "").takeWhile { it != '?' }
                        if (value.rawValue.last().isLetter()) partner = value.rawValue.takeLastWhile { it != '=' }
                        if (product != "") products.add(Pair(product, partner))
                    }

                    when (products.size) {
                        1 -> openProduct()
                        in 2..Int.MAX_VALUE -> openProducts()
                    }
                    lastTimestamp = System.currentTimeMillis()
                }
            }
        })

        textRecognizer = TextRecognizer.Builder(requireContext()).build()
        textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
                val items = detections.detectedItems
                if (items.size() <= 0) return

                val builder = StringBuilder()
                for (i in 0 until items.size()) {
                    val item = items.valueAt(i)
                    if (item.value.isDigitsOnly()) builder.append(item.value)
                }

                if (builder.length == 13) {
                    val id = builder.trimStart('0').dropLast(1)
                    products.add(Pair(id.toString(), partner))
                    openProduct()
                }
            }
        })

        val multiDetector = MultiDetector.Builder()
            .add(barcodeDetector)
            .add(textRecognizer)
            .build()

        val builder = CameraSource.Builder(requireActivity().applicationContext, multiDetector)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1600, 1024)
            .setRequestedFps(15.0F)
            .setFocusMode(if (isFocusManual) FOCUS_MODE_FIXED else FOCUS_MODE_CONTINUOUS_PICTURE)

        cameraSource = builder
            .setFlashMode(null)
            .build()
    }

    override fun onResume() {
        super.onResume()
        products.clear()
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

    fun openProduct() {
        150.beepSound()
        val ids = mutableListOf<String>()
        products.forEach { ids.add(it.first) }

        val productIds = ids.joinToString(",")

        findNavController().navigate(
            R.id.action_demoScanFragment_to_demoProductFragment,
            bundleOf(PRODUCT_ID to productIds, PARTNER to partner)
        )
    }

    fun openProducts() {
        150.beepSound()
        val ids = mutableListOf<String>()
        products.forEach {
            if (it.second != null) ids.add("${it.first}--${it.second}") else ids.add(it.first)
        }

        val productIds = ids.joinToString(",")

        findNavController().navigate(
            R.id.action_demoScanFragment_to_demoProductsFragment,
            bundleOf(PRODUCT_IDS to productIds)
        )
    }
}