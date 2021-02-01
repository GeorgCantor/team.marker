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
import team.marker.model.responses.Product
import team.marker.model.responses.Products
import team.marker.view.pick.products.PickProductsViewModel
import team.marler.base.BaseAndroidTest

@RunWith(AndroidJUnit4::class)
class PickProductsViewModelTest : BaseAndroidTest() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    val client = ApiClient

    private lateinit var viewModel: PickProductsViewModel
    private lateinit var repository: ApiRepository

    @Before
    fun setup() {
        repository = ApiRepository(client.create(getContext()))
        viewModel = PickProductsViewModel(repository)
    }

    private fun getDummyProduct() = Product(
        id = (0..100).random(),
        code = (0..100).random().toString(),
        title = (0..100).random().toString(),
        destination = (0..100).random().toString(),
        produced = (0..100).random().toString(),
        shipped = (0..100).random().toString()
    )

    @Test
    fun getting_dummy_products() {
        val products = mutableListOf<Product>()
        (0..10).forEach { products.add(getDummyProduct()) }
        viewModel.response.value = Products(products)
        viewModel.response.observe(mockLifecycleOwner()) {
            assertTrue(it.info?.size == 11)
        }
    }
}