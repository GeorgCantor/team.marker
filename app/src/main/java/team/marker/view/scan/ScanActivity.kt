package team.marker.view.scan

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.client.android.R
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import kotlinx.android.synthetic.main.activity_scan.*

class ScanActivity : AppCompatActivity() {

    private var capture: CaptureManager? = null
    private var barcodeScannerView: DecoratedBarcodeView? = null
    private var torchOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        barcodeScannerView = initializeContent()
        capture = CaptureManager(this, barcodeScannerView)
        capture!!.initializeFromIntent(intent, savedInstanceState)
        capture!!.decode()

        btn_scan_back.setOnClickListener { finish() }

        btn_scan_flash.setOnClickListener {
            barcodeScannerView?.setTorchOn()
            when(torchOn) {
                false -> {
                    barcodeScannerView?.setTorchOn();
                    torchOn = true
                }
                true -> {
                    barcodeScannerView?.setTorchOff();
                    torchOn = false
                }
            }
        }
    }

    protected fun initializeContent(): DecoratedBarcodeView? {
        //setContentView(R.layout.zxing_capture)
        setContentView(team.marker.R.layout.activity_scan)
        return findViewById<View>(R.id.zxing_barcode_scanner) as DecoratedBarcodeView
    }

    override fun onResume() {
        super.onResume()
        capture!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture!!.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture!!.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        capture!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return barcodeScannerView!!.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}
