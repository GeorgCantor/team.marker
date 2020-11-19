package team.marker.model.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import team.marker.model.requests.BreachRequest
import team.marker.model.requests.LoginRequest
import team.marker.model.requests.PickRequest
import team.marker.model.responses.*

interface ApiService {

    // auth

    @POST("login")
    suspend fun getLogin(@Body request: LoginRequest?): ResponseAPI<Login?>?

    @POST("logout")
    suspend fun logout(): ResponseAPI<ResponseMessage?>?

    @GET("owner")
    suspend fun getOwner(): ResponseAPI<User?>?

    // scan

    @POST("breach")
    suspend fun breach(@Body request: BreachRequest?): ResponseAPI<ResponseMessage?>?

    @GET("history")
    suspend fun getHistory(@Query("offset") offset: Int?): ResponseAPI<History?>?

    @POST("pick_extra")
    suspend fun pick(@Body request: PickRequest?): ResponseAPI<ResponseMessage?>?

    @GET("product")
    suspend fun getProduct(
        @Query("product_id") product_id: String?,
        @Query("lat") lat: String?,
        @Query("lng") lng: String?
    ): ResponseAPI<Product?>?

    @GET("products")
    suspend fun getProducts(@Query("product_ids") product_ids: String?): ResponseAPI<Products?>?
}