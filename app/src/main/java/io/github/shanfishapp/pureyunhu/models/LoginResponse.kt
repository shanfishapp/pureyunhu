package io.github.shanfishapp.pureyunhu.models

data class LoginRequest(
    val email: String,
    val password: String,
    val deviceId: String,
    val platform: String
)

data class LoginResponse(
    val code: Int,
    val data: LoginData,
    val msg: String
)

data class LoginData(
    val token: String
)