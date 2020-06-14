package team.marker

import android.app.Application
import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import team.marker.view.*

class MyApplication : Application() {

    companion object {
        private var instance: MyApplication? = null
        fun appContext(): Context? = instance?.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        startKoin {
            androidContext(this@MyApplication)
            modules(arrayListOf(apiModule, viewModelModule, repositoryModule))
        }
    }
}