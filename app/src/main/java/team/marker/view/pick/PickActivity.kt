package team.marker.view.pick

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.AudioManager.STREAM_MUSIC
import android.media.ToneGenerator
import android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
import android.os.Bundle
import android.text.InputType
import android.view.View.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_pick.*
import org.koin.android.ext.android.inject
import team.marker.R
import team.marker.model.requests.PickProduct
import team.marker.util.Constants.PRODUCTS
import team.marker.util.PreferenceManager
import team.marker.util.hideKeyboard
import team.marker.util.openFragment
import team.marker.util.runDelayed
import team.marker.view.pick.camera.CameraSource
import team.marker.view.pick.camera.GraphicOverlay
import team.marker.view.pick.camera.barcode.BarcodeGraphic
import team.marker.view.pick.camera.barcode.BarcodeTrackerFactory
import team.marker.view.pick.complete.PickCompleteFragment
import team.marker.view.pick.complete.PickCompleteViewModel
import team.marker.view.pick.settings.PickSettingsFragment
import java.io.IOException

class PickActivity : AppCompatActivity() {

    private val viewModel by inject<PickCompleteViewModel>()
    private val products = arrayListOf<PickProduct>()
    private val prefManager: PreferenceManager by lazy { PreferenceManager(this) }
    private var cameraSource: CameraSource? = null
    private var pickMode = 0
    private var lastId = 0

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.activity_pick)
        window?.statusBarColor = ContextCompat.getColor(this, R.color.dark_blue)
        pickMode = prefManager.getInt("mode") ?: 0

        btn_add.setOnClickListener { addProductQuantity() }
        btn_cancel.setOnClickListener { cancelProduct() }

        val useFlash = intent.getBooleanExtra(USE_FLASH, false)
        if (useFlash) {
            btn_scan_flash_off.visibility = VISIBLE
            btn_scan_flash.visibility = INVISIBLE
        } else {
            btn_scan_flash_off.visibility = INVISIBLE
            btn_scan_flash.visibility = VISIBLE
        }

        val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(true, useFlash)
        } else {
            requestCameraPermission()
        }

        viewModel.products.observe(this) {
            if (it.isNotEmpty()) {
                products.clear()
                products.addAll(it)

                it.map {
                    if (pickMode != 0) {
                        pick_window.visibility = VISIBLE
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
                    pick_success_text.visibility = VISIBLE
                    2000L.runDelayed { pick_success_text.visibility = GONE }
                }
                ToneGenerator(STREAM_MUSIC, 100).startTone(TONE_CDMA_ALERT_CALL_GUARD, 150)
                pick_success_text.text = getString(R.string.recognized, it.size, it.size)
            }
        }

        btn_scan_flash.setOnClickListener {
            val intent = Intent(this, PickActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra(USE_FLASH, true)
            startActivity(intent)
        }

        btn_scan_flash_off.setOnClickListener {
            val intent = Intent(this, PickActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra(USE_FLASH, false)
            startActivity(intent)
        }

        btn_settings.setOnClickListener {
            pick_toolbar.visibility = GONE
            preview?.visibility = GONE
            graphicOverlay?.visibility = GONE
            openFragment(PickSettingsFragment())
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
        prefManager.saveInt("mode", 0)
        val intent = Intent(this, PickActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.putExtra(USE_FLASH, false)
        startActivity(intent)
    }

    private fun goToComplete() {
        window.decorView.hideKeyboard()
        pick_toolbar.visibility = GONE
        preview.visibility = GONE
        graphicOverlay.visibility = GONE
        val bundle = Bundle()
        bundle.putParcelableArrayList(PRODUCTS, products)
        openFragment(PickCompleteFragment().apply {
            arguments = bundle
        })
    }

    private fun requestCameraPermission() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM)
            return
        }
        val listener = OnClickListener {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM)
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
    private fun createCameraSource(autoFocus: Boolean, useFlash: Boolean) {
        val context = applicationContext
        val barcodeDetector = BarcodeDetector.Builder(context).build()
        val barcodeFactory = BarcodeTrackerFactory(
            graphicOverlay as GraphicOverlay<BarcodeGraphic?>,
            viewModel,
            this
        )
        barcodeDetector.setProcessor(MultiProcessor.Builder(barcodeFactory).build())

        var builder = CameraSource.Builder(applicationContext, barcodeDetector)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1600, 1024)
            .setRequestedFps(15.0f)

        builder = builder.setFocusMode(
            if (autoFocus) Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE else null
        )
        cameraSource = builder
            .setFlashMode(if (useFlash) Camera.Parameters.FLASH_MODE_TORCH else null)
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
            val useFlash = intent.getBooleanExtra(USE_FLASH, false)
            createCameraSource(true, useFlash)
            return
        }
        val listener = DialogInterface.OnClickListener { _, _ ->
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM)
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.app_name)
            .setMessage(R.string.permission_camera_rationale)
            .setPositiveButton(getString(R.string.ok), listener)
            .show()
    }

    @Throws(SecurityException::class)
    private fun startCameraSource() {
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
            applicationContext
        )
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg.show()
        }
        if (cameraSource != null) {
            try {
                preview!!.start(cameraSource, graphicOverlay)
            } catch (e: IOException) {
                cameraSource!!.release()
                cameraSource = null
            }
        }
    }

    companion object {
        // intent request code to handle updating play services if needed.
        private const val RC_HANDLE_GMS = 9001

        // permission request codes need to be < 256
        private const val RC_HANDLE_CAMERA_PERM = 2

        // constants used to pass extra data in the intent
        const val USE_FLASH = "UseFlash"
    }
}