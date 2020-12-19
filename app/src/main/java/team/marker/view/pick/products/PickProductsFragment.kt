package team.marker.view.pick.products

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.toolbar_history.*
import org.koin.android.ext.android.inject
import team.marker.R
import team.marker.util.Constants.PRODUCTS_URL
import team.marker.util.Constants.PRODUCT_IDS
import team.marker.util.Constants.PRODUCT_URL

class PickProductsFragment : Fragment(R.layout.fragment_pick_products) {

    private val viewModel by inject<PickProductsViewModel>()
    private val productIds: String by lazy { arguments?.get(PRODUCT_IDS) as String }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getProducts(productIds)

        viewModel.progressIsVisible.observe(viewLifecycleOwner, { visible ->
            progress_bar.visibility = if (visible) View.VISIBLE else View.GONE
        })

        viewModel.response.observe(viewLifecycleOwner, {
            history_recycler.adapter = PickProductsAdapter(it.info ?: mutableListOf()) { item ->
                findNavController().navigate(
                    R.id.action_pickProductsFragment_to_productFragment,
                    bundleOf(PRODUCT_URL to "$PRODUCTS_URL${item.id?.toString()}")
                )
            }
        })

        btn_back.setOnClickListener { activity?.onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.statusBarColor = getColor(requireContext(), R.color.dark_blue)
    }
}