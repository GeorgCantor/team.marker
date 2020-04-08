package team.marker.view.scan

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.google.zxing.integration.android.IntentIntegrator
import team.marker.R

class ScanFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readQrCode()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        val res = scanResult?.contents;
        //Toast.makeText(activity, "Scan Complete: $res", Toast.LENGTH_SHORT).show()
        if (res != null) {
            val bundle = Bundle()
            bundle.putString("product_id", res)
            findNavController(this).navigate(R.id.productFragment, bundle)
        } else findNavController(this).navigate(R.id.homeFragment)
    }

    private fun readQrCode() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.addExtra("RESULT_DISPLAY_DURATION_MS", 1000L);
        integrator.setPrompt("")
        //integrator.setBarcodeImageEnabled(false)
        integrator.setOrientationLocked(false)
        integrator.captureActivity = MyCaptureActivity::class.java
        //integrator.captureActivity = CaptureActivity::class.java
        integrator.initiateScan()
    }

}