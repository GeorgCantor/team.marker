package team.marker.view.pick

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.AudioManager.STREAM_MUSIC
import android.media.ToneGenerator
import android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.View.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.BarcodeDetector
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
import team.marker.view.pick.camera.CameraSourcePreview
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
    private var mCameraSource: CameraSource? = null
    private var mPreview: CameraSourcePreview? = null
    private var mGraphicOverlay: GraphicOverlay<BarcodeGraphic?>? = null
    private var pickMode = 0
    private var lastId = 0

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.activity_pick)
        window?.statusBarColor = ContextCompat.getColor(this, R.color.dark_blue)
        pickMode = prefManager.getInt("mode") ?: 0

        btn_add.setOnClickListener { addProductQuantity() }
        btn_cancel.setOnClickListener { cancelProduct() }

        mPreview = findViewById<View>(R.id.preview) as CameraSourcePreview
        mGraphicOverlay = findViewById<View>(R.id.graphicOverlay) as GraphicOverlay<BarcodeGraphic?>

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
                pick_success_text.text = "Распознано ${it.size} из ${it.size}"
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
            preview.visibility = GONE
            graphicOverlay.visibility = GONE
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
        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM)
            return
        }
        val thisActivity: Activity = this
        val listener = View.OnClickListener {
            ActivityCompat.requestPermissions(
                thisActivity, permissions,
                RC_HANDLE_CAMERA_PERM
            )
        }
        findViewById<View>(R.id.topLayout).setOnClickListener(listener)
        Snackbar.make(
            mGraphicOverlay!!, R.string.permission_camera_rationale,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction("OK", listener)
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
        val barcodeFactory = BarcodeTrackerFactory(mGraphicOverlay!!, viewModel, this)
        barcodeDetector.setProcessor(MultiProcessor.Builder(barcodeFactory).build())
        if (!barcodeDetector.isOperational) {
            val lowstorageFilter = IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW)
            val hasLowStorage = registerReceiver(null, lowstorageFilter) != null
            if (hasLowStorage) {
                Toast.makeText(this, "Мало памяти", Toast.LENGTH_LONG).show()
            }
        }

        var builder = CameraSource.Builder(applicationContext, barcodeDetector)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1600, 1024)
            .setRequestedFps(15.0f)

        builder = builder.setFocusMode(
            if (autoFocus) Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE else null
        )
        mCameraSource = builder
            .setFlashMode(if (useFlash) Camera.Parameters.FLASH_MODE_TORCH else null)
            .build()
    }

    override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    override fun onPause() {
        super.onPause()
        if (mPreview != null) mPreview?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mPreview != null) mPreview?.release()
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on [.requestPermissions].
     *
     *
     * **Note:** It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     *
     *
     * @param requestCode  The request code passed in [.requestPermissions].
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     * which is either [PackageManager.PERMISSION_GRANTED]
     * or [PackageManager.PERMISSION_DENIED]. Never null.
     * @see .requestPermissions
     */
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
        val listener = DialogInterface.OnClickListener { dialog, id -> finish() }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Multitracker sample")
            .setMessage(R.string.no_camera_permissions)
            .setPositiveButton("OK", listener)
            .show()
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    @Throws(SecurityException::class)
    private fun startCameraSource() {
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
            applicationContext
        )
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg.show()
        }
        if (mCameraSource != null) {
            try {
                mPreview!!.start(mCameraSource, mGraphicOverlay)
            } catch (e: IOException) {
                mCameraSource!!.release()
                mCameraSource = null
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