package team.marker.view.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.android.ext.android.inject
import team.marker.R
import team.marker.util.PreferenceManager
import team.marker.view.MainActivity
import team.marker.view.pick.PickActivity

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel by inject<HomeViewModel>()
    private lateinit var prefManager: PreferenceManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = PreferenceManager(requireActivity())

        btn_scan.setOnClickListener { scan() }
        btn_pick.setOnClickListener { pick() }
        btn_breach.setOnClickListener { breach() }
        btn_logout.setOnClickListener { logout() }
        // geo
        getLocation()
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.statusBarColor = getColor(requireContext(), R.color.dark_blue)
    }

    private fun scan() {
        findNavController().navigate(R.id.action_homeFragment_to_scanFragment)
    }

    private fun pick() {
        startActivity(Intent(requireContext(), PickActivity::class.java))
    }

    private fun breach() {
        findNavController().navigate(R.id.action_homeFragment_to_breachFragment)
    }

    private fun logout() {
        // pref manager
        prefManager.saveString("sid", "")
        prefManager.saveString("token", "")
        viewModel.logout()
        // restart
        activity?.finish()
        startActivity(Intent(requireActivity(), MainActivity::class.java))
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
        } else {
            getLocationExecute()
        }
    }

    private fun getLocationExecute() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient
            .lastLocation
            .addOnSuccessListener(requireActivity()) { location ->
                if (location != null) {
                    val lat = location.latitude.toString()
                    val lng = location.longitude.toString()
                    prefManager.saveString("lat", lat)
                    prefManager.saveString("lng", lng)
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