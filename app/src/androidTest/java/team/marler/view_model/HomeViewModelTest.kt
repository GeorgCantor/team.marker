package team.marler.view_model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import team.marker.model.remote.ApiClient
import team.marker.model.remote.ApiRepository
import team.marker.view.home.HomeViewModel
import team.marler.base.BaseAndroidTest
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class HomeViewModelTest : BaseAndroidTest() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    val client = ApiClient

    private lateinit var viewModel: HomeViewModel
    private lateinit var repository: ApiRepository

    @Before
    fun setup() {
        repository = ApiRepository(client.create(getContext()))
        viewModel = HomeViewModel(repository)
    }

    @Test
    fun logout() = runBlocking {
        if (isUserLoggedIn()) {
            viewModel.logout()
            TimeUnit.SECONDS.sleep(1)
            assert(!isUserLoggedIn())
        }
    }
}