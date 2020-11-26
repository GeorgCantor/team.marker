package team.marker.model.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import team.marker.model.requests.LoginRequest
import team.marker.model.requests.PickRequest

class ApiRepository(private val apiService: ApiService) {
    // auth
    suspend fun login(loginRequest: LoginRequest) = apiService.getLogin(loginRequest)
    suspend fun logout() = apiService.logout()

    // common
    suspend fun breach(
        productId: Int,
        reasonId: Int,
        userReason: RequestBody,
        comment: RequestBody,
        files: Array<MultipartBody.Part>?
    ) = apiService.breach(productId, reasonId, userReason, comment, files)

    suspend fun history(offset: Int?) = apiService.getHistory(offset)
    suspend fun pick(pickRequest: PickRequest) = apiService.pick(pickRequest)
    suspend fun product(product_id: String, lat: String, lng: String) =
        apiService.getProduct(product_id, lat, lng)

    suspend fun products(product_ids: String?) = apiService.getProducts(product_ids)
}