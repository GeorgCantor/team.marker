package team.marker.model.remote

import io.reactivex.Observable
import team.marker.model.requests.*
import team.marker.model.responses.*

class ApiRepository(private val apiService: ApiService) {
    // auth
    fun login(loginRequest: LoginRequest): Observable<ResponseAPI<Login?>?>? = apiService.getLogin(loginRequest)
    fun logout(): Observable<ResponseAPI<ResponseMessage?>?>? = apiService.logout()
    // common
    fun history(offset: String): Observable<ResponseAPI<History?>?>? = apiService.getHistory(offset)
    fun product(product_id: String): Observable<ResponseAPI<Product?>?>? = apiService.getProduct(product_id)
}