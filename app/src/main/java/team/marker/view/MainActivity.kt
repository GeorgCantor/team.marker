package team.marker.view

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import team.marker.R
import team.marker.model.DeferredFiles
import team.marker.util.Constants.DEFERRED_FILES
import team.marker.util.Constants.MAIN_STORAGE
import team.marker.util.Constants.SID
import team.marker.util.Constants.TOKEN
import team.marker.util.Constants.access_sid
import team.marker.util.Constants.access_token
import team.marker.util.NetworkUtils.getNetworkLiveData
import team.marker.util.getAny
import team.marker.view.breach.complete.BreachCompleteViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel by inject<BreachCompleteViewModel>()
    private val preferences: SharedPreferences by inject(named(MAIN_STORAGE))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.dark_blue)

        access_sid = preferences.getAny("", SID) as String
        access_token = preferences.getAny("", TOKEN) as String

        val navHostFragment = navHostFragment as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_graph)

        graph.startDestination = if (access_token.isEmpty()) R.id.loginFragment else R.id.homeFragment

        navHostFragment.navController.graph = graph

        getNetworkLiveData(applicationContext).observe(this) {
            no_internet_warning.isVisible = !it
            if (it) {
                val json = preferences.getAny("", DEFERRED_FILES) as String
                if (json.isNotBlank()) {
                    val type = object : TypeToken<DeferredFiles>() {}.type
                    val gson = Gson()
                    val files = gson.fromJson<DeferredFiles>(json, type)
                    viewModel.sendDeferredFiles(files)
                }
            }
        }
    }
}