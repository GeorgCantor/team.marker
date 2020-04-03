package team.marker.view.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import kotlinx.android.synthetic.main.fragment_home.*
import team.marker.R

class HistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_history, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*login_button.setOnClickListener {
            findNavController(this).navigate(R.id.action_introFragment_to_authPhoneFragment)
        }

        signup_button.setOnClickListener {
            findNavController(this).navigate(R.id.action_introFragment_to_registrationFragment)
        }*/
    }
}