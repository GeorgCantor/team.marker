package team.marler.view.fragment

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import team.marker.R
import team.marker.util.Constants.PRODUCT_IDS
import team.marker.view.breach.complete.BreachCompleteFragment
import team.marler.base.BaseAndroidTest

@RunWith(AndroidJUnit4ClassRunner::class)
class BreachCompleteFragmentTest : BaseAndroidTest() {

    @Before
    fun setup() {
        val bundle = bundleOf(PRODUCT_IDS to arrayListOf("id"))
        launchFragmentInContainer<BreachCompleteFragment>(
            themeResId = R.style.AppTheme,
            fragmentArgs = bundle
        )
    }

    @Test
    fun click_send_with_empty_comment() {
        onView(withId(R.id.input_comment)).perform(replaceText("  "))
        onView(withId(R.id.btn_send)).perform((click()))
        onView(withText(R.string.enter_description)).check(matches(isDisplayed()))
    }
}