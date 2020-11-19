package team.marker.view.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_login.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import team.marker.R
import team.marker.model.requests.LoginRequest
import team.marker.util.Constants.access_sid
import team.marker.util.Constants.access_token
import team.marker.util.PreferenceManager
import team.marker.util.showError

class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel { parametersOf() }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            activity?.window?.statusBarColor = resources.getColor(R.color.dark_gray)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // buttons
        btn_login.setOnClickListener { apply() }
        // success
        viewModel.response.observe(viewLifecycleOwner, Observer { response ->
            // vars
            val sid = response?.sid
            val token = response?.token
            // update
            if (sid != null && token != null) {
                PreferenceManager(requireActivity()).saveString("sid", sid)
                PreferenceManager(requireActivity()).saveString("token", token)
                access_sid = sid
                access_token = token
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_homeFragment)
            }
        })
        // error
        viewModel.error.observe(viewLifecycleOwner, Observer {
            showError(context, error_login, "неправильный логин или пароль", 5000, 0)
        })
    }

    override fun onResume() {
        super.onResume()
        // video
        /*val uri: Uri = Uri.parse("android.resource://team.marker/" + R.raw.bg_login)
        videoView.setVideoURI(uri)
        videoView.start()
        // prepare
        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            mediaPlayer.isLooping = true
            mediaPlayer.setScreenOnWhilePlaying(false)
        }*/
    }

    private fun apply() {
        // vars
        val login: String = input_login.text.toString()
        val password: String = input_password.text.toString()
        // request
        viewModel.login(LoginRequest(login, password))
        /*if (!loginIsReady) {
            showError(context, error_tv, getString(R.string.accept_conditions_error), 5000, 0)
            return
        }*/
        //Log.e("Message", "$login, $password");
    }
}