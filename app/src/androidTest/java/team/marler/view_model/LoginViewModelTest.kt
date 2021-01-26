package team.marler.view_model

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

    @Before
    fun setup() {
        repository = ApiRepository(client.create(getContext()))
        viewModel = LoginViewModel(repository)
    }

    @Test
    fun login_with_incorrect_credentials() {
        if (isNetworkAvailable() && !isUserLoggedIn()) {
            viewModel.login(LoginRequest("xx", "xx"))
            viewModel.error.observe(mockLifecycleOwner()) {
                assertTrue(it == LOGIN_ERROR)
            }
        }
    }

    @Test
    fun login_with_correct_credentials() {
        if (isNetworkAvailable() && !isUserLoggedIn()) {
            viewModel.login(LoginRequest(LOGIN, PASSWORD))
            viewModel.response.observe(mockLifecycleOwner()) {
                assertTrue(it.token?.isNotEmpty() == true)
            }
        }
    }
}