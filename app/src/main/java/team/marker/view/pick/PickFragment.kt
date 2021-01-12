package team.marker.view.pick

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.Camera.Parameters.FLASH_MODE_OFF
import android.hardware.Camera.Parameters.FLASH_MODE_TORCH
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiDetector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_pick.*
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import team.marker.R
import team.marker.model.Product
import team.marker.model.requests.PickProduct
import team.marker.util.*
import team.marker.util.Constants.MAIN_STORAGE
import team.marker.util.Constants.MODE
import team.marker.util.Constants.PRODUCTS
import team.marker.view.pick.camera.CameraSource
import team.marker.view.pick.camera.GraphicOverlay
import team.marker.view.pick.camera.barcode.BarcodeGraphic
import team.marker.view.pick.camera.barcode.BarcodeTrackerFactory
import team.marker.view.pick.complete.PickCompleteViewModel
import java.io.IOException
import kotlin.properties.Delegates

class PickFragment : Fragment(R.layout.fragment_pick) {

    private val viewModel by inject<PickCompleteViewModel>()
    private var products = mutableListOf<PickProduct>()
    private val preferences: SharedPreferences by inject(named(MAIN_STORAGE))
    private var textRecognizer by Delegates.notNull<TextRecognizer>()
    private var cameraSource: CameraSource? = null
    private var pickMode = 0
    private var lastId = 0
    private var torchOn: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setOnTouchListener { v, event ->
            val x = event.x.toInt()
            val y = event.y.toInt()
            if (event.action == MotionEvent.ACTION_DOWN) {
                viewModel.products.observeOnce(viewLifecycleOwner) { products ->
                    products.forEach {
                        if (it.rect?.contains(x, y) == true) {
                            viewModel.setClickStatus(
                                Product(it.id, it.name, it.rect, if (it.clickStatus == 0) 1 else 0)
                            )
                        }
                    }
                }
            }
            true
        }

        pickMode = preferences.getAny(0, MODE) as Int

        btn_add.setOnClickListener { addProductQuantity() }
        btn_cancel.setOnClickListener { cancelProduct() }

        val rc = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) createCameraSource() else requestCameraPermission()

        viewModel.currentProduct.observe(viewLifecycleOwner) { product ->
            if (product != null) {
                if (pickMode != 0) {
                    pick_window.visible()
                    when (pickMode) {
                        1 -> {
                            pick_note.text = getString(R.string.enter_number_accepted_units)
                            pick_quantity.inputType = InputType.TYPE_CLASS_NUMBER
                        }
                        2 -> pick_note.text = getString(R.string.enter_product_length)
                        3 -> pick_note.text = getString(R.string.enter_product_weight)
                        4 -> pick_note.text = getString(R.string.enter_product_volume)
                    }
                } else {
                    if (products.isEmpty() || products.all { it.id != product.id }) products.add(product)
                }

                lastId = product.id ?: 0
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
        pick_window.gone()
        pick_bg.gone()
        requireView().hideKeyboard()

        val quantityRaw = pick_quantity.text.toString()
        val quantity = if (quantityRaw.isEmpty()) 0.0 else quantityRaw.toDouble()
        if (quantity <= 0) return
        pick_quantity.setText("")

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            products.removeIf { it.id == lastId }
        }

        val product = PickProduct()
        product.id = lastId
        product.quantity = quantity
        product.type = pickMode
        products.add(product)
    }

    private fun cancelProduct() {
        findNavController().navigate(R.id.action_pickFragment_self)
    }

    private fun goToComplete() {
        when (pickMode) {
            0 -> {
                viewModel.products.observeOnce(viewLifecycleOwner) {
                    val list = mutableListOf<PickProduct>()
                    it.forEach {
                       if (it.clickStatus == 1) list.add(PickProduct(it.id, 1.toDouble(), 0))
                    }
                    try {
                        findNavController().navigate(
                            R.id.action_pickFragment_to_pickCompleteFragment,
                            bundleOf(PRODUCTS to list)
                        )
                    } catch (e: IllegalArgumentException) {
                    }
                }
            }
            else -> {
                findNavController().navigate(
                    R.id.action_pickFragment_to_pickCompleteFragment,
                    bundleOf(PRODUCTS to products)
                )
            }
        }
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

    @SuppressLint("InlinedApi")
    private fun createCameraSource() {
        val context = requireActivity().applicationContext
        val barcodeDetector = BarcodeDetector.Builder(context).build()
        val barcodeFactory = BarcodeTrackerFactory(
            graphicOverlay as GraphicOverlay<BarcodeGraphic?>,
            viewModel,
            viewLifecycleOwner
        )
        barcodeDetector.setProcessor(MultiProcessor.Builder(barcodeFactory).build())

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
                    val id = builder.trimStart('0').dropLast(1).toString().toInt()
                    if (products.all { it.id != id }) {
                        viewModel.addProduct(PickProduct(id, 1.toDouble(), 0))
                        products.add(PickProduct(id, 1.toDouble(), 0))
                        lastId = id
                    }
                }
            }
        })

        val multiDetector = MultiDetector.Builder()
            .add(barcodeDetector)
            .add(textRecognizer)
            .build()

        var builder = CameraSource.Builder(requireActivity().applicationContext, multiDetector)
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