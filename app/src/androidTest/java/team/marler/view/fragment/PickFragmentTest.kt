package team.marler.view.fragment

import android.content.Context.MODE_PRIVATE
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import junit.framework.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import team.marker.R
import team.marker.util.Constants.MAIN_STORAGE
import team.marker.util.Constants.MODE
import team.marker.util.getAny
import team.marker.view.MainActivity
import team.marler.base.BaseAndroidTest

@RunWith(AndroidJUnit4ClassRunner::class)
class PickFragmentTest : BaseAndroidTest() {

    @get: Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun open_settings_set_mode_1_check_is_saved() {
        if (isNetworkAvailable() && isUserLoggedIn()) {
            onView(withId(R.id.btn_pick)).perform(click())
            onView(withId(R.id.btn_settings)).perform(click())
            onView(withId(R.id.btn_update_1)).perform(click())
            val mode = getContext().getSharedPreferences(MAIN_STORAGE, MODE_PRIVATE).getAny(0, MODE) as Int
            assertTrue(mode == 1)
        }
    }
}