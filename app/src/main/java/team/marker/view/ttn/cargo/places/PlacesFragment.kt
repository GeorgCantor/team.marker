package team.marker.view.ttn.cargo.places

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import team.marker.R
import team.marker.view.ttn.cargo.CargoPlacesViewModel

class PlacesFragment : Fragment(R.layout.fragment_places) {

    private val viewModel by sharedViewModel<CargoPlacesViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}