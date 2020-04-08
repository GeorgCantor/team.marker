package team.marker.view.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import team.marker.R
import team.marker.util.PreferenceManager
import team.marker.view.MainActivity

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel { parametersOf() }
        prefManager = PreferenceManager(requireActivity())
        //accessToken = prefManager.getString(TOKEN) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // common
        super.onViewCreated(view, savedInstanceState)
        // buttons
        btn_scan.setOnClickListener { scan(view) }
        btn_history.setOnClickListener { history(view) }
        btn_logout.setOnClickListener { logout() }
    }

    private fun scan(view: View) {
        Navigation.findNavController(view).navigate(R.id.scanFragment)
    }

    private fun history(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_historyFragment)
    }

    private fun logout() {
        // pref manager
        prefManager.saveInt("sid", 0)
        prefManager.saveString("token", "")
        viewModel.logout()
        // restart
        activity?.finish()
        startActivity(Intent(requireActivity(), MainActivity::class.java))
    }

}