package team.marker.model.remote

import team.marker.model.requests.BreachRequest
import team.marker.model.requests.LoginRequest
import team.marker.model.requests.PickRequest

class ApiRepository(private val apiService: ApiService) {
    // auth
    suspend fun login(loginRequest: LoginRequest) = apiService.getLogin(loginRequest)
    suspend fun logout() = apiService.logout()

    // common
    suspend fun breach(breachRequest: BreachRequest) = apiService.breach(breachRequest)
    suspend fun history(offset: Int?) = apiService.getHistory(offset)
    suspend fun pick(pickRequest: PickRequest) = apiService.pick(pickRequest)
    suspend fun product(product_id: String, lat: String, lng: String) = apiService.getProduct(product_id, lat, lng)
    suspend fun products(product_ids: String?) = apiService.getProducts(product_ids)
}