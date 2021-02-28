package team.marker.model.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import team.marker.model.requests.LoginRequest
import team.marker.model.requests.PickRequest
import team.marker.model.responses.*
import team.marker.util.Constants.LATITUDE
import team.marker.util.Constants.LONGITUDE
import team.marker.util.Constants.PRODUCTS
import team.marker.util.Constants.PRODUCT_IDS

interface ApiService {

    @POST("login")
    suspend fun getLogin(@Body request: LoginRequest?): ResponseApi<Login?>?

    @POST("logout")
    suspend fun logout(): ResponseApi<ResponseMessage?>?

    @GET("owner")
    suspend fun getOwner(): ResponseApi<User?>?

    @Multipart
    @POST("breach")
    suspend fun breach(
        @Part("product_id") productId: Int,
        @Part("reason_id") reasonId: Int,
        @Part("user_reason") userReason: RequestBody,
        @Part("comment") comment: RequestBody,
        @Part files: Array<MultipartBody.Part>?
    ): ResponseApi<ResponseMessage?>?

    @GET("history")
    suspend fun getHistory(@Query("offset") offset: Int?): ResponseApi<History?>?

    @POST("pick_extra")
    suspend fun pick(@Body request: PickRequest?): ResponseApi<ResponseMessage?>?

    @GET("product")
    suspend fun getProduct(
        @Query("product_id") productId: String,
        @Query(LATITUDE) lat: String?,
        @Query(LONGITUDE) lng: String?,
        @Query("partner") partner: String?
    ): ResponseApi<Product?>?

    @GET(PRODUCTS)
    suspend fun getProducts(@Query(PRODUCT_IDS) productIds: String?): ResponseApi<Products?>?
}