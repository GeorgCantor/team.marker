package team.marker.view.ttn.cargo.create

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_create_ttn.*
import kotlinx.android.synthetic.main.toolbar_common.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import team.marker.R
import team.marker.util.Constants.FORCE
import team.marker.util.ExpandList

class CreateTtnFragment : Fragment(R.layout.fragment_create_ttn) {

    private val viewModel by sharedViewModel<CreateTtnViewModel>()
    private lateinit var inputs: List<EditText>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_text.text = getString(R.string.creation_ttn)

        val listInvoice = ExpandList(expand_invoice, exp_icon_invoice)
        val listTransport = ExpandList(expand_transport, exp_icon_transport)
        val listShipper = ExpandList(expand_shipper, exp_icon_shipper)
        listInvoice.toggleList(FORCE)

        btn_back.setOnClickListener { activity?.onBackPressed() }
        item_invoice.setOnClickListener { listInvoice.toggleList() }
        item_transport.setOnClickListener { listTransport.toggleList() }
        item_shipper.setOnClickListener { listShipper.toggleList() }

        inputs = listOf(
            input_freight_cost,
            input_shipping_cost,
            input_gross_weight
        ).onEach { it.doAfterTextChanged { checkAllFilled() } }

        val spinners = listOf(
            ts_spinner to R.array.ts_type_array,
            shipper_spinner to R.array.companies_array,
            customer_spinner to R.array.companies_array,
            carrier_spinner to R.array.companies_array,
            consignee_spinner to R.array.companies_array
        ).onEach { setSpinner(it.first, it.second) }

        viewModel.progressIsVisible.observe(viewLifecycleOwner) { progress_bar.isVisible = it }

        viewModel.showSuccessScreen.observe(viewLifecycleOwner) {
            if (it) findNavController().navigate(R.id.action_createTtnFragment_to_successFragment)
        }

        btn_send.setOnClickListener { viewModel.sendTtn() }
        btn_send.isClickable = false
    }

    private fun checkAllFilled() {
        inputs.all { it.text.isNotBlank() }.apply {
            btn_send.isClickable = this
            btn_send.setBackgroundColor(getColor(requireContext(), if (this) R.color.dark_blue else R.color.gray))
        }
    }

    private fun setSpinner(spinner: Spinner, resArray: Int) = ArrayAdapter.createFromResource(
        requireContext(),
        resArray,
        android.R.layout.simple_spinner_item
    ).also { adapter ->
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    override fun onDestroyView() {
        viewModel.showSuccessScreen.value = false
        viewModel.progressIsVisible.value = false
        super.onDestroyView()
    }
}