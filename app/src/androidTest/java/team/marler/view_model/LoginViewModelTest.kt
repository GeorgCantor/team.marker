package team.marler.view_model

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import team.marker.model.remote.ApiClient
import team.marker.model.remote.ApiRepository
import team.marker.model.requests.LoginRequest
import team.marker.util.Constants.MAIN_STORAGE
import team.marker.view.login.LoginViewModel
import team.marler.base.BaseAndroidTest

@RunWith(AndroidJUnit4::class)
class LoginViewModelTest : BaseAndroidTest() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    val client = ApiClient

    private lateinit var viewModel: LoginViewModel
    private lateinit var repository: ApiRepository
    private lateinit var preferences: SharedPreferences

    @Before
    fun setup() {
        repository = ApiRepository(client.create(getContext()))
        preferences = getContext().getSharedPreferences(MAIN_STORAGE, MODE_PRIVATE)
        viewModel = LoginViewModel(repository, preferences)
    }

    @Test
    fun login_with_incorrect_credentials() {
        viewModel.login(LoginRequest("xx", "xx"))
        viewModel.error.observe(mockLifecycleOwner()) {
            assertTrue(it == LOGIN_ERROR)
        }
    }

    @Test
    fun login_with_correct_credentials() {
        viewModel.login(LoginRequest(LOGIN, PASSWORD))
        viewModel.loginSuccess.observe(mockLifecycleOwner()) {
            assertTrue(it == true)
        }
    }
}