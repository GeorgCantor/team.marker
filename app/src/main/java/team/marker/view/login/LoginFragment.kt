package team.marker.view.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_login.*
import team.marker.R
import team.marker.model.requests.LoginRequest
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import team.marker.util.Constants.access_sid
import team.marker.util.Constants.access_token
import team.marker.util.PreferenceManager
import team.marker.util.show_error

class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel { parametersOf() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_login.setOnClickListener { apply() }
        // success
        viewModel.response.observe(viewLifecycleOwner, Observer { response ->
            // vars
            val sid = response?.sid
            val token = response?.token
            // update
            if (sid != null && token != null) {
                PreferenceManager(requireActivity()).saveInt("sid", sid)
                PreferenceManager(requireActivity()).saveString("token", token)
                access_sid = sid
                access_token = token
                Navigation.findNavController(view).navigate(R.id.homeFragment)
            }
        })
        // error
        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            // vars
            val msg = error?.error_msg
            // update
            //Log.e("Message", msg)
            show_error(context, error_login, "неправильный логин или пароль", 5000, 0)
            error_login.text = "неправильный логин или пароль"
        })
    }

    private fun apply() {
        /*if (!loginIsReady) {
            showError(context, error_tv, getString(R.string.accept_conditions_error), 5000, 0)
            return
        }*/

        val login: String = input_login.text.toString()
        val password: String = input_password.text.toString()
        Log.e("Message", "$login, $password");

        viewModel.login(LoginRequest(login, password))
    }
}