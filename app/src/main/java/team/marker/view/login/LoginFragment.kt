package team.marker.view.login

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_login.*
import org.koin.android.ext.android.inject
import team.marker.R
import team.marker.model.requests.LoginRequest
import team.marker.util.isNetworkAvailable
import team.marker.util.showError

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val viewModel by inject<LoginViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_login.setOnClickListener { login() }

        viewModel.loginSuccess.observe(viewLifecycleOwner) {
            if (it) findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }

        viewModel.error.observe(viewLifecycleOwner) {
            context?.showError(error_login, getString(R.string.wrong_login_password))
        }

        input_password.setOnFocusChangeListener { _, hasFocus ->
            ic_password.isVisible = !hasFocus
        }
    }

    private fun login() {
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