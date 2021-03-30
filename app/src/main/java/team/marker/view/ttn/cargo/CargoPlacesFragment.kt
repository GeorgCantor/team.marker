package team.marker.view.ttn.cargo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_cargo_places.*
import kotlinx.android.synthetic.main.toolbar_common.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import team.marker.R

class CargoPlacesFragment : Fragment(R.layout.fragment_cargo_places) {

    private val viewModel by sharedViewModel<CargoPlacesViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_text.text = getString(R.string.cargo_places)

        btn_back.setOnClickListener { activity?.onBackPressed() }

        val titles = arrayOf(getString(R.string.products), getString(R.string.places))

        view_pager.adapter = PagerAdapter(requireActivity())
        view_pager.isUserInputEnabled = false
        TabLayoutMediator(tab_layout, view_pager) { tab, position ->
            tab.text = titles[position]
            view_pager.setCurrentItem(tab.position, true)
        }.attach()
    }

    override fun onDetach() {
        viewModel.clearAll()
        super.onDetach()
    }
}