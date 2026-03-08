package io.github.shanfishapp.pureyunhu.models

import kotlinx.serialization.Serializable

// 标签
@Serializable
data class Tag(
    val id: Long,
    val text: String,
    val color: String
)

// 消息内容
@Serializable
data class Content(
    val text: String? = null,
    val buttons: String? = null,
    val image_url: String? = null,
    val file_name: String? = null,
    val file_url: String? = null,
    val form: String? = null,
    val quote_msg_text: String? = null,
    val sticker_url: String? = null,
    val post_id: String? = null,
    val post_title: String? = null,
    val post_content: String? = null,
    val post_content_type: String? = null,
    val expression_id: String? = null,
    val quote_image_url: String? = null,
    val quote_image_name: String? = null,
    val file_size: Long? = null,
    val video_url: String? = null,
    val audio_url: String? = null,
    val audio_time: Long? = null,
    val quote_video_url: String? = null,
    val quote_video_time: Long? = null,
    val sticker_item_id: Long? = null,
    val sticker_pack_id: Long? = null,
    val call_text: String? = null,
    val call_status_text: String? = null,
    val width: Long? = null,
    val height: Long? = null,
    val tip: String? = null
)

// 发送者信息
@Serializable
data class Sender(
    val chat_id: String,
    val chat_type: Long,
    val name: String,
    val avatar_url: String,
    val tag_old: List<String>? = null,
    val tag: List<Tag>? = null
)

// 消息的Command部分
@Serializable
data class Cmd(
    val name: String? = null,
    val type: Long? = null
)

// 消息
@Serializable
data class Msg(
    val msg_id: String,
    val sender: Sender,
    val direction: String,
    val content_type: Long,
    val content: Content,
    val send_time: Long, // 时间戳(毫秒)
    val cmd: Cmd? = null, // 指令
    val msg_delete_time: Long? = null, // 消息撤回时间
    val quote_msg_id: String? = null, // 引用消息ID
    val msg_seq: Long? = null,
    val edit_time: Long? = null // 最后编辑时间
)

// 状态
@Serializable
data class Status(
    val number: Long, // 不知道干啥的,可能是请求ID
    val code: Long, // 状态码,1为正常
    val msg: String // 返回消息
)

// 获取消息请求
@Serializable
data class ListMessageSend(
    val msg_count: Long = 50, // 默认获取50条消息
    val msg_id: String = "", // 从指定消息ID开始，空字符串表示从最新开始
    val chat_type: Long,
    val chat_id: String
)

// 获取消息响应
@Serializable
data class ListMessageResponse(
    val status: Status,
    val msg: List<Msg>
)