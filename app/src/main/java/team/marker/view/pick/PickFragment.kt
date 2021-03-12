package team.marker.view.pick

import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.hardware.Camera.Parameters.*
import android.os.Bundle
import android.text.InputType.TYPE_CLASS_NUMBER
import android.transition.TransitionManager
import android.transition.TransitionManager.beginDelayedTransition
import android.view.MotionEvent.ACTION_DOWN
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
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_pick.*
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import team.marker.R
import team.marker.model.Dialog
import team.marker.model.Product
import team.marker.model.requests.PickProduct
import team.marker.util.*
import team.marker.util.Constants.MAIN_STORAGE
import team.marker.util.Constants.MODE
import team.marker.util.Constants.PRODUCTS
import team.marker.util.Constants.PRODUCT_ID
import team.marker.util.Constants.RC_HANDLE_CAMERA_PERM
import team.marker.util.Constants.RC_HANDLE_GMS
import team.marker.util.barcode.BarcodeGraphic
import team.marker.util.barcode.BarcodeTrackerFactory
import team.marker.util.camera.CameraSource
import team.marker.util.camera.GraphicOverlay
import team.marker.view.pick.complete.PickCompleteViewModel
import java.io.IOException
import kotlin.properties.Delegates

class PickFragment : Fragment(R.layout.fragment_pick) {

    private val viewModel by inject<PickCompleteViewModel>()
    private var products = mutableListOf<PickProduct>()
    private var dProducts = mutableListOf<PickProduct>()
    private val preferences: SharedPreferences by inject(named(MAIN_STORAGE))
    private var textRecognizer by Delegates.notNull<TextRecognizer>()
    private var cameraSource: CameraSource? = null
    private var pickMode = 0
    private var lastProduct: Product? = null
    private var torchOn: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setOnTouchListener { _, event ->
            val x = event.x.toInt() + 200
            val y = event.y.toInt()
            if (pickMode != 5 && event.action == ACTION_DOWN) {
                viewModel.products.observeOnce(viewLifecycleOwner) { products ->
                    products.forEach {
                        if (it.rectName?.contains(x, y) == true && it.isVisible) {
                            findNavController().navigate(
                                R.id.action_pickFragment_to_productFragment,
                                bundleOf(PRODUCT_ID to it.id.toString())
                            )
                        }
                        if (it.rectButton?.contains(x, y) == true && it.isVisible) {
                            if (pickMode != 0) {
                                when (it.clickStatus) {
                                    0 -> {
                                        beginDelayedTransition(scan, btn_complete.getTransform(pick_window))
                                        btn_complete.gone()
                                        pick_window.visible()
                                        lastProduct = it
                                    }
                                    1 -> {
                                        val list = mutableListOf<PickProduct>()
                                        dProducts.forEach { product ->
                                            if (product.id != it.id) list.add(product)
                                        }
                                        dProducts = list
                                    }
                                }
                            }
                            when (pickMode) {
                                1 -> {
                                    pick_note.text = getString(R.string.enter_number_accepted_units)
                                    pick_quantity.inputType = TYPE_CLASS_NUMBER
                                }
                                2 -> pick_note.text = getString(R.string.enter_product_length)
                                3 -> pick_note.text = getString(R.string.enter_product_weight)
                                4 -> pick_note.text = getString(R.string.enter_product_volume)
                            }
                            viewModel.setClickStatus(
                                Product(it.id, it.name, it.rectName, it.rectButton, if (it.clickStatus == 0) 1 else 0)
                            )
                        }
                    }
                }
            }
            true
        }

        pickMode = preferences.getAny(0, MODE) as Int

        btn_add.setOnClickListener { addProductQuantity() }
        btn_cancel.setOnClickListener {
            beginDelayedTransition(scan, pick_window.getTransform(btn_complete))
            btn_complete.visible()
            pick_window.gone()
            if (lastProduct != null) {
                viewModel.setClickStatus(
                    Product(lastProduct!!.id, lastProduct!!.name, lastProduct!!.rectName, lastProduct!!.rectButton, if (lastProduct!!.clickStatus == 0) 1 else 0)
                )
            }
        }

        val rc = ActivityCompat.checkSelfPermission(requireContext(), CAMERA)
        if (rc == PERMISSION_GRANTED) createCameraSource() else requestCameraPermission()

        viewModel.currentProduct.observe(viewLifecycleOwner) { product ->
            if (product != null) {
                when (pickMode) {
                    5 -> {
                        if (products.isEmpty() || products.all { it.id != product.id }) {
                            products.add(product)
                            pick_success_text.visible()
                            2000L.runDelayed { pick_success_text?.gone() }
                            150.beepSound()
                            pick_success_text.text = getString(R.string.recognized, products.size, products.size)
                        }
                    }
                }
            }
        }

        btn_scan_flash.setOnClickListener {
            toggleTorch(torchOn)
            torchOn = !torchOn
            btn_scan_flash.setImageResource(if (torchOn) R.drawable.ic_flash_off else R.drawable.ic_flash)
        }

        btn_settings.setOnClickListener {
            findNavController().navigate(R.id.action_pickFragment_to_pickSettingsFragment)
        }

        btn_complete.setOnClickListener { goToComplete() }
    }

    private fun addProductQuantity() {
        beginDelayedTransition(scan, pick_window.getTransform(btn_complete))
        btn_complete.visible()
        pick_window.gone()
        pick_bg.gone()
        requireView().hideKeyboard()

        val quantityRaw = pick_quantity.text.toString()
        val quantity = if (quantityRaw.isEmpty()) 0.0 else quantityRaw.toDouble()
        if (quantity <= 0) return
        pick_quantity.setText("")

        val product = PickProduct()
        product.id = lastProduct?.id
        product.quantity = quantity
        product.type = pickMode
        dProducts.add(product)
    }

    private fun goToComplete() {
        when (pickMode) {
            0 -> {
                if (viewModel.products.value == null) viewModel.products.value = mutableSetOf()
                viewModel.products.observeOnce(viewLifecycleOwner) {
                    val list = mutableListOf<PickProduct>()
                    it.forEach {
                        if (it.clickStatus == 1) list.add(PickProduct(it.id, 1.toDouble(), 0))
                    }
                    findNavController().navigate(
                        R.id.action_pickFragment_to_pickCompleteFragment,
                        bundleOf(PRODUCTS to list)
                    )
                }
            }
            1, 2, 3, 4 -> {
                findNavController().navigate(
                    R.id.action_pickFragment_to_pickCompleteFragment,
                    bundleOf(PRODUCTS to dProducts)
                )
            }
            5 -> {
                findNavController().navigate(
                    R.id.action_pickFragment_to_pickCompleteFragment,
                    bundleOf(PRODUCTS to products)
                )
            }
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
        top_layout.setOnClickListener(listener)
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

        textRecognizer = TextRecognizer.Builder(requireContext()).build()
        textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
                val items = detections.detectedItems
                if (pickMode != 5 || items.size() <= 0) return

                val builder = StringBuilder()
                for (i in 0 until items.size()) {
                    val item = items.valueAt(i)
                    if (item.value.isDigitsOnly()) builder.append(item.value)
                }

                if (builder.length == 13) {
                    val id = builder.trimStart('0').dropLast(1).toString().toInt()
                    if (products.all { it.id != id }) {
                        products.add(PickProduct(id, 1.toDouble(), 0))
                        pick_toolbar.post {
                            pick_success_text.visible()
                            2000L.runDelayed { pick_success_text?.gone() }
                            150.beepSound()
                            pick_success_text.text = getString(R.string.recognized, products.size, products.size)
                        }
                    }
                }
            }
        })

        val multiDetector = MultiDetector.Builder()
            .add(barcodeDetector)
            .add(textRecognizer)
            .build()

        val builder = CameraSource.Builder(requireContext(), multiDetector)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
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