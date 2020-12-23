package team.marker.view

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavArgument
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import team.marker.R
import team.marker.util.Constants.MAIN_STORAGE
import team.marker.util.Constants.PRODUCT_URL
import team.marker.util.Constants.SID
import team.marker.util.Constants.TOKEN
import team.marker.util.Constants.access_sid
import team.marker.util.Constants.access_token
import team.marker.util.NetworkUtils.getNetworkLiveData
import team.marker.util.getAny
import team.marker.util.runDelayed
import team.marker.util.slideAnim

class MainActivity : AppCompatActivity() {

    private val preferences: SharedPreferences by inject(named(MAIN_STORAGE))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        access_sid = preferences.getAny("", SID) as String
        access_token = preferences.getAny("", TOKEN) as String

        val action = intent.action
        val url = intent.dataString
        val navHostFragment = navHostFragment as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_graph)

        if (access_token.isEmpty()) {
            graph.startDestination = R.id.loginFragment
        } else if (Intent.ACTION_VIEW == action && url != null) {
            graph.addArgument(PRODUCT_URL, NavArgument.Builder().setDefaultValue(url).build())
            graph.startDestination = R.id.productFragment
        } else {
            graph.startDestination = R.id.homeFragment
        }
        navHostFragment.navController.graph = graph

        getNetworkLiveData(applicationContext).observe(this) { isConnected ->
            if (isConnected) {
                no_internet_warning.slideAnim(root_layout, false)
            } else {
                5000L.runDelayed { no_internet_warning.slideAnim(root_layout, true) }
            }
        }
    }
}