package team.marker.view.ttn.cargo.create

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_success.*
import kotlinx.android.synthetic.main.toolbar_common.*
import team.marker.R

class SuccessFragment : Fragment(R.layout.fragment_success) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_text.text = getString(R.string.ttn_sending)

        btn_back.setOnClickListener { activity?.onBackPressed() }
        btn_close.setOnClickListener { activity?.onBackPressed() }
    }
}