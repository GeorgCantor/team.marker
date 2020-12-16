package team.marker.view.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_login.*
import org.koin.android.ext.android.inject
import team.marker.R
import team.marker.model.requests.LoginRequest
import team.marker.util.Constants.SID
import team.marker.util.Constants.TOKEN
import team.marker.util.Constants.access_sid
import team.marker.util.Constants.access_token
import team.marker.util.PreferenceManager
import team.marker.util.showError

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val viewModel by inject<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.statusBarColor = resources.getColor(R.color.dark_blue)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // buttons
        btn_login.setOnClickListener { apply() }
        // success
        viewModel.response.observe(viewLifecycleOwner, { response ->
            // vars
            val sid = response?.sid
            val token = response?.token
            // update
            if (sid != null && token != null) {
                with(PreferenceManager(requireActivity())) {
                    saveString(SID, sid)
                    saveString(TOKEN, token)
                }
                access_sid = sid
                access_token = token
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            }
        })
        // error
        viewModel.error.observe(viewLifecycleOwner, {
            context?.showError(error_login, getString(R.string.wrong_login_password), 5000)
        })
    }

    private fun apply() {
        // vars
        val login: String = input_login.text.toString()
        val password: String = input_password.text.toString()
        // request
        viewModel.login(LoginRequest(login, password))
    }
}