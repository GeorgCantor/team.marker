package team.marker

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import team.marker.view.apiModule
import team.marker.view.repositoryModule
import team.marker.view.viewModelModule

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(listOf(apiModule, viewModelModule, repositoryModule))
        }
    }
}