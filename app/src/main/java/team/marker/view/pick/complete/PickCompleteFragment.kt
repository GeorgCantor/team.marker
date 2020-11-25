package team.marker.view.pick.complete

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_pick_complete.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import team.marker.R
import team.marker.model.requests.PickProduct
import team.marker.model.requests.PickRequest
import team.marker.util.nameCase

class PickCompleteFragment : Fragment(R.layout.fragment_pick_complete) {

    private lateinit var viewModel: PickCompleteViewModel
    private val products: ArrayList<PickProduct> by lazy { arguments?.get("products") as ArrayList<PickProduct> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel { parametersOf() }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            activity?.window?.statusBarColor = resources.getColor(R.color.dark_gray)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        for(product in products) {
            Log.e("Message", product.id.toString() + " " + product.quantity + " " + product.type)
        }
        val size = products.size
        val labelScan = nameCase(size, arrayOf("Отсканирован", "Отсканировано", "Отсканировано"))
        val labelCode = nameCase(size, arrayOf("код", "кода", "кодов"))
        if (size > 0) note_text.text = "$labelScan $size $labelCode, введите электронную почту для отправки отчета."
        else note_text.text = "Отсканированных кодов нет. Выполните повторное сканирование перед отправкой отчета."
        if (size == 0) {
            input_email.visibility = View.GONE
            btn_send.text = "Закрыть"
            //btn_send.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_logout)
            note_title.setImageResource(R.drawable.ic_empty)
            btn_products.visibility = View.GONE
            ic_email.visibility = View.GONE
        }
        btn_products.setOnClickListener { products(view) }
        btn_send.setOnClickListener { send(view, size) }
    }

    private fun products(view: View) {
        val bundle = Bundle()
        val productIds = arrayListOf<String>()
        for(product in products) productIds.add(product.id.toString())
        val productIdsStr = productIds.joinToString(",")
        Log.e("productIdsStr", productIdsStr)
        bundle.putString("product_ids", productIdsStr)
        Navigation.findNavController(view).navigate(R.id.action_pickCompleteFragment_to_pickProductsFragment, bundle)
    }

    private fun send(view: View, size: Int) {
        val email = input_email.text.toString()
        if (size > 0 && email.isEmpty()) return
        if (size > 0) viewModel.pick(PickRequest(products, email))
        Navigation.findNavController(view).navigate(R.id.action_pickCompleteFragment_to_homeFragment)
    }

}