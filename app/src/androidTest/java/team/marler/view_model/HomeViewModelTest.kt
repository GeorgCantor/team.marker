package team.marler.view_model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.assertTrue
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
    fun logout_without_token() = runBlocking {
        if (isNetworkAvailable()) {
            viewModel.logout()
            viewModel.error.observe(mockLifecycleOwner()) {
                assertTrue(it == AUTH_ERROR)
            }
        }
    }
}