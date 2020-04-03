package team.marker

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import team.marker.view.appModule
import team.marker.view.repositoryModule
import team.marker.view.viewModelModule

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(arrayListOf(appModule, viewModelModule, repositoryModule))
        }
    }
}