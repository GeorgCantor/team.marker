package team.marker.view.ttn.cargo.places

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_places.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import team.marker.R
import team.marker.view.ttn.cargo.CargoPlacesViewModel

class PlacesFragment : Fragment(R.layout.fragment_places) {

    private val viewModel by sharedViewModel<CargoPlacesViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.places.observe(viewLifecycleOwner) {
            empty_hint.isVisible = it.isNullOrEmpty()
            places_recycler.setHasFixedSize(true)
            places_recycler.adapter = PlacesAdapter(it) { place ->
                viewModel.selectedPlace.value = place
                findNavController().navigate(R.id.action_cargoPlacesFragment_to_placeListFragment)
            }
        }
    }
}