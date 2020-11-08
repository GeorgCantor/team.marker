package team.marker.view.pick.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_pick_settings.*
import kotlinx.android.synthetic.main.toolbar_product.*
import team.marker.R
import team.marker.util.Constants
import team.marker.util.PreferenceManager

class PickSettingsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            activity?.window?.statusBarColor = resources.getColor(R.color.dark_gray)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pick_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // mode
        val mode = PreferenceManager(requireActivity()).getInt("mode") ?: 0
        if (mode == 0) ic_update_0.setColorFilter(ContextCompat.getColor(requireContext(), R.color.blue_gray), android.graphics.PorterDuff.Mode.SRC_IN);
        if (mode == 1) ic_update_1.setColorFilter(ContextCompat.getColor(requireContext(), R.color.blue_gray), android.graphics.PorterDuff.Mode.SRC_IN);
        if (mode == 2) ic_update_2.setColorFilter(ContextCompat.getColor(requireContext(), R.color.blue_gray), android.graphics.PorterDuff.Mode.SRC_IN);
        if (mode == 3) ic_update_3.setColorFilter(ContextCompat.getColor(requireContext(), R.color.blue_gray), android.graphics.PorterDuff.Mode.SRC_IN);
        if (mode == 4) ic_update_4.setColorFilter(ContextCompat.getColor(requireContext(), R.color.blue_gray), android.graphics.PorterDuff.Mode.SRC_IN);
        // listeners
        btn_update_0.setOnClickListener { update(view, 0) }
        btn_update_1.setOnClickListener { update(view, 1) }
        btn_update_2.setOnClickListener { update(view, 2) }
        btn_update_3.setOnClickListener { update(view, 3) }
        btn_update_4.setOnClickListener { update(view, 4) }
        btn_back.setOnClickListener { back(view) }
    }

    private fun update(view: View, mode: Int) {
        PreferenceManager(requireActivity()).saveInt("mode", mode)
        Navigation.findNavController(view).navigate(R.id.action_pickSettingsFragment_to_pickFragment)
    }

    private fun back(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_pickSettingsFragment_to_pickFragment)
    }

}