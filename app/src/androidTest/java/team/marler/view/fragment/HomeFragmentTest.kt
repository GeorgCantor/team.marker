package team.marler.view.fragment

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.google.android.material.internal.ContextUtils.getActivity
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import team.marker.R
import team.marker.view.MainActivity
import team.marler.base.BaseAndroidTest

@RunWith(AndroidJUnit4ClassRunner::class)
class HomeFragmentTest : BaseAndroidTest() {

    @get: Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun logout_when_click_exit_yes() {
        if (isUserLoggedIn()) {
            onView(withId(R.id.btn_logout)).perform(click())
            onView(withText(R.string.logout_dialog_title))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
            onView(withId(android.R.id.button1)).perform((click()))

            onView(isRoot()).perform(waitFor(2000))
            onView(withId(R.id.input_login)).check(matches(isDisplayed()))
            onView(withId(R.id.btn_login)).check(matches(isDisplayed()))
            onView(withId(R.id.login_note)).check(matches(isDisplayed()))
            if (!isUserLoggedIn()) assert(true)
        }
    }

    @Test
    fun show_toast_if_click_scan_without_internet() {
        if (!isNetworkAvailable() && isUserLoggedIn()) {
            onView(withId(R.id.btn_scan)).perform(click())
            onView(withId(R.id.btn_pick)).perform(click())
            onView(withId(R.id.btn_breach)).perform(click())

            onView(withText(R.string.internet_unavailable))
                .inRoot(withDecorView(not(getActivity(getContext())?.window?.decorView)))
                .check(matches(isDisplayed()))
            onView(withId(R.id.btn_logout)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun open_scan_fragment() {
        if (isNetworkAvailable() && isUserLoggedIn()) {
            onView(withId(R.id.btn_scan)).perform(click())
            onView(isRoot()).perform(waitFor(1000))
            onView(withId(R.id.btn_scan_flash)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun open_pick_fragment() {
        if (isNetworkAvailable() && isUserLoggedIn()) {
            onView(withId(R.id.btn_pick)).perform(click())
            onView(isRoot()).perform(waitFor(1000))
            onView(withId(R.id.pick_toolbar)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun open_breach_fragment() {
        if (isNetworkAvailable() && isUserLoggedIn()) {
            onView(withId(R.id.btn_breach)).perform(click())
            onView(isRoot()).perform(waitFor(1000))
            onView(withId(R.id.sv_barcode)).check(matches(isDisplayed()))
        }
    }
}