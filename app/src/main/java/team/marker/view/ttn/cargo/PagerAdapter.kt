package team.marker.view.ttn.cargo

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import team.marker.view.ttn.cargo.places.PlacesFragment
import team.marker.view.ttn.cargo.products.ProductsFragment

class PagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun createFragment(position: Int) = when (position) {
        0 -> ProductsFragment()
        else -> PlacesFragment()
    }

    override fun getItemCount() = 2
}