package team.marker.model.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import team.marker.model.requests.CargoRequest
import team.marker.model.requests.LoginRequest
import team.marker.model.requests.PickRequest

class ApiRepository(private val apiService: ApiService) {

    suspend fun login(loginRequest: LoginRequest) = apiService.getLogin(loginRequest)

    suspend fun logout() = apiService.logout()

    suspend fun breach(
        productId: Int,
        reasonId: Int,
        userReason: RequestBody,
        comment: RequestBody,
        files: Array<MultipartBody.Part>?
    ) = apiService.breach(productId, reasonId, userReason, comment, files)

    suspend fun history(offset: Int?) = apiService.getHistory(offset)

    suspend fun pick(pickRequest: PickRequest) = apiService.pick(pickRequest)

    suspend fun product(
        productId: String,
        lat: String,
        lng: String,
        partner: String?
    ) = apiService.getProduct(productId, lat, lng, partner)

    suspend fun products(productIds: String?) = apiService.getProducts(productIds)

    suspend fun cargo(request: CargoRequest) = apiService.cargo(request)
}