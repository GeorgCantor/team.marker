package team.marker

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import team.marker.di.apiModule
import team.marker.di.preferenceModule
import team.marker.di.repositoryModule
import team.marker.di.viewModelModule

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(listOf(apiModule, viewModelModule, repositoryModule, preferenceModule))
        }
    }
}