package team.marker.view.ttn.cargo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_cargo_places.*
import kotlinx.android.synthetic.main.toolbar_common.*
import team.marker.R
import team.marker.view.ttn.cargo.pager.PagerAdapter

class CargoPlacesFragment : Fragment(R.layout.fragment_cargo_places) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_text.text = getString(R.string.cargo_places)

        btn_back.setOnClickListener { activity?.onBackPressed() }

        val titles = arrayOf(getString(R.string.products), getString(R.string.places))

        view_pager.adapter = PagerAdapter(requireActivity())
        TabLayoutMediator(tab_layout, view_pager) { tab, position ->
            tab.text = titles[position]
            view_pager.setCurrentItem(tab.position, true)
        }.attach()
    }
}