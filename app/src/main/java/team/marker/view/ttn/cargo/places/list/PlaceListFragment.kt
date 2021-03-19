package team.marker.view.ttn.cargo.places.list

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_place_list.*
import kotlinx.android.synthetic.main.toolbar_history.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import team.marker.R
import team.marker.util.Constants.PARTNER
import team.marker.util.Constants.PRODUCT_ID
import team.marker.view.ttn.cargo.CargoPlacesViewModel

class PlaceListFragment : Fragment(R.layout.fragment_place_list) {

    private val viewModel by sharedViewModel<CargoPlacesViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.selectedPlace.observe(viewLifecycleOwner) {
            products_recycler.setHasFixedSize(true)
            products_recycler.adapter = PlaceListAdapter(it.products) { item ->
                val id = if (item.partnerProductId?.isNotBlank() == true) item.partnerProductId else item.id.toString()
                findNavController().navigate(
                    R.id.action_placeListFragment_to_productFragment,
                    bundleOf(PRODUCT_ID to id, PARTNER to item.partnerTitle)
                )
            }
        }

        btn_back.setOnClickListener { activity?.onBackPressed() }
    }
}