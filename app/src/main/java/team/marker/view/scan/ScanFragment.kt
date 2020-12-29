package team.marker.view.scan

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager.FEATURE_CAMERA_FLASH
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.hardware.Camera
import android.hardware.Camera.Parameters.FLASH_MODE_OFF
import android.hardware.Camera.Parameters.FLASH_MODE_TORCH
import android.media.AudioManager.STREAM_MUSIC
import android.media.ToneGenerator
import android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.os.bundleOf
import androidx.core.text.isDigitsOnly
import androidx.core.util.forEach
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiDetector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.Barcode.ALL_FORMATS
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.android.synthetic.main.fragment_scan.*
import team.marker.R
import team.marker.util.Constants.PRODUCTS_URL
import team.marker.util.Constants.PRODUCT_IDS
import team.marker.util.Constants.PRODUCT_URL
import team.marker.util.shortToast
import java.lang.reflect.Field
import kotlin.properties.Delegates

class ScanFragment : Fragment(R.layout.fragment_scan) {

    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private val products = mutableListOf<String>()
    private var torchOn: Boolean = false
    private var textRecognizer by Delegates.notNull<TextRecognizer>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val displayMetrics = DisplayMetrics()
        val wm = requireActivity().applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(displayMetrics)

        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        barcodeDetector = BarcodeDetector.Builder(requireContext()).setBarcodeFormats(ALL_FORMATS).build()
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

        cameraSource = CameraSource.Builder(requireContext(), multiDetector)
            .setRequestedPreviewSize(screenHeight, screenWidth).setRequestedFps(60f)
            .setAutoFocusEnabled(true).build()

        sv_barcode.holder.addCallback(object : SurfaceHolder.Callback2 {
            override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }

            override fun surfaceCreated(holder: SurfaceHolder) {
                if (checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
                    cameraSource.start(holder)
                } else {
                    requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 89)
                }
            }
        })

        btn_scan_flash.setOnClickListener {
            toggleTorch(torchOn)
            torchOn = !torchOn
            btn_scan_flash.setImageResource(if (torchOn) R.drawable.ic_flash_off_2 else R.drawable.ic_flash_2)
        }
        btn_cancel.setOnClickListener { activity?.onBackPressed() }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 89) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                cameraSource.start(sv_barcode.holder)
            } else {
                context?.shortToast(getString(R.string.permission_camera_rationale))
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        barcodeDetector.release()
        cameraSource.stop()
        cameraSource.release()
    }

    @SuppressLint("MissingPermission")
    fun toggleTorch(torchOn: Boolean) {
        requireActivity().packageManager.hasSystemFeature(FEATURE_CAMERA_FLASH)
        cameraSource.start(sv_barcode.holder)
        val camera: Camera? = getCamera(cameraSource)
        val parameters: Camera.Parameters = camera!!.parameters
        parameters.flashMode = if (torchOn) FLASH_MODE_OFF else FLASH_MODE_TORCH
        camera.parameters = parameters
        camera.startPreview()
    }

    private fun getCamera(cameraSource: CameraSource?): Camera? {
        val declaredFields: Array<Field> = CameraSource::class.java.declaredFields
        for (field in declaredFields) {
            if (field.type == Camera::class.java) {
                field.isAccessible = true
                try {
                    return field.get(cameraSource) as Camera
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
                break
            }
        }
        return null
    }

    fun openProduct() {
        if (this.isResumed) ToneGenerator(STREAM_MUSIC, 100).startTone(TONE_CDMA_ALERT_CALL_GUARD, 150)
        val productIdsStr = products.joinToString(",")
        products.clear()
        findNavController(this).navigate(
            R.id.action_scannFragment_to_productFragment,
            bundleOf(PRODUCT_URL to "${PRODUCTS_URL}$productIdsStr")
        )
    }

    fun openProducts() {
        if (this.isResumed) ToneGenerator(STREAM_MUSIC, 100).startTone(TONE_CDMA_ALERT_CALL_GUARD, 150)
        val productIdsStr = products.joinToString(",")
        products.clear()
        findNavController(this).navigate(
            R.id.action_scannFragment_to_pickProductsFragment,
            bundleOf(PRODUCT_IDS to productIdsStr)
        )
    }
}