package io.github.shanfishapp.pureyunhu.managers

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import io.github.shanfishapp.pureyunhu.models.MessageItem
import io.github.shanfishapp.pureyunhu.models.MessageType

/**
 * 消息管理器，用于在不同组件之间共享和管理消息
 */
object MessageManager {
    private const val TAG = "MessageManager"
    
    // 使用一个Map来存储不同聊天的消息列表
    private val chatMessages = mutableMapOf<String, SnapshotStateList<MessageItem>>()
    
    /**
     * 获取特定聊天的消息列表
     */
    fun getMessages(chatId: String): SnapshotStateList<MessageItem> {
        Log.d(TAG, "获取聊天 $chatId 的消息列表")
        return chatMessages.getOrPut(chatId) { 
            Log.d(TAG, "为聊天 $chatId 创建新的消息列表")
            mutableStateListOf<MessageItem>() 
        }
    }
    
    /**
     * 添加文本消息项
     */
    fun addMessageTextItems(
        chatId: String,
        chatType: String,
        msgId: String,
        text: String,
        senderId: String,
        senderNickName: String,
        senderType: String,
        senderAvatarUrl: String,
        isMe: Boolean
    ) {
        Log.d(TAG, "添加消息到聊天 $chatId: msgId=$msgId, text=$text, isMe=$isMe")
        val message = MessageItem(
            chatId = chatId,
            chatType = chatType,
            msgId = msgId,
            text = text,
            messageType = MessageType.TEXT,
            senderId = senderId,
            senderNickName = senderNickName,
            senderType = senderType,
            senderAvatarUrl = senderAvatarUrl,
            isMe = isMe
        )
        
        val messages = getMessages(chatId)
        messages.add(message)
        Log.d(TAG, "消息已添加到聊天 $chatId，当前消息数量: ${messages.size}")
    }
    
    /**
     * 添加图片消息项
     */
    fun addMessageImageItems(
        chatId: String,
        chatType: String,
        msgId: String,
        text: String,
        imageUrl: String,
        senderId: String,
        senderNickName: String,
        senderType: String,
        senderAvatarUrl: String,
        isMe: Boolean
    ) {
        Log.d(TAG, "添加图片消息到聊天 $chatId: msgId=$msgId, imageUrl=$imageUrl, isMe=$isMe")
        val message = MessageItem(
            chatId = chatId,
            chatType = chatType,
            msgId = msgId,
            text = text,
            imageUrl = imageUrl,
            messageType = MessageType.IMAGE,
            senderId = senderId,
            senderNickName = senderNickName,
            senderType = senderType,
            senderAvatarUrl = senderAvatarUrl,
            isMe = isMe
        )
        
        val messages = getMessages(chatId)
        messages.add(message)
        Log.d(TAG, "图片消息已添加到聊天 $chatId，当前消息数量: ${messages.size}")
    }
    
    /**
     * 添加Markdown消息项
     */
    fun addMessageMarkdownItems(
        chatId: String,
        chatType: String,
        msgId: String,
        text: String,
        senderId: String,
        senderNickName: String,
        senderType: String,
        senderAvatarUrl: String,
        isMe: Boolean
    ) {
        Log.d(TAG, "添加Markdown消息到聊天 $chatId: msgId=$msgId, text=$text, isMe=$isMe")
        val message = MessageItem(
            chatId = chatId,
            chatType = chatType,
            msgId = msgId,
            text = text,
            messageType = MessageType.MARKDOWN,
            senderId = senderId,
            senderNickName = senderNickName,
            senderType = senderType,
            senderAvatarUrl = senderAvatarUrl,
            isMe = isMe
        )
        
        val messages = getMessages(chatId)
        messages.add(message)
        Log.d(TAG, "Markdown消息已添加到聊天 $chatId，当前消息数量: ${messages.size}")
    }
    
    /**
     * 添加WebView消息项
     */
    fun addMessageWebviewItems(
        chatId: String,
        chatType: String,
        msgId: String,
        text: String,
        senderId: String,
        senderNickName: String,
        senderType: String,
        senderAvatarUrl: String,
        isMe: Boolean
    ) {
        Log.d(TAG, "添加WebView消息到聊天 $chatId: msgId=$msgId, text=$text, isMe=$isMe")
        val message = MessageItem(
            chatId = chatId,
            chatType = chatType,
            msgId = msgId,
            text = text,
            messageType = MessageType.WEBVIEW,
            senderId = senderId,
            senderNickName = senderNickName,
            senderType = senderType,
            senderAvatarUrl = senderAvatarUrl,
            isMe = isMe
        )
        
        val messages = getMessages(chatId)
        messages.add(message)
        Log.d(TAG, "WebView消息已添加到聊天 $chatId，当前消息数量: ${messages.size}")
    }
    
    /**
     * 清除特定聊天的消息
     */
    fun clearMessages(chatId: String) {
        Log.d(TAG, "清除聊天 $chatId 的消息")
        chatMessages[chatId]?.clear()
    }
    
    /**
     * 获取聊天消息数量
     */
    fun getMessageCount(chatId: String): Int {
        return getMessages(chatId).size
    }
}