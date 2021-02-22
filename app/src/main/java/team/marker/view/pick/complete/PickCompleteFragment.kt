package team.marker.view.pick.complete

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_pick_complete.*
import kotlinx.android.synthetic.main.toolbar_common.*
import org.koin.android.ext.android.inject
import team.marker.R
import team.marker.model.requests.PickProduct
import team.marker.model.requests.PickRequest
import team.marker.util.*
import team.marker.util.Constants.PRODUCTS
import team.marker.util.Constants.PRODUCT_IDS

class PickCompleteFragment : Fragment(R.layout.fragment_pick_complete) {

    private val viewModel by inject<PickCompleteViewModel>()
    private val products: MutableList<PickProduct> by lazy { arguments?.get(PRODUCTS) as MutableList<PickProduct> }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_text.text = getString(R.string.sending_report)

        val size = products.size
        val labelScan = size.nameCase(resources.getStringArray(R.array.scan_array))
        val labelCode = size.nameCase(resources.getStringArray(R.array.codes_array))
        when (size) {
            0 -> {
                note_text.text = getString(R.string.no_scanned_codes)
                btn_send.text = getString(R.string.close)
                note_title.setImageResource(R.drawable.ic_empty)
                input_email.gone()
                btn_products.gone()
                ic_email.gone()
            }
            else -> note_text.text = "$labelScan $size $labelCode, ${getString(R.string.enter_email)}"
        }

        viewModel.error.observe(viewLifecycleOwner) { it?.let { context?.longToast(it) } }

        viewModel.sentSuccess.observe(viewLifecycleOwner) { if (it) activity?.onBackPressed() }

        viewModel.progressIsVisible.observe(viewLifecycleOwner) { progress_bar.isVisible = it }

        btn_back.setOnClickListener { activity?.onBackPressed() }
        btn_products.setOnClickListener { products() }
        btn_send.setOnClickListener { send(size) }

        input_email.doOnTextChanged { text, _, _, _ ->
            when (text?.isBlank()) {
                true -> email_input_view.error = getString(R.string.enter_email_warning)
                false -> email_input_view.error = null
            }
        }
    }

    override fun onResume() {
        super.onResume()
        email_input_view.error = null
    }

    override fun onDetach() {
        viewModel.sentSuccess.value = false
        super.onDetach()
    }

    private fun products() {
        if (!requireContext().hasInternetBeforeAction()) return
        val productIds = arrayListOf<String>()
        for (product in products) productIds.add(product.id.toString())
        val productIdsStr = productIds.joinToString(",")
        findNavController().navigate(
            R.id.action_pickCompleteFragment_to_pickProductsFragment,
            bundleOf(PRODUCT_IDS to productIdsStr)
        )
    }

    private fun send(size: Int) {
        if (size > 0) {
            val email = input_email.text.toString()
            if (email.isEmpty()) {
                email_input_view.error = getString(R.string.enter_email_warning)
                return
            }
            when (context?.isNetworkAvailable()) {
                true -> viewModel.pick(PickRequest(products, email))
                false -> {
                    viewModel.saveForDeferredSending(PickRequest(products, email))
                    context?.longToast(getString(R.string.send_later))
                    activity?.onBackPressed()
                }
            }
        } else {
            activity?.onBackPressed()
        }
    }
}