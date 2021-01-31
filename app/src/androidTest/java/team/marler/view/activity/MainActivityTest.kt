package team.marler.view.activity

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import team.marker.R
import team.marker.view.MainActivity
import team.marler.base.BaseAndroidTest

@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityTest : BaseAndroidTest() {

    @get: Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun is_activity_in_view() {
        onView(withId(R.id.root_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun show_warning_if_internet_unavailable() {
        if (!isNetworkAvailable()) {
            onView(withId(R.id.no_internet_warning))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.internet_unavailable)))
        }
    }

    @Test
    fun grand_permission_and_dialog_dismiss() {
        grantPermission()
        onView(withId(R.id.root_layout)).check(matches(isDisplayed()))
    }
}