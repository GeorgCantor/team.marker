package team.marker.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import team.marker.util.Constants.MAIN_STORAGE

class PreferenceManager(activity: Activity) {

    private val prefs: SharedPreferences = activity.getSharedPreferences(MAIN_STORAGE, Context.MODE_PRIVATE)

    fun getString(key: String): String? = prefs.getString(key, "")
    fun saveString(key: String, value: String) = prefs.edit().putString(key, value).apply()

    fun getInt(key: String): Int? = prefs.getInt(key,0)
    fun saveInt(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
}