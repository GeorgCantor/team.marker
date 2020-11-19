package team.marker.view.pick

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_pick.*
import kotlinx.android.synthetic.main.fragment_pick.btn_scan_flash
import kotlinx.android.synthetic.main.fragment_pick.view.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import team.marker.R
import team.marker.model.requests.PickProduct
import team.marker.util.PreferenceManager
import team.marker.util.runDelayed
import team.marker.util.scanner.ScannerDecoratedBarcodeView

class PickFragment : Fragment() {

    private lateinit var viewModel: PickViewModel
    private var barcodeScannerView: ScannerDecoratedBarcodeView? = null
    private var torchOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel { parametersOf() }
        pickMode = PreferenceManager(requireActivity()).getInt("mode") ?: 0
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            activity?.window?.statusBarColor = resources.getColor(R.color.blackText)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pick, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // scanner
        barcodeScannerView = zxing_barcode_scanner as ScannerDecoratedBarcodeView
        capture = PickCaptureManager(requireActivity(), barcodeScannerView!!, view)
        capture.decode()
        // listeners
        btn_scan_back.setOnClickListener { finish(view) }
        btn_scan_flash.setOnClickListener { toggleFlash() }
        btn_settings.setOnClickListener { settings(view) }
        btn_add.setOnClickListener { addProductQuantity() }
        btn_cancel.setOnClickListener { cancelProduct() }
    }

    private fun finish(view: View) {
        //Log.e("productIds", productIds.toString())
        val bundle = Bundle()
        val array = arrayListOf<PickProduct>()
        array.addAll(products)
        bundle.putParcelableArrayList("products", array)
        products = mutableListOf()
        Navigation.findNavController(view).navigate(R.id.action_pickFragment_to_pickCompleteFragment, bundle)
    }

    private fun toggleFlash() {
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

    private fun settings(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_pickFragment_to_pickSettingsFragment)
    }

    private fun addProductQuantity() {
        // vars
        val quantityRaw = pick_quantity.text.toString()
        val quantity = if (quantityRaw.isEmpty()) 0.0 else quantityRaw.toDouble()
        // validate
        if (quantity <= 0) return
        // vars (product)
        val product = PickProduct()
        product.id = currentProductId.toInt()
        product.quantity = quantity
        product.type = pickMode
        // add
        val status = updateProducts(product)
        // view
        pick_window.visibility = View.GONE
        pick_bg.visibility = View.GONE
        capture.barcodeView.resume()
    }

    private fun cancelProduct() {
        currentProductId = "0"
        pick_window.visibility = View.GONE
        pick_bg.visibility = View.GONE
        capture.barcodeView.resume()
    }

    companion object {

        private var products: MutableList<PickProduct> = mutableListOf()
        private var currentProductId: String = "0"
        private var pickMode: Int = 0
        private lateinit var capture: PickCaptureManager

        fun addProduct(view: View, rawResultSize: Int, currentProductIds: MutableList<String>) {
            // parse
            if (pickMode == 0) {
                // vars
                val productIds = arrayListOf<String>()
                // products
                for (currentProductId in currentProductIds) {
                    // vars (product)
                    val product = PickProduct()
                    product.id = currentProductId.toInt()
                    product.quantity = 1.0
                    product.type = pickMode
                    productIds.add(currentProductId)
                    // add
                    updateProducts(product)
                }
                if (products.size >= 1) {
                    //PickFragment.sendResult(productIds)
                    view.pick_success.visibility = View.VISIBLE
                    view.pick_success_text.text = "Распознано " + productIds.size + " из " + rawResultSize
                    runDelayed(1000) { view.pick_success.visibility = View.GONE }
                }
                // fail
                else if (rawResultSize >= 1) {
                    view.pick_fail.visibility = View.VISIBLE
                    view.pick_fail_text.text = "Распознано " + productIds.size + " из " + rawResultSize
                    runDelayed(1000) { view.pick_fail.visibility = View.GONE }
                }
                // resume
                runDelayed(700) { capture.barcodeView.resume() }
            } else {
                // current product
                currentProductId = "0"
                if (currentProductIds.size > 0) currentProductId = currentProductIds[0]
                // view
                view.pick_window.visibility = View.VISIBLE
                view.pick_bg.visibility = View.VISIBLE
                view.pick_quantity.setText("")
                // mode
                if (pickMode == 1) view.pick_note.text = "Введите количество оприходуемых единиц (шт.)"
                if (pickMode == 2) view.pick_note.text = "Введите длину оприходуемой продукции (в погонных метрах)"
                if (pickMode == 3) view.pick_note.text = "Введите вес оприходуемой продукции (в кг)"
                if (pickMode == 4) view.pick_note.text = "Введите объем оприходуемой продукции (в метрах кубических)"
                if (pickMode == 1) view.pick_quantity.inputType = InputType.TYPE_CLASS_NUMBER
            }
        }

        private fun updateProducts(currentProduct: PickProduct): String {
            // vars
            var exist = false
            // parse
            for (i in 0 until products.size) {
                if (products[i].id == currentProduct.id) {
                    if (products[i].type == currentProduct.type) {
                        if (pickMode != 0) products[i].quantity += currentProduct.quantity
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
