package team.marker.view.pick.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_pick_settings.*
import kotlinx.android.synthetic.main.toolbar_product.*
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import team.marker.R
import team.marker.util.Constants.MAIN_STORAGE
import team.marker.util.Constants.MODE
import team.marker.util.getAny
import team.marker.util.putAny

class PickSettingsFragment : Fragment(R.layout.fragment_pick_settings) {

    private val preferences: SharedPreferences by inject(named(MAIN_STORAGE))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when(preferences.getAny(0, MODE) as Int) {
            0 -> ic_update_0.setColorFilter(getColor(requireContext(), R.color.blue_gray), android.graphics.PorterDuff.Mode.SRC_IN)
            1 -> ic_update_1.setColorFilter(getColor(requireContext(), R.color.blue_gray), android.graphics.PorterDuff.Mode.SRC_IN)
            2 -> ic_update_2.setColorFilter(getColor(requireContext(), R.color.blue_gray), android.graphics.PorterDuff.Mode.SRC_IN)
            3 -> ic_update_3.setColorFilter(getColor(requireContext(), R.color.blue_gray), android.graphics.PorterDuff.Mode.SRC_IN)
            4 -> ic_update_4.setColorFilter(getColor(requireContext(), R.color.blue_gray), android.graphics.PorterDuff.Mode.SRC_IN)
        }

        btn_update_0.setOnClickListener { update(0) }
        btn_update_1.setOnClickListener { update(1) }
        btn_update_2.setOnClickListener { update(2) }
        btn_update_3.setOnClickListener { update(3) }
        btn_update_4.setOnClickListener { update(4) }
        btn_back.setOnClickListener { activity?.onBackPressed() }
    }

    private fun update(mode: Int) {
        preferences.putAny(MODE, mode)
        findNavController().navigate(R.id.action_pickSettingsFragment_to_pickFragment)
    }
}