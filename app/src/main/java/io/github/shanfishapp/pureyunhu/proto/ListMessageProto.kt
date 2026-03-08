@file:OptIn(ExperimentalSerializationApi::class)

package io.github.shanfishapp.pureyunhu.proto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

// 请求消息
@Serializable
data class ListMessageSend(
    @ProtoNumber(2) val msgCount: ULong = 0u, // 获取消息数
    @ProtoNumber(3) val msgId: String = "", // 从指定消息ID开始
    @ProtoNumber(4) val chatType: ULong = 0u, // 对象类型
    @ProtoNumber(5) val chatId: String = "" // 对象ID
)

// 标签


// 消息内容
@Serializable
data class Content(
    @ProtoNumber(1) val text: String? = null, // 消息内容
    @ProtoNumber(2) val buttons: String? = null, // 按钮
    @ProtoNumber(3) val imageUrl: String? = null,
    @ProtoNumber(4) val fileName: String? = null,
    @ProtoNumber(5) val fileUrl: String? = null,
    @ProtoNumber(7) val form: String? = null, // 表单消息
    @ProtoNumber(8) val quoteMsgText: String? = null, // 引用消息文字
    @ProtoNumber(9) val stickerUrl: String? = null, // 表情URL
    @ProtoNumber(10) val postId: String? = null, // 文章ID
    @ProtoNumber(11) val postTitle: String? = null, // 文章标题
    @ProtoNumber(12) val postContent: String? = null, // 文章内容
    @ProtoNumber(13) val postContentType: String? = null, // 文章类型
    @ProtoNumber(15) val expressionId: String? = null, // 个人表情ID(不知道为啥为STR)
    @ProtoNumber(16) val quoteImageUrl: String? = null, // 引用图片直链,https://...
    @ProtoNumber(17) val quoteImageName: String? = null, // 引用图片文件名称
    @ProtoNumber(18) val fileSize: ULong = 0u, // 文件/图片大小(字节)
    @ProtoNumber(19) val videoUrl: String? = null, // 视频URL
    @ProtoNumber(21) val audioUrl: String? = null, // 语音URL
    @ProtoNumber(22) val audioTime: ULong = 0u, // 语音时长
    @ProtoNumber(23) val quoteVideoUrl: String? = null, // 引用视频直链,https://...
    @ProtoNumber(24) val quoteVideoTime: ULong = 0u, // 引用视频时长
    @ProtoNumber(25) val stickerItemId: ULong = 0u, // 表情ID
    @ProtoNumber(26) val stickerPackId: ULong = 0u, // 表情包ID
    @ProtoNumber(29) val callText: String? = null, // 语音通话文字
    @ProtoNumber(32) val callStatusText: String? = null, // 语音通话状态文字
    @ProtoNumber(33) val width: ULong = 0u, // 图片的宽度
    @ProtoNumber(34) val height: ULong = 0u, // 图片的高度
    @ProtoNumber(37) val tip: String? = null // 提示信息
)

// 发送者信息
@Serializable
data class Sender(
    @ProtoNumber(1) val chatId: String = "",
    @ProtoNumber(2) val chatType: ULong = 0u,
    @ProtoNumber(3) val name: String = "",
    @ProtoNumber(4) val avatarUrl: String = "",
    @ProtoNumber(6) val tagOld: List<String> = emptyList(),
    @ProtoNumber(7) val tag: List<Tag> = emptyList()
) {
    @Serializable
    data class Tag(
        @ProtoNumber(1) val id: ULong = 0u, // 标签ID(貌似)
        @ProtoNumber(3) val text: String = "",
        @ProtoNumber(4) val color: String = ""
    )
}

// 消息的Command部分
@Serializable
data class Cmd(
    @ProtoNumber(2) val name: String? = null, // 指令名
    @ProtoNumber(4) val type: ULong? = null // 指令类型
)

// 消息
@Serializable
data class Msg(
    @ProtoNumber(1) val msgId: String = "", // 消息ID
    @ProtoNumber(2) val sender: Sender? = null,
    @ProtoNumber(3) val direction: String = "", // 消息位置,左边/右边
    @ProtoNumber(4) val contentType: ULong = 0u,
    @ProtoNumber(5) val content: Content? = null,
    @ProtoNumber(6) val sendTime: ULong = 0u, // 时间戳(毫秒)
    @ProtoNumber(7) val cmd: Cmd? = null, // 指令
    @ProtoNumber(8) val msgDeleteTime: ULong? = null, // 消息撤回时间
    @ProtoNumber(9) val quoteMsgId: String? = null, // 引用消息ID
    @ProtoNumber(10) val msgSeq: ULong = 0u,
    @ProtoNumber(12) val editTime: ULong? = null // 最后编辑时间
)

// 状态
@Serializable
data class Status(
    @ProtoNumber(1) val number: ULong = 0u, // 不知道干啥的,可能是请求ID
    @ProtoNumber(2) val code: ULong = 0u, // 状态码,1为正常
    @ProtoNumber(3) val msg: String = "" // 返回消息
)

// 获取消息响应
@Serializable
data class ListMessageResponse(
    @ProtoNumber(1) val status: Status? = null,
    @ProtoNumber(2) val msg: List<Msg> = emptyList()
)