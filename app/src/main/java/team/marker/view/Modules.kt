package team.marker.view

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import team.marker.model.remote.ApiClient
import team.marker.model.remote.ApiRepository
import team.marker.view.history.HistoryViewModel
import team.marker.view.home.HomeViewModel
import team.marker.view.login.LoginViewModel
import team.marker.view.product.ProductViewModel
import team.marker.view.scan.ScanViewModel

val repositoryModule = module {
    single { ApiRepository(get()) }
}

val viewModelModule = module {
    viewModel {
        HistoryViewModel(get())
    }
    viewModel {
        HomeViewModel(get())
    }
    viewModel {
        LoginViewModel(get())
    }
    viewModel {
        ProductViewModel(get())
    }
    viewModel {
        ScanViewModel(get())
    }
}

val appModule = module {
    single { ApiClient.create(get()) }
}