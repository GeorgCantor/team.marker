package team.marker.view.scan

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.toolbar_product.*
import team.marker.R

class ScanErrorFragment : Fragment(R.layout.fragment_scan_error) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                back(view)
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)

        btn_back.setOnClickListener { activity?.onBackPressed() }
    }

    private fun back(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_scanErrorFragment_to_homeFragment)
    }
}