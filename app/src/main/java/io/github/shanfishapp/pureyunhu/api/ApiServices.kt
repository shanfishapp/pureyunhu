package io.github.shanfishapp.pureyunhu.api

import io.github.shanfishapp.pureyunhu.models.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiServices {
    @GET("https://chat-web-go.jwzhd.com/v1/user/info")
    fun getUserInfo(
        @Header("token") token:  String
    ) :Call<GetUserInfo>
    
    @GET("https://chat-web-go.jwzhd.com/v1/user/homepage")
    fun getUserHomepageInfo(
        @Header("token") token: String,
        @Query("userId") userId: String
    ) :Call<UserHomepageInfo>

    @POST("v1/user/email-login")
    fun login(
        @Body loginRequest: LoginRequest
    ) :Call<LoginResponse>

    @POST("v1/user/logout")
    fun logOut(
        @Body logOutRequest: LogOutRequest,
        @Header("token") token: String
    ) :Call<GeneralResponse>
    
    @POST("https://chat-go.jwzhd.com/v1/conversation/list")
    fun getConversationList(
        @Header("token") token: String
    ) :Call<ResponseBody>
    
    @POST("https://chat-web-go.jwzhd.com/v1/bot/bot-info")
    fun getBotInfo(
        @Body requestBody: okhttp3.RequestBody,
        @Header("token") token: String
    ) :Call<ResponseBody>
    
    @POST("https://chat-web-go.jwzhd.com/v1/group/group-info")
    fun getGroupInfo(
        @Body requestBody: okhttp3.RequestBody,
        @Header("token") token: String
    ) :Call<ResponseBody>

    @POST("/v1/msg/list-message")
    fun getListMessage(
        @Body requestBody: RequestBody,
        @Header("token") token: String
    ): Call<ResponseBody>
    
    @POST("/v1/msg/list-message")
    suspend fun getListMessageSuspend(
        @Body requestBody: RequestBody,
        @Header("token") token: String
    ): ResponseBody
}