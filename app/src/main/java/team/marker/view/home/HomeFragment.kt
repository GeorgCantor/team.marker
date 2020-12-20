package team.marker.view.home

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import team.marker.R
import team.marker.util.Constants
import team.marker.util.Constants.SID
import team.marker.util.Constants.TOKEN
import team.marker.util.putAny
import team.marker.view.MainActivity

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel by inject<HomeViewModel>()
    private val preferences: SharedPreferences by inject(named(Constants.MAIN_STORAGE))
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_scan.setOnClickListener { scan() }
        btn_pick.setOnClickListener { pick() }
        btn_breach.setOnClickListener { breach() }
        btn_logout.setOnClickListener { logout() }

        getLocation()
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.statusBarColor = getColor(requireContext(), R.color.dark_blue)
    }

    private fun scan() {
        findNavController().navigate(R.id.action_homeFragment_to_scannFragment)
    }

    private fun pick() {
        findNavController().navigate(R.id.action_homeFragment_to_pickFragment)
    }

    private fun breach() {
        findNavController().navigate(R.id.action_homeFragment_to_breachFragment)
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
            checkSelfPermission(requireContext(), ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient
            .lastLocation
            .addOnSuccessListener(requireActivity()) { location ->
                if (location != null) {
                    val lat = location.latitude.toString()
                    val lng = location.longitude.toString()
                    preferences.putAny("lat", lat)
                    preferences.putAny("lng", lng)
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