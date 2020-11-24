package team.marker.view.breach

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_breach.*
import team.marker.R
import team.marker.util.scanner.ScannerDecoratedBarcodeView

class BreachFragment : Fragment() {

    private lateinit var capture: BreachCaptureManager
    private var barcodeScannerView: ScannerDecoratedBarcodeView? = null
    private var torchOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            activity?.window?.statusBarColor = resources.getColor(R.color.blackText)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_breach, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barcodeScannerView = zxing_barcode_scanner as ScannerDecoratedBarcodeView
        capture = BreachCaptureManager(requireActivity(), barcodeScannerView!!, view)
        capture.decode()

        btn_scan_back.setOnClickListener { activity?.onBackPressed() }

        btn_scan_flash.setOnClickListener {
            barcodeScannerView?.setTorchOn()
            when(torchOn) {
                false -> {
                    barcodeScannerView?.setTorchOn();
                    torchOn = true
                    btn_scan_flash.setImageResource(R.drawable.ic_flash_off_2)
                }
                true -> {
                    barcodeScannerView?.setTorchOff();
                    torchOn = false
                    btn_scan_flash.setImageResource(R.drawable.ic_flash_2)
                }
            }
        }
    }

    companion object {

        private var productIds: MutableList<String> = mutableListOf()

        fun sendResult(view: View, currentProductIds: MutableList<String>) {
            for (currentProductId in currentProductIds) {
                if (!productIds.contains(currentProductId)) productIds.add(currentProductId)
            }
            Log.e("Breach productIds Size", productIds.size.toString())

            val bundle = Bundle()
            val array = arrayListOf<String>()
            array.addAll(productIds)
            bundle.putStringArrayList("product_ids", array)
            productIds = mutableListOf()
            Navigation.findNavController(view).navigate(R.id.action_breachFragment_to_breachCompleteFragment, bundle)
        }

    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
