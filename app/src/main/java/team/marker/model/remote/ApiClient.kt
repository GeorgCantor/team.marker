package team.marker.model.remote

import android.content.Context
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import team.marker.model.remote.interceptor.OfflineResponseCacheInterceptor
import team.marker.util.Constants.API_VERSION
import team.marker.util.Constants.APP_KEY
import team.marker.util.Constants.BASE_URL
import team.marker.util.Constants.access_sid
import team.marker.util.Constants.access_token
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiClient {

    fun create(context: Context): ApiService {

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE

        val interceptor: Interceptor = object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .addHeader("Accept", "application/json")
                    .addHeader("v", API_VERSION)
                    .addHeader("app-key", APP_KEY)
                    .addHeader("sid", access_sid)
                    .addHeader("token", access_token)
                    .build()
                return chain.proceed(request)
            }
        }

        val okHttpClient = OkHttpClient().newBuilder()
            .addInterceptor(OfflineResponseCacheInterceptor(context))
            .addInterceptor(loggingInterceptor)
            .addInterceptor(interceptor)
            .cache(Cache(File(context.cacheDir, "ResponsesCache"), (10 * 1024 * 1024).toLong()))
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        return retrofit.create(ApiService::class.java)
    }
}