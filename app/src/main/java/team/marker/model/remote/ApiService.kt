package team.marker.model.remote

import io.reactivex.Observable
import team.marker.model.requests.*
import team.marker.model.responses.*
import retrofit2.http.*

interface ApiService {

    @GET("history")
    fun getHistory(@Query("offset") offset: Int?): Observable<ResponseAPI<History?>?>?

    @POST("login")
    fun getLogin(@Body request: LoginRequest?): Observable<ResponseAPI<Login?>?>?

    @POST("logout")
    fun logout(): Observable<ResponseAPI<ResponseMessage?>?>?

    @GET("owner")
    fun getOwner(): Observable<ResponseAPI<User?>?>?

    @GET("product")
    fun getProduct(@Query("product_id") offset: String?): Observable<ResponseAPI<Product?>?>?

}