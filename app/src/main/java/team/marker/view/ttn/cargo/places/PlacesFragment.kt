package team.marker.view.ttn.cargo.places

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_places.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import team.marker.R
import team.marker.model.responses.Product
import team.marker.model.ttn.ProductPlace
import team.marker.util.SwipeToDeleteCallback
import team.marker.view.ttn.cargo.CargoPlacesViewModel

class PlacesFragment : Fragment(R.layout.fragment_places) {

    private val viewModel by sharedViewModel<CargoPlacesViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.places.observe(viewLifecycleOwner) {
            val list = mutableListOf<ProductPlace>()
            it.forEach {
                val prods = mutableListOf<Product>()
                it.products.forEach { if (viewModel.products.value?.contains(it) == false) prods.add(it) }
                if (prods.isNotEmpty()) list.add(ProductPlace(prods))
            }
            viewModel.setPlaces(list)

            viewModel.nextClickable.value = list.isNotEmpty()
            empty_hint.isVisible = list.isEmpty()
            places_recycler.setHasFixedSize(true)
            places_recycler.adapter = PlacesAdapter(list) { place ->
                viewModel.selectedPlace.value = place
                findNavController().navigate(R.id.action_cargoPlacesFragment_to_placeListFragment)
            }
        }

        viewModel.nextClickable.observe(viewLifecycleOwner) {
            btn_further.isClickable = it
            btn_further.setBackgroundColor(
                ContextCompat.getColor(requireContext(), if (it) R.color.dark_blue else R.color.gray)
            )
        }

        val callback: SwipeToDeleteCallback = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                viewModel.removePlace(viewHolder.adapterPosition)
            }
        }

        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(places_recycler)
    }
}