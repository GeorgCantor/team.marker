package team.marker.view.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import team.marker.R
import team.marker.util.PreferenceManager
import team.marker.view.MainActivity


class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var prefManager: PreferenceManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel { parametersOf() }
        prefManager = PreferenceManager(requireActivity())
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            activity?.window?.statusBarColor = resources.getColor(R.color.dark_gray)
        }
        //activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //view?.fitsSystemWindows = true
        //view?.requestFitSystemWindows();
        //accessToken = prefManager.getString(TOKEN) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // common
        super.onViewCreated(view, savedInstanceState)
        // buttons
        btn_scan.setOnClickListener { scan(view) }
        btn_pick.setOnClickListener { pick(view) }
        btn_breach.setOnClickListener { breach(view) }
        btn_logout.setOnClickListener { logout() }
        // geo
        getLocation()
    }

    private fun scan(view: View) {
        Navigation.findNavController(view).navigate(R.id.scanFragment)
    }

    private fun pick(view: View) {
        Navigation.findNavController(view).navigate(R.id.pickFragment)
    }

    private fun breach(view: View) {
        Navigation.findNavController(view).navigate(R.id.breachFragment)
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
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
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
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) { return }
        fusedLocationClient
            .lastLocation
            .addOnSuccessListener(requireActivity()) { location ->
                if (location != null) {
                    val lat = location.latitude.toString()
                    val lng = location.longitude.toString()
                    prefManager.saveString("lat", lat)
                    prefManager.saveString("lng", lng)
                    Log.e("Message", "$lat:$lng")
                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == 1) {
            getLocationExecute()
        }
    }

}