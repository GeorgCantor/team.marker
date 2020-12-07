package team.marker.view.pick

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.zxing.client.android.BeepManager
import com.google.zxing.client.android.InactivityTimer
import com.google.zxing.client.android.Intents
import team.marker.R
import team.marker.util.scanner.ScannerCameraPreview
import team.marker.util.scanner.ScannerDecoratedBarcodeView
import team.marker.util.scanner.common.ScannerBarcodeCallback
import team.marker.util.scanner.common.ScannerBarcodeResultMultiple
import team.marker.util.scanner.common.ScannerResultPoint
import team.marker.view.pick.products.PickProductsViewModel

class PickCaptureManager(
    private val activity: FragmentActivity,
    private val viewModel: PickProductsViewModel,
    val barcodeView: ScannerDecoratedBarcodeView,
    private val view: View
) {

    private var destroyed = false
    private val inactivityTimer: InactivityTimer
    private lateinit var beepManager: BeepManager
    private lateinit var handler: Handler
    private var finishWhenClosed = false

    private val callback: ScannerBarcodeCallback = object : ScannerBarcodeCallback {
        override fun barcodeResult(result: ScannerBarcodeResultMultiple) {
            barcodeView.pause()
            beepManager.playBeepSoundAndVibrate()
            handler.post { returnResult(result) }
        }

        override fun possibleResultPoints(resultPoints: List<ScannerResultPoint>) {}
    }

    private val stateListener: ScannerCameraPreview.StateListener = object :
        ScannerCameraPreview.StateListener {
        override fun previewSized() {}
        override fun previewStarted() {}
        override fun previewStopped() {}
        override fun cameraError(error: Exception?) { displayFrameworkBugMessageAndExit() }
        override fun cameraClosed() { if (finishWhenClosed) finish() }
    }

    fun decode() {
        barcodeView.decodeContinuous(callback)
    }

    fun onResume() {
        if (Build.VERSION.SDK_INT >= 23) openCameraWithPermission()
        else barcodeView.resume()
        inactivityTimer.start()
    }

    private var askedPermission = false

    @TargetApi(23)
    private fun openCameraWithPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) === PackageManager.PERMISSION_GRANTED) {
            barcodeView.resume()
        } else if (!askedPermission) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), cameraPermissionReqCode)
            askedPermission = true
        } else {
            // Wait for permission result
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == cameraPermissionReqCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                barcodeView.resume()
            } else {
                displayFrameworkBugMessageAndExit()
            }
        }
    }

    fun onPause() {
        inactivityTimer.cancel()
        barcodeView.pauseAndWait()
    }

    fun onDestroy() {
        destroyed = true
        inactivityTimer.cancel()
        handler.removeCallbacksAndMessages(null)
    }

    private fun finish() {
        activity.finish()
    }

    private fun closeAndFinish() {
        if (barcodeView.barcodeView!!.isCameraClosed) finish()
        else finishWhenClosed = true
        barcodeView.pause()
        //barcodeView.pauseAndWait()
        inactivityTimer.cancel()
    }

    private fun returnResultTimeout() {
        val intent = Intent(Intents.Scan.ACTION)
        intent.putExtra(Intents.Scan.TIMEOUT, true)
        activity.setResult(Activity.RESULT_CANCELED, intent)
        closeAndFinish()
    }

    private fun returnResult(rawResult: ScannerBarcodeResultMultiple) {
        // vars
        val rx1 = Regex(";eot;")
        val rawArray = rawResult.toString().split(rx1)
        val productIds: MutableList<String> = ArrayList()
        var rawResultSize = 0
        // parse
        for (rawItem in rawArray) {
            Log.e("scanItem", rawItem)
            val rx2 = "^https://marker.team/products/([0-9]+)$".toRegex()
            if (rawItem.matches(rx2)) {
                val productId = rawItem.replace(rx2, "$1")
                productIds.add(productId)
            }
            if (rawItem.isNotEmpty()) rawResultSize++
        }
        // update
        PickFragment.addProduct(view, rawResultSize, productIds)
        viewModel.getProducts(productIds.joinToString(","))
    }

    private fun displayFrameworkBugMessageAndExit() {
        if (activity.isFinishing || destroyed || finishWhenClosed) return
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(activity.getString(R.string.zxing_app_name))
        builder.setMessage(activity.getString(R.string.zxing_msg_camera_framework_bug))
        builder.setPositiveButton(R.string.zxing_button_ok) { dialog, which -> finish() }
        builder.setOnCancelListener { finish() }
        builder.show()
    }

    companion object {
        private val TAG = PickCaptureManager::class.java.simpleName
        private var cameraPermissionReqCode = 250
        private const val SAVED_ORIENTATION_LOCK = "SAVED_ORIENTATION_LOCK"

        fun resultIntent(rawResult: String): Intent {
            val intent = Intent(Intents.Scan.ACTION)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
            intent.putExtra(Intents.Scan.RESULT, rawResult)
            return intent
        }

        fun getCameraPermissionReqCode(): Int {
            return cameraPermissionReqCode
        }

        fun setCameraPermissionReqCode(cameraPermissionReqCode: Int) {
            Companion.cameraPermissionReqCode = cameraPermissionReqCode
        }
    }

    init {
        barcodeView.barcodeView!!.addStateListener(stateListener)
        handler = Handler()
        inactivityTimer = InactivityTimer(activity, Runnable {
            Log.d(TAG, "Finishing due to inactivity")
            finish()
        })
        beepManager = BeepManager(activity)
    }
}