package team.marker.view.scan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.google.zxing.integration.android.IntentIntegrator
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import team.marker.R


class ScanFragment : Fragment() {

    private lateinit var viewModel: ScanViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel { parametersOf() }
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
        //Log.e("result", "Result 3: $res");
        //Toast.makeText(activity, "Scan Complete: $res", Toast.LENGTH_SHORT).show()
        if (res != null) {
            val bundle = Bundle()
            bundle.putString("product_id", res)
            findNavController(this).navigate(R.id.productFragment, bundle)
        } else findNavController(this).navigate(R.id.homeFragment)
        //finish()
        //onQRScanResult(scanResult)
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