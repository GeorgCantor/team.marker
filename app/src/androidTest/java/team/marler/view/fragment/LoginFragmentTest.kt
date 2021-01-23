package team.marler.view.fragment

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
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
class LoginFragmentTest : BaseAndroidTest() {

    @get: Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun show_error_when_enter_without_internet() {
        if (!isNetworkAvailable() && !isUserLoggedIn()) {
            onView(withId(R.id.btn_login)).perform(click())
            onView(withId(R.id.error_login))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.internet_unavailable)))
        }
    }

    @Test
    fun show_input_login_when_click_without_login() {
        if (isNetworkAvailable() && !isUserLoggedIn()) {
            onView(withId(R.id.input_login))
                .perform(replaceText(" "))
            onView(withId(R.id.btn_login)).perform(click())
            onView(withId(R.id.error_login))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.input_login)))
        }
    }

    @Test
    fun show_input_password_when_click_without_password() {
        if (isNetworkAvailable() && !isUserLoggedIn()) {
            onView(withId(R.id.input_login))
                .perform(replaceText("login"))
            onView(withId(R.id.input_password))
                .check(matches(withText("")))
            onView(withId(R.id.btn_login)).perform(click())
            onView(withId(R.id.error_login))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.input_password)))
        }
    }
}