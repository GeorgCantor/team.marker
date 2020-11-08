package team.marker.model.remote

import io.reactivex.Observable
import team.marker.model.requests.*
import team.marker.model.responses.*
import retrofit2.http.*

interface ApiService {

    // auth

    @POST("login")
    fun getLogin(@Body request: LoginRequest?): Observable<ResponseAPI<Login?>?>?

    @POST("logout")
    fun logout(): Observable<ResponseAPI<ResponseMessage?>?>?

    @GET("owner")
    fun getOwner(): Observable<ResponseAPI<User?>?>?

    // scan

    @POST("breach")
    fun breach(@Body request: BreachRequest?): Observable<ResponseAPI<ResponseMessage?>?>?

    @GET("history")
    fun getHistory(@Query("offset") offset: Int?): Observable<ResponseAPI<History?>?>?

    @POST("pick_extra")
    fun pick(@Body request: PickRequest?): Observable<ResponseAPI<ResponseMessage?>?>?

    @GET("product")
    fun getProduct(
        @Query("product_id") product_id: String?,
        @Query("lat") lat: String?,
        @Query("lng") lng: String?
    ): Observable<ResponseAPI<Product?>?>?

    @GET("products")
    fun getProducts(@Query("product_ids") product_ids: String?): Observable<ResponseAPI<Products?>?>?

}