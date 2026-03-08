@file:OptIn(ExperimentalSerializationApi::class)

package io.github.shanfishapp.pureyunhu.proto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

// 共用区 - Tag
@Serializable
data class Tag(
    @ProtoNumber(1) val id: ULong = 0u,      // 标签ID，设为可选
    @ProtoNumber(3) val text: String = "",
    @ProtoNumber(4) val color: String = ""
)

// WebSocket 外层包装 - 保持必需（不要改）
@Serializable
data class WebSocketMessage(
    @ProtoNumber(1) val info: WebSocketInfo  // 必需
)

// WebSocket 信息 - 保持必需
@Serializable
data class WebSocketInfo(
    @ProtoNumber(1) val seq: String,  // 必需
    @ProtoNumber(2) val cmd: String    // 必需
)

// ws推送消息（整体结构）- 全部设为可选
@Serializable
data class PushMessage(
    @ProtoNumber(1) val info: WebSocketInfo? = null,  // 改为可选
    @ProtoNumber(2) val data: Data           // 改为可选
) {
    @Serializable
    data class Data(
        @ProtoNumber(1) val any: String = "",
        @ProtoNumber(2) val msg: Msg? = null
    )

    @Serializable
    data class Msg(
        @ProtoNumber(1) val msgId: String = "",           // 默认值
        @ProtoNumber(2) val sender: Sender? = null,       // 改为可空
        @ProtoNumber(3) val recvId: String = "",
        @ProtoNumber(4) val chatId: String,
        @ProtoNumber(5) val chatType: Int,
        @ProtoNumber(6) val content: Content,     // 改为可空
        @ProtoNumber(7) val contentType: ULong,
        @ProtoNumber(8) val timestamp: ULong = 0u,
        @ProtoNumber(9) val cmd: Cmd? = null,             // 改为可空（这就是导致之前错误的字段）
        @ProtoNumber(10) val deleteTimestamp: ULong = 0u,
        @ProtoNumber(11) val quoteMsgId: String = "",
        @ProtoNumber(12) val msgSeq: ULong = 0u
    )

    @Serializable
    data class Cmd(
        @ProtoNumber(1) val id: ULong = 0u,    // 默认值
        @ProtoNumber(2) val name: String = ""   // 默认值
    )

    @Serializable
    data class Sender(
        @ProtoNumber(1) val chatId: String = "",
        @ProtoNumber(2) val chatType: ULong = 0u,
        @ProtoNumber(3) val name: String = "",
        @ProtoNumber(4) val avatarUrl: String = "",
        @ProtoNumber(6) val tagOld: List<String> = emptyList(),
        @ProtoNumber(7) val tag: List<Tag> = emptyList()
    )

    @Serializable
    data class Content(
        @ProtoNumber(1) val text: String = "",
        @ProtoNumber(2) val buttons: String = "",
        @ProtoNumber(3) val imageUrl: String = "",
        @ProtoNumber(4) val fileName: String = "",
        @ProtoNumber(5) val fileUrl: String = "",
        @ProtoNumber(7) val form: String = "",
        @ProtoNumber(8) val quoteMsgText: String = "",
        @ProtoNumber(9) val stickerUrl: String = "",
        @ProtoNumber(10) val postId: String = "",
        @ProtoNumber(11) val postTitle: String = "",
        @ProtoNumber(12) val postContent: String = "",
        @ProtoNumber(13) val postContentType: String = "",
        @ProtoNumber(15) val expressionId: String = "",
        @ProtoNumber(16) val quoteImageUrl: String = "",
        @ProtoNumber(17) val quoteImageName: String = "",
        @ProtoNumber(18) val fileSize: ULong = 0u,
        @ProtoNumber(19) val videoUrl: String = "",
        @ProtoNumber(21) val audioUrl: String = "",
        @ProtoNumber(22) val audioTime: ULong = 0u,
        @ProtoNumber(23) val quoteVideoUrl: String = "",
        @ProtoNumber(24) val quoteVideoTime: ULong = 0u,
        @ProtoNumber(25) val stickerItemId: ULong = 0u,
        @ProtoNumber(26) val stickerPackId: ULong = 0u,
        @ProtoNumber(29) val callText: String = "",
        @ProtoNumber(32) val callStatusText: String = "",
        @ProtoNumber(33) val width: ULong = 0u,
        @ProtoNumber(34) val height: ULong = 0u,
        @ProtoNumber(37) val tip: String = ""
    )
}