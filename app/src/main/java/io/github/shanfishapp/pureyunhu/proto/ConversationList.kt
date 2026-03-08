@file:OptIn(ExperimentalSerializationApi::class)

package io.github.shanfishapp.pureyunhu.proto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

// 对话列表响应 - 外层结构
@Serializable
data class ConversationListResponse(
    @ProtoNumber(1) val status: Status? = null,
    @ProtoNumber(2) val data: List<ConversationData> = emptyList(),
    @ProtoNumber(3) val total: ULong = 0u,
    @ProtoNumber(4) val requestId: String = ""
)

// 状态信息


// 对话数据
@Serializable
data class ConversationData(
    @ProtoNumber(1) val chatId: String = "",
    @ProtoNumber(2) val chatType: ULong = 0u,
    @ProtoNumber(3) val name: String = "",
    @ProtoNumber(4) val chatContent: String = "",
    @ProtoNumber(5) val timestampMs: ULong = 0u,
    @ProtoNumber(6) val unreadMessage: ULong = 0u,
    @ProtoNumber(7) val at: ULong = 0u,
    @ProtoNumber(8) val avatarId: ULong = 0u,
    @ProtoNumber(9) val avatarUrl: String? = null,
    @ProtoNumber(11) val doNotDisturb: ULong = 0u,
    @ProtoNumber(12) val timestamp: ULong = 0u,
    @ProtoNumber(14) val atData: AtData? = null,
    @ProtoNumber(16) val certificationLevel: ULong = 0u
)

// @数据
@Serializable
data class AtData(
    @ProtoNumber(1) val unknown: ULong = 0u,
    @ProtoNumber(2) val mentionedId: String = "",
    @ProtoNumber(3) val mentionedName: String = "",
    @ProtoNumber(4) val mentionedIn: String = "",
    @ProtoNumber(6) val mentionerId: String = "",
    @ProtoNumber(7) val mentionerName: String = "",
    @ProtoNumber(8) val msgSeq: ULong = 0u
)