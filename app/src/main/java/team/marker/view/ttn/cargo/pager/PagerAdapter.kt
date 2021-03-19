package team.marker.view.ttn.cargo.pager

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun createFragment(position: Int) = when (position) {
        0 -> ProductsFragment()
        else -> PlacesFragment()
    }

    override fun getItemCount() = 2
}