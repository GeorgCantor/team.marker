package team.marker.view.ttn.cargo.products

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_products.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import team.marker.R
import team.marker.view.ttn.cargo.CargoPlacesViewModel

class ProductsFragment : Fragment(R.layout.fragment_products) {

    private val viewModel by sharedViewModel<CargoPlacesViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getProducts()

        viewModel.progressIsVisible.observe(viewLifecycleOwner) {
            progress_bar.isVisible = it
        }

        viewModel.products.observe(viewLifecycleOwner) {
            products_recycler.adapter = ProductsAdapter(it.info ?: listOf()) { item ->
                val id = if (item.partnerProductId?.isNotBlank() == true) item.partnerProductId else item.id.toString()

//                findNavController().navigate(
//                    R.id.action_pickProductsFragment_to_productFragment,
//                    bundleOf(Constants.PRODUCT_ID to id, Constants.PARTNER to item.partnerTitle)
//                )
            }
            products_recycler.scheduleLayoutAnimation()
        }
    }
}