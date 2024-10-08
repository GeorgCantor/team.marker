package team.marler.base

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import org.hamcrest.Matcher
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import team.marker.util.Constants.MAIN_STORAGE
import team.marker.util.Constants.TOKEN
import team.marker.util.getAny
import team.marker.util.isNetworkAvailable

open class BaseAndroidTest {

    companion object {
        const val LOGIN = "info@ngkomplekt.ru"
        const val PASSWORD = "demo12345"
        const val LOGIN_ERROR = "Пользователь с указанной почтой не найден"
        const val AUTH_ERROR = "User authorization failed: no access token passed."
    }

    protected fun getContext(): Context = ApplicationProvider.getApplicationContext()

    protected fun isUserLoggedIn(): Boolean {
        val prefs = getContext().applicationContext.getSharedPreferences(MAIN_STORAGE, MODE_PRIVATE)
        val token = prefs.getAny("", TOKEN) as String

        return token.isNotEmpty()
    }

    protected fun isNetworkAvailable() = getContext().isNetworkAvailable()

    protected fun mockLifecycleOwner(): LifecycleOwner {
        val owner: LifecycleOwner = mock(LifecycleOwner::class.java)
        val lifecycle = LifecycleRegistry(owner)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        `when`(owner.lifecycle).thenReturn(lifecycle)

        return owner
    }

    protected fun waitFor(delay: Long): ViewAction {
        return object : ViewAction {
            override fun perform(uiController: UiController?, view: View?) {
                uiController?.loopMainThreadForAtLeast(delay)
            }

            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isRoot()
            }

            override fun getDescription(): String {
                return "wait for " + delay + "milliseconds"
            }
        }
    }

    protected fun grantPermission() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        if (Build.VERSION.SDK_INT >= 23) {
            val allowPermission = UiDevice.getInstance(instrumentation).findObject(
                UiSelector().text(
                    when {
                        Build.VERSION.SDK_INT == 23 -> "Allow"
                        Build.VERSION.SDK_INT <= 28 -> "ALLOW"
                        Build.VERSION.SDK_INT == 29 -> "Allow only while using the app"
                        else -> "While using the app"
                    }
                )
            )
            if (allowPermission.exists()) allowPermission.click()
        }
    }

    protected fun denyPermission() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        if (Build.VERSION.SDK_INT >= 23) {
            val denyPermission = UiDevice.getInstance(instrumentation).findObject(
                UiSelector().text(
                    when (Build.VERSION.SDK_INT) {
                        in 24..28 -> "DENY"
                        else -> "Deny"
                    }
                )
            )
            if (denyPermission.exists()) denyPermission.click()
        }
    }
}