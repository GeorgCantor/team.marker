package team.marker.view

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import team.marker.model.remote.*
import team.marker.view.history.HistoryViewModel
import team.marker.view.home.HomeViewModel
import team.marker.view.login.LoginViewModel
import team.marker.view.pick.PickViewModel
import team.marker.view.pick.complete.PickCompleteViewModel
import team.marker.view.pick.products.PickProductsViewModel
import team.marker.view.product.ProductViewModel

val repositoryModule = module {
    single { ApiRepository(get()) }
}

val viewModelModule = module {
    viewModel { HistoryViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { PickViewModel(get()) }
    viewModel { PickCompleteViewModel(get()) }
    viewModel { PickProductsViewModel(get()) }
    viewModel { ProductViewModel(get()) }
}

val apiModule = module {
    single { ApiClient.create(get()) }
}