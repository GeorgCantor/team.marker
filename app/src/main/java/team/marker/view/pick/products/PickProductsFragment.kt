package team.marker.view.pick.products

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.toolbar_history.*
import org.koin.android.ext.android.inject
import team.marker.R
import team.marker.util.Constants.PRODUCTS_URL
import team.marker.util.openFragment
import team.marker.view.product.ProductFragment

class PickProductsFragment : Fragment(R.layout.fragment_pick_products) {

    private val viewModel by inject<PickProductsViewModel>()
    private val productIds: String by lazy { arguments?.get("product_ids") as String }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getProducts(productIds)

        viewModel.progressIsVisible.observe(viewLifecycleOwner, { visible ->
            progress_bar.visibility = if (visible) View.VISIBLE else View.GONE
        })

        viewModel.response.observe(viewLifecycleOwner, {
            history_recycler.adapter = PickProductsAdapter(it.info ?: mutableListOf()) { item ->
                val bundle = Bundle()
                bundle.putString("product_url", PRODUCTS_URL + item.id?.toString())

                try {
                    findNavController().navigate(R.id.action_pickProductsFragment_to_productFragment, bundle)
                } catch (e: IllegalStateException) {
                    (requireActivity() as AppCompatActivity).openFragment(ProductFragment().apply {
                        arguments = bundle
                    })
                }
            }
        })

        btn_back.setOnClickListener { back() }
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.statusBarColor = getColor(requireContext(), R.color.dark_blue)
    }

    private fun back() {
        activity?.onBackPressed()
    }

}