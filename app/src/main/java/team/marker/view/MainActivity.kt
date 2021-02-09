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
import team.marker.model.requests.PickRequest
import team.marker.util.Constants.DEFERRED_FILES
import team.marker.util.Constants.DEFERRED_REQUEST
import team.marker.util.Constants.MAIN_STORAGE
import team.marker.util.Constants.SID
import team.marker.util.Constants.TOKEN
import team.marker.util.Constants.accessSid
import team.marker.util.Constants.accessToken
import team.marker.util.NetworkUtils.getNetworkLiveData
import team.marker.util.getAny
import team.marker.util.longToast
import team.marker.util.observeOnce
import team.marker.view.breach.complete.BreachCompleteViewModel
import team.marker.view.pick.complete.PickCompleteViewModel

class MainActivity : AppCompatActivity() {

    private val breachViewModel by inject<BreachCompleteViewModel>()
    private val pickViewModel by inject<PickCompleteViewModel>()
    private val preferences: SharedPreferences by inject(named(MAIN_STORAGE))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.dark_blue)

        accessSid = preferences.getAny("", SID) as String
        accessToken = preferences.getAny("", TOKEN) as String

        val navHostFragment = navHostFragment as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_graph)

        graph.startDestination = if (accessToken.isEmpty()) R.id.loginFragment else R.id.homeFragment

        navHostFragment.navController.graph = graph

        getNetworkLiveData(this).observe(this) { connect ->
            no_internet_warning.isVisible = !connect
            if (connect) {
                val jsonFiles = preferences.getAny("", DEFERRED_FILES) as String
                if (jsonFiles.isNotBlank()) {
                    val type = object : TypeToken<DeferredFiles>() {}.type
                    val files = Gson().fromJson<DeferredFiles>(jsonFiles, type)
                    breachViewModel.sendDeferredFiles(files)
                }

                val jsonPick = preferences.getAny("", DEFERRED_REQUEST) as String
                if (jsonPick.isNotBlank()) {
                    val type = object : TypeToken<PickRequest>() {}.type
                    val request = Gson().fromJson<PickRequest>(jsonPick, type)
                    pickViewModel.pick(request)
                }
            }
        }

        breachViewModel.sentSuccess.observeOnce(this) {
            if (it) longToast(getString(R.string.breach_request_sent))
        }

        pickViewModel.sentSuccess.observeOnce(this) {
            if (it) longToast(getString(R.string.pick_request_sent))
        }
    }
}