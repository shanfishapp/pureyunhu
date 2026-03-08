package io.github.shanfishapp.pureyunhu.models

data class MessageItem(
    val chatId: String,
    val chatType: String,
    val msgId: String,
    val text: String,
    val imageUrl: String? = null,
    val messageType: MessageType = MessageType.TEXT, // 新增消息类型字段
    val senderId: String,
    val senderNickName: String,
    val senderType: String,
    val senderAvatarUrl: String,
    val isMe: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageType {
    TEXT,      // 文本消息
    IMAGE,     // 图片消息
    MARKDOWN,  // Markdown消息
    WEBVIEW    // WebView消息
}