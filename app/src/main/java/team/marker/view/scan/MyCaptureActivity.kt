package team.marker.view.scan

import android.R.bool
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.client.android.R
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import kotlinx.android.synthetic.main.tmp_scan_capture.*


class MyCaptureActivity : AppCompatActivity() { //Nothing in side.
    private var capture: CaptureManager? = null
    private var barcodeScannerView: DecoratedBarcodeView? = null
    private var torchOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        barcodeScannerView = initializeContent()
        capture = CaptureManager(this, barcodeScannerView)
        capture!!.initializeFromIntent(intent, savedInstanceState)
        capture!!.decode()

        btn_scan_back.setOnClickListener {
            Log.e("Message", "back button pressed")
            //activity?.finish()
            finish()
            /*setContentView(team.marker.R.layout.activity_main)

            val navHostFragment = navHostFragment as NavHostFragment
            val inflater = navHostFragment.navController.navInflater
            val graph = inflater.inflate(team.marker.R.navigation.nav_graph)
            graph.startDestination = team.marker.R.id.homeFragment

            navHostFragment.navController.graph = graph*/
        }

        btn_scan_flash.setOnClickListener {
            Log.e("Message", "flash on")
            barcodeScannerView?.setTorchOn()

            if (!torchOn) {
                barcodeScannerView?.setTorchOn();
                torchOn = true
            } else {
                barcodeScannerView?.setTorchOff();
                torchOn = false
            }
        }
    }

    /**
     * Override to use a different layout.
     *
     * @return the DecoratedBarcodeView
     */
    protected fun initializeContent(): DecoratedBarcodeView? {
        //setContentView(R.layout.zxing_capture)
        setContentView(team.marker.R.layout.tmp_scan_capture)
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
