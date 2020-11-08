package team.marker.view.pick

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_pick.*
import kotlinx.android.synthetic.main.fragment_pick.btn_scan_flash
import kotlinx.android.synthetic.main.fragment_pick.view.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import team.marker.R
import team.marker.model.requests.PickProduct
import team.marker.util.PreferenceManager
import team.marker.util.scanner.ScannerDecoratedBarcodeView

class PickFragment : Fragment() {

    private lateinit var viewModel: PickViewModel
    private lateinit var capture: PickCaptureManager
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
        capture = PickCaptureManager(requireActivity(), barcodeScannerView!!, view)
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

        btn_settings.setOnClickListener { settings(view) }
        btn_pick_window.setOnClickListener { resume(view) }
    }

    private fun finish(view: View) {
        //Log.e("productIds", productIds.toString())
        val bundle = Bundle()
        //bundle.putString("product_ids", "ids")
        val array = arrayListOf<PickProduct>()
        array.addAll(products)
        bundle.putParcelableArrayList("products", array)
        products = mutableListOf()
        Navigation.findNavController(view).navigate(R.id.action_pickFragment_to_pickCompleteFragment, bundle)
    }

    private fun settings(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_pickFragment_to_pickSettingsFragment)
    }

    private fun resume(view: View) {
        // vars
        val mode = PreferenceManager(requireActivity()).getInt("mode") ?: 0
        val product = PickProduct()
        product.id = currentProductId.toInt()
        product.quantity = input_quantity.text.toString()
        product.type = mode
        products.add(product)
        // view
        pick_window.visibility = View.GONE
        pick_bg.visibility = View.GONE
        capture.barcodeView.resume()
    }

    companion object {

        private var products: MutableList<PickProduct> = mutableListOf()
        private var currentProductId: String = "0"

        fun showQuantityWindow(view: View, currentProductIds: MutableList<String>) {
            // parse
            /*for (currentProductId in currentProductIds) {
                if (!productIds.contains(currentProductId)) productIds.add(currentProductId)
            }*/
            currentProductId = "0"
            if (currentProductIds.size > 0) currentProductId = currentProductIds[0]
            // view
            view.pick_window.visibility = View.VISIBLE
            view.pick_bg.visibility = View.VISIBLE
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
