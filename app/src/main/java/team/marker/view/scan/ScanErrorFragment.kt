package team.marker.view.scan

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.toolbar_product.*
import team.marker.R

class ScanErrorFragment : Fragment(R.layout.fragment_scan_error) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_back.setOnClickListener { activity?.onBackPressed() }
    }
}