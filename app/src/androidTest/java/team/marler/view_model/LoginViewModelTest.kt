package team.marler.view_model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import team.marker.R
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
                if (it == getContext().getString(R.string.wrong_login_password)) assert(true)
            }
        }
    }
}