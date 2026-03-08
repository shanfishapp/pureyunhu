package io.github.shanfishapp.pureyunhu.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.serialization.decodeFromByteArray
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.shanfishapp.pureyunhu.api.ApiClient
import io.github.shanfishapp.pureyunhu.managers.MessageManager
import io.github.shanfishapp.pureyunhu.models.MessageItem
import io.github.shanfishapp.pureyunhu.models.MessageType
import io.github.shanfishapp.pureyunhu.utils.AsyncImageRefer
import io.github.shanfishapp.pureyunhu.utils.TokenManager
import io.github.shanfishapp.pureyunhu.utils.MarkdownParser
import io.github.shanfishapp.pureyunhu.utils.WebviewUtils
import androidx.compose.foundation.clickable
import io.github.shanfishapp.pureyunhu.proto.ListMessageResponse
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSerializationApi::class)
@Composable
fun MessageScreen(
    navController: NavController,
    chatId: String,
    chatType: String,
    chatName: String
) {
    val TAG = "MessageScreen"
    
    // 使用一个状态来跟踪是否正在导航，防止重复点击
    var isNavigating by remember { mutableStateOf(false) }
    
    // 从MessageManager获取特定聊天的消息列表
    val messages = MessageManager.getMessages(chatId)
    Log.d(TAG, "MessageScreen: 获取到聊天 $chatId 的消息列表，当前数量: ${messages.size}")
    
    // 状态管理加载状态
    var isLoading by remember { mutableStateOf(false) }
    
    // 获取当前用户ID
    val currentUserId = io.github.shanfishapp.pureyunhu.utils.UserInfoManager.getCachedUserHomepageInfo()?.userId ?: ""
    
    // 添加一个状态来跟踪是否正在加载更多消息
    var isLoadingMore by remember { mutableStateOf(false) }
    
    // 添加一个状态来跟踪最早的消息ID，用于分页加载
    var earliestMsgId by remember { mutableStateOf("") }
    
    // 添加一个协程作用域用于加载更多消息
    val coroutineScope = rememberCoroutineScope()
    
    // 定义加载更多消息的函数
    fun loadMoreMessages(
        chatId: String,
        chatType: String,
        currentUserId: String
    ) {
        if (isLoadingMore) return // 如果已经在加载中，则不重复加载

        coroutineScope.launch {
            // 内部实现加载更多消息的逻辑
            Log.d(TAG, "开始加载更多历史消息，chatId=$chatId, earliestMsgId=$earliestMsgId")
            isLoadingMore = true

            try {
                // 创建请求对象，从最早的消息开始获取
                val request = io.github.shanfishapp.pureyunhu.proto.ListMessageSend(
                    msgCount = 50u,  // 获取50条消息
                    msgId = earliestMsgId,  // 从指定消息ID开始（实现分页）
                    chatType = if (chatType == "group") 2U else 1U,  // 转换chatType
                    chatId = chatId
                )

                // 获取token
                val token = TokenManager.get()

                // 将protobuf对象编码为字节数组
                val protobufData = kotlinx.serialization.protobuf.ProtoBuf.encodeToByteArray(io.github.shanfishapp.pureyunhu.proto.ListMessageSend.serializer(), request)
                val requestBody = protobufData.toRequestBody("application/x-protobuf".toMediaType())

                // 使用Retrofit调用API (suspend version)
                val resp = ApiClient.apiService.getListMessageSuspend(requestBody, token)

                try {
                    Log.d(TAG, "获取更多消息成功")
                    val respData = ProtoBuf.decodeFromByteArray<ListMessageResponse>(resp.bytes())

                    // 检查响应状态
                    if (respData.status?.code?.toLong() == 1L) {
                        Log.d(TAG, "获取更多历史消息成功，共${respData.msg.size}条消息")

                        // 将新消息添加到现有消息的前面
                        respData.msg.forEach { msg ->
                            val isMe = msg.sender!!.chatId == currentUserId
                            val textContent = msg.content?.text ?: ""
                            val senderInfo = msg.sender
                            val contentType = msg.contentType // 获取消息类型

                            // 检查消息是否已存在，避免重复添加
                            val existingMessage = messages.find { it.msgId == msg.msgId }
                            if (existingMessage == null) {
                                                            when (contentType) {
                                                                                    2UL -> { // 图片消息
                                                                                        val imageUrl = msg.content?.imageUrl ?: ""
                                                                                        MessageManager.addMessageImageItems(
                                                                                            chatId = chatId,
                                                                                            chatType = chatType,
                                                                                            msgId = msg.msgId,
                                                                                            text = textContent,
                                                                                            imageUrl = imageUrl,
                                                                                            senderId = senderInfo!!.chatId ?: "",
                                                                                            senderNickName = senderInfo?.name ?: "",
                                                                                            senderType = "user",  // 根据实际情况可能需要调整
                                                                                            senderAvatarUrl = senderInfo?.avatarUrl ?: "",
                                                                                            isMe = isMe
                                                                                        )
                                                                                    }
                                                                                    3UL -> { // Markdown消息
                                                                                        MessageManager.addMessageMarkdownItems(
                                                                                            chatId = chatId,
                                                                                            chatType = chatType,
                                                                                            msgId = msg.msgId,
                                                                                            text = textContent,
                                                                                            senderId = senderInfo!!.chatId ?: "",
                                                                                            senderNickName = senderInfo?.name ?: "",
                                                                                            senderType = "user",  // 根据实际情况可能需要调整
                                                                                            senderAvatarUrl = senderInfo?.avatarUrl ?: "",
                                                                                            isMe = isMe
                                                                                        )
                                                                                    }
                                                                                    8UL -> { // WebView消息
                                                                                        MessageManager.addMessageWebviewItems(
                                                                                            chatId = chatId,
                                                                                            chatType = chatType,
                                                                                            msgId = msg.msgId,
                                                                                            text = textContent,
                                                                                            senderId = senderInfo!!.chatId ?: "",
                                                                                            senderNickName = senderInfo?.name ?: "",
                                                                                            senderType = "user",  // 根据实际情况可能需要调整
                                                                                            senderAvatarUrl = senderInfo?.avatarUrl ?: "",
                                                                                            isMe = isMe
                                                                                        )
                                                                                    }
                                                                                    else -> { // 默认为文本消息
                                                                                        MessageManager.addMessageTextItems(
                                                                                            chatId = chatId,
                                                                                            chatType = chatType,
                                                                                            msgId = msg.msgId,
                                                                                            text = textContent,
                                                                                            senderId = senderInfo!!.chatId ?: "",
                                                                                            senderNickName = senderInfo?.name ?: "",
                                                                                            senderType = "user",  // 根据实际情况可能需要调整
                                                                                            senderAvatarUrl = senderInfo?.avatarUrl ?: "",
                                                                                            isMe = isMe
                                                                                        )
                                                                                    }
                                                                                }
                                Log.d(TAG, "添加历史消息: msgId=${msg.msgId}, text=${textContent}, isMe=$isMe, contentType=$contentType")
                            }
                        }

                        // 更新最早的msgId，用于下次分页加载
                        if (respData.msg.isNotEmpty()) {
                            // 最早的消息是列表中的最后一条（因为是从新到旧排列）
                            val lastMsg = respData.msg.last()
                            earliestMsgId = lastMsg.msgId
                        }
                    } else {
                        Log.e(TAG, "获取更多历史消息失败，状态码: ${respData.status?.code}, 消息: ${respData.status!!.msg}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "解析更多消息响应时出错", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载更多历史消息时出错", e)
            } finally {
                isLoadingMore = false
            }
        }
    }

    // 获取历史消息
    LaunchedEffect(key1 = chatId) {
        Log.d(TAG, "MessageScreen: LaunchedEffect触发，开始加载历史消息，chatId=$chatId")
        isLoading = true

        try {
            // 获取当前用户ID以判断isMe
            val currentUserId = io.github.shanfishapp.pureyunhu.utils.UserInfoManager.getCachedUserHomepageInfo()?.userId ?: ""
            Log.d(TAG, "当前用户ID: $currentUserId")

            // 创建请求对象
            val request = io.github.shanfishapp.pureyunhu.proto.ListMessageSend(
                msgCount = 50u,  // 获取50条消息
                msgId = "",     // 从最新开始
                chatType = if (chatType == "group") 2U else 1U ,  // 转换chatType
                chatId = chatId
            )

            // 获取token
            val token = TokenManager.get()

            // 将protobuf对象编码为字节数组
            val protobufData = kotlinx.serialization.protobuf.ProtoBuf.encodeToByteArray(io.github.shanfishapp.pureyunhu.proto.ListMessageSend.serializer(), request)
            val requestBody = protobufData.toRequestBody("application/x-protobuf".toMediaType())

            try {
                // 使用Retrofit调用API (suspend version)
                val resp = ApiClient.apiService.getListMessageSuspend(requestBody, token)
                Log.d(TAG, "获取群聊$chatId 的消息成功")
                val respData = ProtoBuf.decodeFromByteArray<ListMessageResponse>(resp.bytes())

                // 检查响应状态
                if (respData.status?.code?.toLong() == 1L) {
                    Log.d(TAG, "获取历史消息成功，共${respData.msg.size}条消息")

                    // 清除当前聊天的现有消息（因为是首次加载）
                    MessageManager.clearMessages(chatId)

                                            // 遍历响应中的消息并添加到MessageManager
                                            respData.msg.reversed().forEach { msg ->
                                                val isMe = msg.sender?.chatId == currentUserId
                                                val textContent = msg.content?.text ?: ""
                                                val senderInfo = msg.sender
                                                val contentType = msg.contentType // 获取消息类型
                    
                                                when (contentType) {
                                                    2UL -> { // 图片消息
                                                        val imageUrl = msg.content?.imageUrl ?: ""
                                                        MessageManager.addMessageImageItems(
                                                            chatId = chatId,
                                                            chatType = chatType,
                                                            msgId = msg.msgId,
                                                            text = textContent,
                                                            imageUrl = imageUrl,
                                                            senderId = senderInfo?.chatId ?: "",
                                                            senderNickName = senderInfo?.name ?: "",
                                                            senderType = "user",  // 根据实际情况可能需要调整
                                                            senderAvatarUrl = senderInfo?.avatarUrl ?: "",
                                                            isMe = isMe
                                                        )
                                                    }
                                                    3UL -> { // Markdown消息
                                                        MessageManager.addMessageMarkdownItems(
                                                            chatId = chatId,
                                                            chatType = chatType,
                                                            msgId = msg.msgId,
                                                            text = textContent,
                                                            senderId = senderInfo?.chatId ?: "",
                                                            senderNickName = senderInfo?.name ?: "",
                                                            senderType = "user",  // 根据实际情况可能需要调整
                                                            senderAvatarUrl = senderInfo?.avatarUrl ?: "",
                                                            isMe = isMe
                                                        )
                                                    }
                                                    else -> { // 默认为文本消息
                                                        MessageManager.addMessageTextItems(
                                                            chatId = chatId,
                                                            chatType = chatType,
                                                            msgId = msg.msgId,
                                                            text = textContent,
                                                            senderId = senderInfo?.chatId ?: "",
                                                            senderNickName = senderInfo?.name ?: "",
                                                            senderType = "user",  // 根据实际情况可能需要调整
                                                            senderAvatarUrl = senderInfo?.avatarUrl ?: "",
                                                            isMe = isMe
                                                        )
                                                    }
                                                }
                    
                                                Log.d(TAG, "添加消息: msgId=${msg.msgId}, text=${textContent}, isMe=$isMe, contentType=$contentType")
                                            }                } else {
                    Log.e(TAG, "获取历史消息失败，状态码: ${respData.status?.code}, 消息: ${respData.status?.msg}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取历史消息时出错", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取历史消息时出错", e)
        } finally {
            isLoading = false
        }
    }
    
    // 监听消息数量变化并自动滚动到底部
    val listState = rememberLazyListState()
    LaunchedEffect(key1 = messages.size) {
        Log.d(TAG, "MessageScreen: 消息数量发生变化，当前数量: ${messages.size}")
        // 自动滚动到底部
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
        }
    }
    
    // 监听滚动位置，当用户滚动到顶部附近时加载更多历史消息
    LaunchedEffect(key1 = listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { firstVisibleItemIndex ->
                if (firstVisibleItemIndex == 0 && !isLoadingMore && messages.isNotEmpty()) {
                    Log.d(TAG, "滚动到顶部，尝试加载更多历史消息")
                    // 获取当前用户ID以判断isMe
                    val currentUserId = io.github.shanfishapp.pureyunhu.utils.UserInfoManager.getCachedUserHomepageInfo()?.userId ?: ""
                    loadMoreMessages(chatId, chatType, currentUserId)
                }
            }
    }
    
    // 输入框状态
    var inputText by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chatName) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // 防止重复点击
                            if (!isNavigating) {
                                isNavigating = true
                                navController.popBackStack()
                            }
                        },
                        enabled = !isNavigating // 当正在导航时禁用按钮
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 消息列表
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                Log.d(TAG, "MessageScreen: 开始渲染消息列表，共有 ${messages.size} 条消息")
                items(messages) { message ->
                    Log.d(TAG, "MessageScreen: 渲染消息，text=${message.text}, isMe=${message.isMe}, senderNickName=${message.senderNickName}, senderAvatarUrl=${message.senderAvatarUrl}")
                    MessageItemView(message = message)
                }
            }
            
            // 预留图标行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(6) { index ->
                    Icon(
                        imageVector = when (index) {
                            0 -> Icons.Default.Send // 实际项目中应替换为表情图标
                            1 -> Icons.Default.Send // 实际项目中应替换为图片图标
                            2 -> Icons.Default.Send // 实际项目中应替换为文件图标
                            3 -> Icons.Default.Send // 实际项目中应替换为语音图标
                            4 -> Icons.Default.Send // 实际项目中应替换为视频图标
                            5 -> Icons.Default.Send // 实际项目中应替换为更多图标
                            else -> Icons.Default.Send
                        },
                        contentDescription = "图标 $index",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { /* 图标点击事件 */ },
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // 输入框和发送按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    placeholder = { Text("输入消息...") },
                    shape = RoundedCornerShape(24.dp)
                )
                
                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            Log.d(TAG, "MessageScreen: 发送消息，内容: $inputText")
                            // 发送消息
                            val currentUserId = io.github.shanfishapp.pureyunhu.utils.UserInfoManager.getCachedUserHomepageInfo()?.userId ?: "current_user"
                            
                            // 检查是否是markdown格式的消息（这里可以根据需要添加更复杂的判断逻辑）
                            val isMarkdown = inputText.startsWith("**") || inputText.startsWith("#") || 
                                             inputText.contains("](") || inputText.contains("`")
                            
                            if (isMarkdown) {
                                MessageManager.addMessageMarkdownItems(
                                    chatId = chatId,
                                    chatType = chatType,
                                    msgId = System.currentTimeMillis().toString(),
                                    text = inputText,
                                    senderId = currentUserId,
                                    senderNickName = io.github.shanfishapp.pureyunhu.utils.UserInfoManager.getCachedUserHomepageInfo()?.nickname!!,
                                    senderType = "user",
                                    senderAvatarUrl = io.github.shanfishapp.pureyunhu.utils.UserInfoManager.getCachedUserHomepageInfo()?.avatarUrl!!, // 当前用户头像URL
                                    isMe = true
                                )
                            } else {
                                MessageManager.addMessageTextItems(
                                    chatId = chatId,
                                    chatType = chatType,
                                    msgId = System.currentTimeMillis().toString(),
                                    text = inputText,
                                    senderId = currentUserId,
                                    senderNickName = io.github.shanfishapp.pureyunhu.utils.UserInfoManager.getCachedUserHomepageInfo()?.nickname!!,
                                    senderType = "user",
                                    senderAvatarUrl = io.github.shanfishapp.pureyunhu.utils.UserInfoManager.getCachedUserHomepageInfo()?.avatarUrl!!, // 当前用户头像URL
                                    isMe = true
                                )
                            }
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .height(56.dp)
                        .width(56.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "发送"
                    )
                }
            }
        }
    }
}

@Composable
fun MessageItemView(message: MessageItem) {
    val alignment = if (message.isMe) Alignment.End else Alignment.Start
    // 修改颜色：我的消息为浅紫色，对方消息为蓝色（原来我的消息的颜色）
    val color = if (message.isMe) Color(0xFFD1C4E9) else MaterialTheme.colorScheme.primary // 浅紫色: D1C4E9, 原来我的消息的颜色: primary
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isMe) {
            // 左侧消息显示头像
            AsyncImageRefer(
                imageUrl = message.senderAvatarUrl,
                contentDescription = "${message.senderNickName}的头像",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
        }
        
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .widthIn(max = 280.dp),
            horizontalAlignment = if (message.isMe) Alignment.End else Alignment.Start
        ) {
            if (!message.isMe) {
                Text(
                    text = message.senderNickName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            
            Card(
                modifier = Modifier,
                colors = CardDefaults.cardColors(
                    containerColor = color
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                when (message.messageType) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.text,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Start
                        )
                    }
                    MessageType.IMAGE -> {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            if (message.text.isNotBlank()) {
                                Text(
                                    text = message.text,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    textAlign = TextAlign.Start
                                )
                            }
                            message.imageUrl?.let { imageUrl ->
                                AsyncImageRefer(
                                    imageUrl = imageUrl,
                                    contentDescription = "图片消息",
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                        }
                    }
                    MessageType.MARKDOWN -> {
                        MarkdownParser.parseMarkdown(
                            markdown = message.text,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    MessageType.WEBVIEW -> {
                        // WebView消息显示
                        io.github.shanfishapp.pureyunhu.utils.WebviewUtils.WebviewContent(
                            url = message.text,
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                        )
                    }
                }
            }
        }
        
        if (message.isMe) {
            // 右侧消息显示头像
            AsyncImageRefer(
                imageUrl = message.senderAvatarUrl,
                contentDescription = "${message.senderNickName}的头像",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
        }
    }
}