package team.marler.view_model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.android.get
import org.mockito.Mock
import team.marker.MyApplication
import team.marker.model.remote.ApiClient
import team.marker.model.remote.ApiRepository
import team.marker.view.breach.complete.BreachCompleteViewModel
import team.marler.base.BaseAndroidTest
import java.io.File

@RunWith(AndroidJUnit4::class)
class BreachCompleteViewModelTest : BaseAndroidTest() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    val client = ApiClient

    private lateinit var viewModel: BreachCompleteViewModel
    private lateinit var repository: ApiRepository

    @Before
    fun setup() {
        repository = ApiRepository(client.create(getContext()))
        viewModel = BreachCompleteViewModel(MyApplication().get(), repository)
    }

    @Test
    fun add_file() {
        if (isNetworkAvailable() && isUserLoggedIn()) {
            viewModel.addPhoto(File.createTempFile(LOGIN, LOGIN))
            viewModel.photos.observe(mockLifecycleOwner()) {
                if (it.isNotEmpty()) assert(true)
            }
        }
    }
}