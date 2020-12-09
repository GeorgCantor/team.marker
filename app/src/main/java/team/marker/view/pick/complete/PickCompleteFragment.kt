package team.marker.view.pick.complete

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.barcode_capture.*
import kotlinx.android.synthetic.main.fragment_pick_complete.*
import kotlinx.android.synthetic.main.toolbar_common.*
import org.koin.android.ext.android.inject
import team.marker.R
import team.marker.model.requests.PickProduct
import team.marker.model.requests.PickRequest
import team.marker.util.nameCase
import team.marker.util.openFragment
import team.marker.view.pick.products.PickProductsFragment

class PickCompleteFragment : Fragment(R.layout.fragment_pick_complete) {

    private val viewModel by inject<PickCompleteViewModel>()
    private val products: ArrayList<PickProduct> by lazy { arguments?.get("products") as ArrayList<PickProduct> }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val size = products.size
        val labelScan = size.nameCase(resources.getStringArray(R.array.scan_array))
        val labelCode = size.nameCase(resources.getStringArray(R.array.codes_array))
        if (size > 0) note_text.text = "$labelScan $size $labelCode, ${getString(R.string.enter_email)}" else note_text.text = getString(R.string.no_scanned_codes)
        if (size == 0) {
            input_email.visibility = View.GONE
            btn_send.text = getString(R.string.close)
            note_title.setImageResource(R.drawable.ic_empty)
            btn_products.visibility = View.GONE
            ic_email.visibility = View.GONE
        }
        btn_back.setOnClickListener { activity?.onBackPressed() }
        btn_products.setOnClickListener { products() }
        btn_send.setOnClickListener { send(size) }
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.statusBarColor = getColor(requireContext(), R.color.dark_blue)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().pick_toolbar?.visibility = View.VISIBLE
        requireActivity().preview?.visibility = View.VISIBLE
        requireActivity().graphicOverlay?.visibility = View.VISIBLE
    }

    private fun products() {
        val bundle = Bundle()
        val productIds = arrayListOf<String>()
        for (product in products) productIds.add(product.id.toString())
        val productIdsStr = productIds.joinToString(",")
        bundle.putString("product_ids", productIdsStr)
        (requireActivity() as AppCompatActivity).openFragment(PickProductsFragment().apply {
            arguments = bundle
        })
    }

    private fun send(size: Int) {
        val email = input_email.text.toString()
        if (size > 0 && email.isEmpty()) return
        if (size > 0) viewModel.pick(PickRequest(products, email))
        findNavController().navigate(R.id.action_pickCompleteFragment_to_homeFragment)
    }

}