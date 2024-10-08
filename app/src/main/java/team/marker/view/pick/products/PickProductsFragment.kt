package team.marker.view.pick.products

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_pick_products.*
import kotlinx.android.synthetic.main.toolbar_common.*
import org.koin.android.ext.android.inject
import team.marker.R
import team.marker.util.Constants.PARTNER
import team.marker.util.Constants.PRODUCT_ID
import team.marker.util.Constants.PRODUCT_IDS

class PickProductsFragment : Fragment(R.layout.fragment_pick_products) {

    private val viewModel by inject<PickProductsViewModel>()
    private val productIds: String by lazy { arguments?.get(PRODUCT_IDS) as String }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_text.text = getString(R.string.product_ist)

        viewModel.getProducts(productIds)

        viewModel.progressIsVisible.observe(viewLifecycleOwner) {
            progress_bar.isVisible = it
        }

        viewModel.response.observe(viewLifecycleOwner) {
            products_recycler.adapter = PickProductsAdapter(it.info ?: listOf()) { item ->
                val id = if (item.partnerProductId?.isNotBlank() == true) item.partnerProductId else item.id.toString()

                findNavController().navigate(
                    R.id.action_pickProductsFragment_to_productFragment,
                    bundleOf(PRODUCT_ID to id, PARTNER to item.partnerTitle)
                )
            }
            products_recycler.scheduleLayoutAnimation()
        }

        btn_back.setOnClickListener { activity?.onBackPressed() }
    }
}