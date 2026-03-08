package io.github.shanfishapp.pureyunhu.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class WsLoginRequest(
    val seq: String,
    val cmd: String,
    val data: Data
) {
    @Serializable
    data class Data(
        @SerialName("userId")
        val userId: String,
        val token: String,
        val platform: String,  // 不限制，可以是任意字符串
        @SerialName("deviceId")
        val deviceId: String
    )
}