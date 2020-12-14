package team.marker.view.scan

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.util.forEach
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.fragment_scann.*
import team.marker.R
import team.marker.util.Constants
import team.marker.util.shortToast

class ScannFragment : Fragment(R.layout.fragment_scann) {

    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private val products = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val displayMetrics = DisplayMetrics()
        val wm =
            requireActivity().applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(displayMetrics)

        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        barcodeDetector =
            BarcodeDetector.Builder(requireContext()).setBarcodeFormats(Barcode.ALL_FORMATS).build()

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
                val barcodes = detections?.detectedItems
                barcodes?.forEach { key, value ->
                    products.add(value.rawValue?.takeLastWhile { it.isDigit() } ?: "")
                }

                when (products.size) {
                    1 -> openProduct()
                    in 2..Int.MAX_VALUE -> openProducts()
                }

//                if (barcodes!!.size() > 0) {
//                    tv_barcode.post {
//                        val valueholder = barcodes.valueAt(0).displayValue
//                        tv_barcode.text = valueholder
//                        val isValid = URLUtil.isValidUrl(tv_barcode.text.toString())
//
//                        if (isValid) {
//                            tv_barcode.setTextColor(Color.parseColor("#9acd32"))
//                            tv_barcode.isClickable = true
//                        } else {
//                            tv_barcode.setTextColor(Color.WHITE)
//                            tv_barcode.isClickable = false
//                        }
//                    }
//                }
            }
        })

        cameraSource = CameraSource.Builder(requireContext(), barcodeDetector)
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

    fun openProduct() {
        val bundle = Bundle()
        val productIdsStr = products.joinToString(",")
        bundle.putString("product_url", "${Constants.PRODUCTS_URL}$productIdsStr")
        products.clear()
        findNavController(this).navigate(R.id.action_scannFragment_to_productFragment, bundle)
    }

    fun openProducts() {
        val bundle = Bundle()
        val productIdsStr = products.joinToString(",")
        bundle.putString("product_ids", productIdsStr)
        products.clear()
        findNavController(this).navigate(R.id.action_scannFragment_to_pickProductsFragment, bundle)
    }
}