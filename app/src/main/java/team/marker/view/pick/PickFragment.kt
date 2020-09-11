package team.marker.view.pick

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_pick.*
import kotlinx.android.synthetic.main.fragment_pick.btn_scan_flash
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import team.marker.R
import team.marker.model.requests.PickRequest
import team.marker.util.scanner.ScannerCaptureManager
import team.marker.util.scanner.ScannerDecoratedBarcodeView

class PickFragment : Fragment() {

    private lateinit var viewModel: PickViewModel
    private lateinit var capture: ScannerCaptureManager
    private var barcodeScannerView: ScannerDecoratedBarcodeView? = null
    private var torchOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel { parametersOf() }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            activity?.window?.statusBarColor = resources.getColor(R.color.blackText)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pick, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barcodeScannerView = zxing_barcode_scanner as ScannerDecoratedBarcodeView
        capture = ScannerCaptureManager(requireActivity(), barcodeScannerView!!, view)
        capture.decode()

        btn_scan_back.setOnClickListener { finish(view) }

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

    private fun finish(view: View) {
        //Log.e("productIds", productIds.toString())
        val bundle = Bundle()
        //bundle.putString("product_ids", "ids")
        val array = arrayListOf<String>()
        array.addAll(productIds)
        bundle.putStringArrayList("product_ids", array)
        productIds = mutableListOf()
        Navigation.findNavController(view).navigate(R.id.action_pickFragment_to_pickCompleteFragment, bundle)
    }

    companion object {

        private var productIds: MutableList<String> = mutableListOf()

        fun sendResult(currentProductIds: MutableList<String>) {
            for (currentProductId in currentProductIds) {
                if (!productIds.contains(currentProductId)) productIds.add(currentProductId)
            }
            //Log.e("productIds Size", productIds.size.toString())
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
