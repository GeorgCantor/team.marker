package team.marker.view.home

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.transition.TransitionManager.beginDelayedTransition
import android.view.View
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import team.marker.R
import team.marker.util.Constants.LATITUDE
import team.marker.util.Constants.LONGITUDE
import team.marker.util.Constants.MAIN_STORAGE
import team.marker.util.Constants.SID
import team.marker.util.Constants.TOKEN
import team.marker.util.customDialog
import team.marker.util.getTransform
import team.marker.util.putAny
import team.marker.view.MainActivity

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel by inject<HomeViewModel>()
    private val preferences: SharedPreferences by inject(named(MAIN_STORAGE))
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.buttons.observe(viewLifecycleOwner) {
            buttons_recycler.setHasFixedSize(true)
            buttons_recycler.adapter = ButtonsAdapter(it) { position ->
                findNavController().navigate(when (position) {
                    0 -> R.id.action_homeFragment_to_scannFragment
                    1 -> R.id.action_homeFragment_to_pickFragment
                    2 -> R.id.action_homeFragment_to_ttnScanFragment
                    else -> R.id.action_homeFragment_to_breachFragment
                })
            }
        }

        btn_logout.setOnClickListener {
            val dialogView = requireContext().customDialog(
                getString(R.string.logout_title), getString(R.string.no), getString(R.string.yes),
                { view, root -> beginDelayedTransition(root, view.getTransform(line)) }, { logout() }
            )
            beginDelayedTransition(root_layout, line.getTransform(dialogView))
        }

        getLocation()
    }

    private fun logout() {
        preferences.putAny(SID, "")
        preferences.putAny(TOKEN, "")
        viewModel.logout()
        activity?.finish()
        startActivity(Intent(requireActivity(), MainActivity::class.java))
    }

    private fun getLocation() {
        if (checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION) != PERMISSION_GRANTED ||
            checkSelfPermission(requireContext(), ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION), 1)
        } else {
            getLocationExecute()
        }
    }

    private fun getLocationExecute() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
            checkSelfPermission(requireContext(), ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            location?.let {
                preferences.putAny(LATITUDE, it.latitude.toString())
                preferences.putAny(LONGITUDE, it.longitude.toString())
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED && requestCode == 1) {
            getLocationExecute()
        }
    }
}