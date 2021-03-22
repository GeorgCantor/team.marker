package team.marker.view.ttn.cargo.create

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat.getColor
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_create_ttn.*
import kotlinx.android.synthetic.main.toolbar_common.*
import team.marker.R
import team.marker.util.Constants.FORCE
import team.marker.util.ExpandList

class CreateTtnFragment : Fragment(R.layout.fragment_create_ttn) {

    private lateinit var inputs: List<EditText>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_text.text = getString(R.string.creation_ttn)

        val listInvoice = ExpandList(expand_invoice, exp_icon_invoice)
        val listTransport = ExpandList(expand_transport, exp_icon_transport)
        val listShipper = ExpandList(expand_shipper, exp_icon_shipper)
        val listConsignee = ExpandList(expand_consignee, exp_icon_consignee)
        val listPayer = ExpandList(expand_payer, exp_icon_payer)
        listInvoice.toggleList(FORCE)

        btn_back.setOnClickListener { activity?.onBackPressed() }
        item_invoice.setOnClickListener { listInvoice.toggleList() }
        item_transport.setOnClickListener { listTransport.toggleList() }
        item_shipper.setOnClickListener { listShipper.toggleList() }
        item_consignee.setOnClickListener { listConsignee.toggleList() }
        item_payer.setOnClickListener { listPayer.toggleList() }

        inputs = listOf(
            input_invoice, input_transport, input_shipper, input_consignee, input_payer
        ).onEach { it.doAfterTextChanged { checkAllFilled() } }
    }

    private fun checkAllFilled() {
        inputs.all { it.text.isNotBlank() }.apply {
            btn_send.isClickable = this
            btn_send.setBackgroundColor(getColor(requireContext(), if (this) R.color.dark_blue else R.color.gray))
        }
    }
}