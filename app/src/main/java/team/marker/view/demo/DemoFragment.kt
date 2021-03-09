package team.marker.view.demo

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_demo.*
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import team.marker.R
import team.marker.util.Constants.DEMO
import team.marker.util.Constants.MAIN_STORAGE
import team.marker.util.Constants.SID
import team.marker.util.Constants.TOKEN
import team.marker.util.putAny

class DemoFragment : Fragment(R.layout.fragment_demo) {

    private val preferences: SharedPreferences by inject(named(MAIN_STORAGE))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_scan.setOnClickListener { findNavController().navigate(R.id.action_demoFragment_to_demoScanFragment) }

        btn_logout.setOnClickListener {
            with(preferences) {
                putAny(SID, "")
                putAny(TOKEN, "")
                putAny(DEMO, false)
            }
            findNavController().navigate(R.id.action_demoFragment_to_loginFragment)
        }
    }
}