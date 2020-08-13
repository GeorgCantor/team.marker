package team.marker.model.remote

import io.reactivex.Observable
import team.marker.model.requests.*
import team.marker.model.responses.*

class ApiRepository(private val apiService: ApiService) {
    // auth
    fun login(loginRequest: LoginRequest): Observable<ResponseAPI<Login?>?>? = apiService.getLogin(loginRequest)
    fun logout(): Observable<ResponseAPI<ResponseMessage?>?>? = apiService.logout()
    // common
    fun history(offset: Int?): Observable<ResponseAPI<History?>?>? = apiService.getHistory(offset)
    fun pick(pickRequest: PickRequest): Observable<ResponseAPI<ResponseMessage?>?>? = apiService.pick(pickRequest)
    fun product(product_id: String, lat: String, lng: String): Observable<ResponseAPI<Product?>?>? = apiService.getProduct(product_id, lat, lng)
    fun products(product_ids: String?): Observable<ResponseAPI<Products?>?>? = apiService.getProducts(product_ids)
}