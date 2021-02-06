package team.marker.di

import android.content.Context.MODE_PRIVATE
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import team.marker.model.remote.ApiClient
import team.marker.model.remote.ApiRepository
import team.marker.util.Constants.MAIN_STORAGE
import team.marker.view.breach.complete.BreachCompleteViewModel
import team.marker.view.history.HistoryViewModel
import team.marker.view.home.HomeViewModel
import team.marker.view.login.LoginViewModel
import team.marker.view.pick.complete.PickCompleteViewModel
import team.marker.view.pick.products.PickProductsViewModel
import team.marker.view.product.ProductViewModel

val repositoryModule = module {
    single { ApiRepository(get()) }
}

val viewModelModule = module {
    viewModel { BreachCompleteViewModel(androidApplication(), get(), get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { PickCompleteViewModel(get(), get()) }
    viewModel { PickProductsViewModel(get()) }
    viewModel { ProductViewModel(get()) }
}

val apiModule = module {
    single { ApiClient.create(get()) }
}

val preferenceModule = module {
    single { androidApplication().getSharedPreferences(MAIN_STORAGE, MODE_PRIVATE) }
    single(named(MAIN_STORAGE)) { androidApplication().getSharedPreferences(MAIN_STORAGE, MODE_PRIVATE) }
}