package io.github.shanfishapp.pureyunhu.models

import kotlinx.serialization.Serializable

// 用于API调用的包装类
@Serializable
data class ListMessageRequest(
    val token: String,
    val data: ListMessageSend
)

data class ListMessageReq(
    val msgCount: Long,
    val msgId: String,
    val chatType: Long,
    val chatId: String
)