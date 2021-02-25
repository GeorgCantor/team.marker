package team.marker.view.pick.products

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_pick_products.*
import kotlinx.android.synthetic.main.toolbar_history.*
import org.koin.android.ext.android.inject
import team.marker.R
import team.marker.util.Constants.PARTNER
import team.marker.util.Constants.PARTNERS
import team.marker.util.Constants.PRODUCTS_URL
import team.marker.util.Constants.PRODUCT_IDS
import team.marker.util.Constants.PRODUCT_URL

class PickProductsFragment : Fragment(R.layout.fragment_pick_products) {

    private val viewModel by inject<PickProductsViewModel>()
    private val productIds: String by lazy { arguments?.get(PRODUCT_IDS) as String }
    private val partners: List<Pair<String, String?>>? by lazy { arguments?.get(PARTNERS) as List<Pair<String, String?>>? }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getProducts(productIds)

        viewModel.progressIsVisible.observe(viewLifecycleOwner) {
            progress_bar.isVisible = it
        }

        viewModel.response.observe(viewLifecycleOwner) {
            products_recycler.adapter = PickProductsAdapter(it.info ?: listOf()) { item ->
                findNavController().navigate(
                    R.id.action_pickProductsFragment_to_productFragment,
                    bundleOf(
                        PRODUCT_URL to "$PRODUCTS_URL${item.id?.toString()}",
                        PARTNER to partners?.find { it.first == item.id.toString() }?.second
                    )
                )
            }
            products_recycler.scheduleLayoutAnimation()
        }

        btn_back.setOnClickListener { activity?.onBackPressed() }
    }
}