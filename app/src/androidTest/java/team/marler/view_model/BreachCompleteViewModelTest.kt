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
import org.koin.android.ext.android.get
import org.mockito.Mock
import team.marker.MyApplication
import team.marker.model.remote.ApiClient
import team.marker.model.remote.ApiRepository
import team.marker.util.Constants.MAIN_STORAGE
import team.marker.view.breach.complete.BreachCompleteViewModel
import team.marler.base.BaseAndroidTest
import java.io.File
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class BreachCompleteViewModelTest : BaseAndroidTest() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    val client = ApiClient

    private lateinit var viewModel: BreachCompleteViewModel
    private lateinit var repository: ApiRepository
    private lateinit var preferences: SharedPreferences

    @Before
    fun setup() {
        repository = ApiRepository(client.create(getContext()))
        preferences = getContext().getSharedPreferences(MAIN_STORAGE, MODE_PRIVATE)
        viewModel = BreachCompleteViewModel(MyApplication().get(), repository, preferences)
    }

    @Test
    fun add_file() {
        viewModel.addPhoto(File.createTempFile(LOGIN, LOGIN))
        viewModel.photos.observe(mockLifecycleOwner()) {
            assertTrue(it.isNotEmpty())
        }
    }

    @Test
    fun remove_file() {
        val file = File.createTempFile(LOGIN, LOGIN)
        viewModel.addPhoto(file)
        viewModel.removePhoto(file)
        TimeUnit.SECONDS.sleep(2)
        assertTrue(viewModel.photos.value?.isEmpty() == true)
    }
}