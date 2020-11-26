package team.marker.model.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
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
    @Multipart
    @POST("breach")
    suspend fun breach(
        @Part("product_id") product_id: Int?,
        @Part("reason_id") reason_id: Int?,
        @Part("user_reason") user_reason: RequestBody?,
        @Part("comment") comment: RequestBody?,
        @Part files: Array<MultipartBody.Part>?
    ): ResponseAPI<ResponseMessage?>?

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