package team.marker.view.ttn.cargo.products

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_cargo_places.*
import kotlinx.android.synthetic.main.fragment_products.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import team.marker.R
import team.marker.util.Constants.PARTNER
import team.marker.util.Constants.PRODUCT_ID
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
            products_recycler.setHasFixedSize(true)
            products_recycler.adapter = ProductsAdapter(it ?: listOf(), { product ->
                viewModel.addSelectedItem(product)
            }) { item ->
                val id = if (item.partnerProductId?.isNotBlank() == true) item.partnerProductId else item.id.toString()
                findNavController().navigate(
                    R.id.action_cargoPlacesFragment_to_productFragment,
                    bundleOf(PRODUCT_ID to id, PARTNER to item.partnerTitle)
                )
            }
            products_recycler.scheduleLayoutAnimation()
        }

        viewModel.buttonClickable.observe(viewLifecycleOwner) {
            btn_create.isClickable = it
            btn_create.setBackgroundColor(
                ContextCompat.getColor(requireContext(), if (it) R.color.dark_blue else R.color.gray)
            )
        }

        btn_create.setOnClickListener {
            viewModel.createProductPlace()
            requireActivity().view_pager.currentItem = 1
        }
    }
}