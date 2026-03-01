package io.github.shanfishapp.pureyunhu.api

import io.github.shanfishapp.pureyunhu.models.*
import retrofit2.Call
import retrofit2.http.*

interface ApiServices {
    @GET("https://chat-web-go.jwzhd.com/v1/user/info")
    fun getUserInfo(
        @Header("token") token:  String
    ) :Call<GetUserInfo>
    
    @POST("v1/user/email-login")
    fun login(
        @Body loginRequest: LoginRequest
    ) :Call<LoginResponse>

    @POST("v1/user/logout")
    fun logOut(
        @Body logOutRequest: LogOutRequest,
        @Header("token") token: String
    ) :Call<GeneralResponse>
}