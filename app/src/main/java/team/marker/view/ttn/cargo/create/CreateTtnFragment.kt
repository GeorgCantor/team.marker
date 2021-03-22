package team.marker.view.ttn.cargo.create

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.toolbar_common.*
import team.marker.R

class CreateTtnFragment : Fragment(R.layout.fragment_create_ttn) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_text.text = getString(R.string.creation_ttn)
    }
}