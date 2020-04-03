package team.marker.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
        access_sid = PreferenceManager(this).getInt("sid") ?: 0
        access_token = PreferenceManager(this).getString("token") ?: ""
        // navigation
        val navHostFragment = navHostFragment as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_graph)
        graph.startDestination = if (access_token.isEmpty()) R.id.loginFragment else R.id.homeFragment
        navHostFragment.navController.graph = graph
    }

}