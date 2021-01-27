package team.marker.view.scan

import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Rect
import android.hardware.Camera
import android.hardware.Camera.Parameters.FLASH_MODE_OFF
import android.hardware.Camera.Parameters.FLASH_MODE_TORCH
import android.media.AudioManager
import android.media.ToneGenerator
import android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.core.text.isDigitsOnly
import androidx.core.util.forEach
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiDetector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_scan.*
import team.marker.R
import team.marker.util.Constants
import team.marker.util.Constants.FOCUS_MODE
import team.marker.util.Constants.PRODUCTS_URL
import team.marker.util.Constants.PRODUCT_URL
import team.marker.view.pick.camera.CameraSource
import java.io.IOException
import kotlin.math.abs
import kotlin.properties.Delegates

class ScanFragment : Fragment(R.layout.fragment_scan) {

    companion object {
        private const val RC_HANDLE_GMS = 9001
        private const val RC_HANDLE_CAMERA_PERM = 2
    }

    private var products = mutableListOf<String>()
    private var textRecognizer by Delegates.notNull<TextRecognizer>()
    private var cameraSource: CameraSource? = null
    private var torchOn = false
    private var focusMode = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            focusMode = it.get(FOCUS_MODE) as Int
        }

        val rc = ActivityCompat.checkSelfPermission(requireContext(), CAMERA)
        if (rc == PERMISSION_GRANTED) createCameraSource() else requestCameraPermission()

        btn_manual_focus.setImageResource(if (focusMode == 0) R.drawable.ic_flash_2 else R.drawable.ic_flash_off_2)

        btn_scan_flash.setOnClickListener {
            toggleTorch(torchOn)
            torchOn = !torchOn
            btn_scan_flash.setImageResource(if (torchOn) R.drawable.ic_flash_off_2 else R.drawable.ic_flash_2)
        }

        btn_manual_focus.setOnClickListener {
            findNavController().navigate(
                R.id.action_scanFragment_self,
                bundleOf(FOCUS_MODE to if (focusMode == 0) 1 else 0)
            )
        }

        preview.setOnTouchListener { _, motionEvent ->
            if (cameraSource != null && focusMode == 1) {
                val rect = calculateFocusArea(motionEvent.x, motionEvent.y)
                cameraSource?.doTouchFocus(rect)
            }
            true
        }
    }

    private fun calculateFocusArea(x: Float, y: Float): Rect {
        val left = clamp(java.lang.Float.valueOf(x / preview.width * 2000 - 1000).toInt(), 1000)
        val top = clamp(java.lang.Float.valueOf(y / preview.height * 2000 - 1000).toInt(), 1000)
        return Rect(left, top, left + 1000, top + 1000)
    }

    private fun clamp(touchCoordinateInCameraReper: Int, focusAreaSize: Int): Int {
        return if (abs(touchCoordinateInCameraReper) + focusAreaSize / 2 > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                1000 - focusAreaSize / 2
            } else {
                -1000 + focusAreaSize / 2
            }
        } else {
            touchCoordinateInCameraReper - focusAreaSize / 2
        }
    }

    private fun requestCameraPermission() {
        val permissions = arrayOf(CAMERA)
        if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), CAMERA)) {
            requestPermissions(permissions, RC_HANDLE_CAMERA_PERM)
            return
        }
        val listener = OnClickListener {
            requestPermissions(permissions, RC_HANDLE_CAMERA_PERM)
        }
        preview.setOnClickListener(listener)
        Snackbar.make(graphicOverlay!!, R.string.permission_camera_rationale, LENGTH_INDEFINITE)
            .setAction(getString(R.string.ok), listener)
            .show()
    }

    @SuppressLint("InlinedApi")
    private fun createCameraSource() {
        val barcodeDetector = BarcodeDetector.Builder(requireContext())
            .setBarcodeFormats(Barcode.ALL_FORMATS).build()
        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
                detections?.detectedItems?.forEach { _, value ->
                    val product = value.rawValue.takeLastWhile { it.isDigit() }
                    if (product != "") products.add(product)
                }

                when (products.size) {
                    1 -> openProduct()
                    in 2..Int.MAX_VALUE -> openProducts()
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
                    products.add(id.toString())
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
            .setRequestedFps(15.0f)
            .setFocusMode(if (focusMode == 0) Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE else Camera.Parameters.FOCUS_MODE_FIXED)

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
            dlg?.show()
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

    fun openProduct() {
        if (isResumed) ToneGenerator(AudioManager.STREAM_MUSIC, 100).startTone(TONE_CDMA_ALERT_CALL_GUARD, 150)
        val productIdsStr = products.joinToString(",")
        products.clear()
        findNavController(this).navigate(
            R.id.action_scannFragment_to_productFragment,
            bundleOf(PRODUCT_URL to "${PRODUCTS_URL}$productIdsStr")
        )
    }

    fun openProducts() {
        if (isResumed) ToneGenerator(AudioManager.STREAM_MUSIC, 100).startTone(TONE_CDMA_ALERT_CALL_GUARD, 150)
        val productIdsStr = products.joinToString(",")
        products.clear()
        findNavController(this).navigate(
            R.id.action_scannFragment_to_pickProductsFragment,
            bundleOf(Constants.PRODUCT_IDS to productIdsStr)
        )
    }
}