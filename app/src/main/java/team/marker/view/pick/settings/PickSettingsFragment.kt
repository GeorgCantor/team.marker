package team.marker.view.pick.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_pick_settings.*
import kotlinx.android.synthetic.main.toolbar_product.*
import team.marker.R
import team.marker.util.PreferenceManager
import team.marker.view.pick.PickActivity

class PickSettingsFragment : Fragment(R.layout.fragment_pick_settings) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            activity?.window?.statusBarColor = resources.getColor(R.color.dark_blue)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                back()
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)

        // mode
        when(PreferenceManager(requireActivity()).getInt("mode") ?: 0) {
            0 -> ic_update_0.setColorFilter(getColor(requireContext(), R.color.blue_gray), android.graphics.PorterDuff.Mode.SRC_IN)
            1 -> ic_update_1.setColorFilter(getColor(requireContext(), R.color.blue_gray), android.graphics.PorterDuff.Mode.SRC_IN)
            2 -> ic_update_2.setColorFilter(getColor(requireContext(), R.color.blue_gray), android.graphics.PorterDuff.Mode.SRC_IN)
            3 -> ic_update_3.setColorFilter(getColor(requireContext(), R.color.blue_gray), android.graphics.PorterDuff.Mode.SRC_IN)
            4 -> ic_update_4.setColorFilter(getColor(requireContext(), R.color.blue_gray), android.graphics.PorterDuff.Mode.SRC_IN)
        }
        // listeners
        btn_update_0.setOnClickListener { update(0) }
        btn_update_1.setOnClickListener { update(1) }
        btn_update_2.setOnClickListener { update(2) }
        btn_update_3.setOnClickListener { update(3) }
        btn_update_4.setOnClickListener { update(4) }
        btn_back.setOnClickListener { back() }
    }

    private fun update(mode: Int) {
        PreferenceManager(requireActivity()).saveInt("mode", mode)
        back()
    }

    private fun back() {
        val intent = Intent(requireContext(), PickActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

}