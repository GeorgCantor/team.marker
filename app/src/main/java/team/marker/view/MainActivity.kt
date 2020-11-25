package team.marker.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavArgument
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.activity_main.*
import team.marker.R
import team.marker.util.Constants.access_sid
import team.marker.util.Constants.access_token
import team.marker.util.PreferenceManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // common
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // vars (access)
        access_sid = PreferenceManager(this).getString("sid") ?: ""
        access_token = PreferenceManager(this).getString("token") ?: ""
        // vars (intent)
        val action = intent.action
        val url = intent.dataString
        // navigation
        val navHostFragment = navHostFragment as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_graph)
        // destination
        if (access_token.isEmpty()) {
            graph.startDestination = R.id.loginFragment
        } else if (Intent.ACTION_VIEW == action && url != null) {
            graph.addArgument("product_url", NavArgument.Builder().setDefaultValue(url).build())
            graph.startDestination = R.id.productFragment
        } else {
            graph.startDestination = R.id.homeFragment
        }
        // graph
        navHostFragment.navController.graph = graph
    }

}