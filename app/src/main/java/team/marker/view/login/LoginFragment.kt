package team.marker.view.login

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_login.*
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import team.marker.R
import team.marker.model.requests.LoginRequest
import team.marker.util.Constants.MAIN_STORAGE
import team.marker.util.Constants.SID
import team.marker.util.Constants.TOKEN
import team.marker.util.Constants.access_sid
import team.marker.util.Constants.access_token
import team.marker.util.isNetworkAvailable
import team.marker.util.putAny
import team.marker.util.showError

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val viewModel by inject<LoginViewModel>()
    private val preferences: SharedPreferences by inject(named(MAIN_STORAGE))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_login.setOnClickListener { apply() }

        viewModel.response.observe(viewLifecycleOwner, { response ->
            val sid = response?.sid
            val token = response?.token

            if (sid != null && token != null) {
                preferences.putAny(SID, sid)
                preferences.putAny(TOKEN, token)
                access_sid = sid
                access_token = token
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            }
        })

        viewModel.error.observe(viewLifecycleOwner, {
            context?.showError(error_login, getString(R.string.wrong_login_password))
        })
    }

    private fun apply() {
        if (context?.isNetworkAvailable() == false) {
            context?.showError(error_login, getString(R.string.internet_unavailable))
            return
        }

        val login = input_login.text.toString()
        val password = input_password.text.toString()
        if (login.isBlank()) {
            context?.showError(error_login, getString(R.string.input_login))
            return
        }
        if (password.isBlank()) {
            context?.showError(error_login, getString(R.string.input_password))
            return
        }

        viewModel.login(LoginRequest(login, password))
    }
}