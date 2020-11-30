package team.marker.view.scan

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_scan.*
import team.marker.R
import team.marker.model.requests.PickProduct
import team.marker.util.Constants.PRODUCTS_URL
import team.marker.util.scanner.ScannerDecoratedBarcodeView

class ScanFragment : Fragment(R.layout.fragment_scan) {

    companion object {
        private var products: MutableList<PickProduct> = mutableListOf()
        private lateinit var capture: PickCaptureManager

        fun addProduct(view: View, rawResultSize: Int, currentProductIds: MutableList<String>) {
            val productIds = arrayListOf<String>()
            // products
            for (currentProductId in currentProductIds) {
                val product = PickProduct()
                product.id = currentProductId.toInt()
                product.quantity = 1.0
                productIds.add(currentProductId)
                // add
                updateProducts(product)
            }

            ScanFragment().openProduct(view)
        }

        private fun updateProducts(currentProduct: PickProduct): String {
            // vars
            var exist = false
            // parse
            for (i in 0 until products.size) {
                if (products[i].id == currentProduct.id) {
                    if (products[i].type == currentProduct.type) {
                        products[i].quantity += currentProduct.quantity
                        exist = true
                        break
                    }
                }
            }
            // add new
            if (!exist) products.add(currentProduct)
            // output
            return if (exist) "update" else "add"
        }
    }

    private var barcodeScannerView: ScannerDecoratedBarcodeView? = null
    private var torchOn: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        barcodeScannerView = zxing_barcode_scanner as ScannerDecoratedBarcodeView
        capture = PickCaptureManager(requireActivity(), barcodeScannerView!!, view)
        capture.decode()

        btn_scan_flash.setOnClickListener { toggleFlash() }
        btn_cancel.setOnClickListener { activity?.onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.statusBarColor = getColor(requireContext(), R.color.blackText)
        capture.onResume()
    }

    override fun onPause() {
        products.clear()
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun openProduct(view: View) {
        val bundle = Bundle()
        val productIds = arrayListOf<String>()
        for(product in products) productIds.add(product.id.toString())
        val productIdsStr = productIds.joinToString(",")
        bundle.putString("product_url", "$PRODUCTS_URL$productIdsStr")
        Navigation.findNavController(view).navigate(R.id.action_scanFragment_to_productFragment, bundle)
    }

    private fun toggleFlash() {
        barcodeScannerView?.setTorchOn()
        when (torchOn) {
            false -> {
                barcodeScannerView?.setTorchOn()
                torchOn = true
                btn_scan_flash.setImageResource(R.drawable.ic_flash_off_2)
            }
            true -> {
                barcodeScannerView?.setTorchOff()
                torchOn = false
                btn_scan_flash.setImageResource(R.drawable.ic_flash_2)
            }
        }
    }
}