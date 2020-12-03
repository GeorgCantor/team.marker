package team.marker.view.breach

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_breach.*
import team.marker.R
import team.marker.util.scanner.ScannerDecoratedBarcodeView

class BreachFragment : Fragment(R.layout.fragment_breach) {

    companion object {
        private var productIds: MutableList<String> = mutableListOf()

        fun sendResult(view: View, currentProductIds: MutableList<String>) {
            for (currentProductId in currentProductIds) {
                if (!productIds.contains(currentProductId)) productIds.add(currentProductId)
            }

            if (productIds.isNotEmpty()) {
                val bundle = Bundle()
                val array = arrayListOf<String>()
                array.addAll(productIds)
                bundle.putStringArrayList("product_ids", array)
                productIds = mutableListOf()
                Navigation.findNavController(view).navigate(R.id.action_breachFragment_to_breachCompleteFragment, bundle)
            } else {
                Navigation.findNavController(view).navigate(R.id.action_breachFragment_self)
            }
        }
    }

    private lateinit var capture: BreachCaptureManager
    private var barcodeScannerView: ScannerDecoratedBarcodeView? = null
    private var torchOn: Boolean = false

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

    override fun onResume() {
        super.onResume()
        activity?.window?.statusBarColor = getColor(requireContext(), R.color.blackText)
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
