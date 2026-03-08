package io.github.shanfishapp.pureyunhu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.shanfishapp.pureyunhu.api.ApiClient
import io.github.shanfishapp.pureyunhu.proto.ConversationListResponse
import io.github.shanfishapp.pureyunhu.utils.TokenManager
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// 数据类
data class ChatItem(
    val chatId: String,
    val chatName: String,
    val chatType: String, // "private", "group", "robot", "unknown"
    val avatarUrl: String?,
    val latestMsg: String,
    val latestMsgTime: Long // timestamp in milliseconds
)

// ViewModel 管理聊天数据
class ChatViewModel : ViewModel() {
    private val _chatItems = mutableStateListOf<ChatItem>()

    val chatItems: SnapshotStateList<ChatItem>
        get() = _chatItems

    /**
     * 添加聊天项
     * @param chatId 聊天ID
     * @param chatName 聊天名称
     * @param chatType 聊天类型（private/group）
     * @param avatarUrl 头像URL
     * @param latestMsg 最新消息
     * @param latestMsgTime 最新消息时间
     */
    fun addChatItem(
        chatId: String,
        chatName: String,
        chatType: String,
        avatarUrl: String?,
        latestMsg: String,
        latestMsgTime: Long = System.currentTimeMillis()
    ) {
        val newItem = ChatItem(
            chatId = chatId,
            chatName = chatName,
            chatType = chatType,
            avatarUrl = avatarUrl,
            latestMsg = latestMsg,
            latestMsgTime = latestMsgTime
        )

        // 查找是否已存在
        val existingIndex = _chatItems.indexOfFirst { it.chatId == chatId }

        if (existingIndex != -1) {
            // 如果存在，更新现有项
            _chatItems[existingIndex] = newItem
            // 如果不在顶部，移动到顶部
            if (existingIndex != 0) {
                _chatItems.removeAt(existingIndex)
                _chatItems.add(0, newItem)
            }
        } else {
            // 如果不存在，直接添加到顶部
            _chatItems.add(0, newItem)
        }

        // 添加日志以便调试
        android.util.Log.d("ChatViewModel", "添加聊天项: $chatName, 当前数量: ${_chatItems.size}")
    }

    /**
     * 批量添加聊天项
     */
    fun addChatItems(
        chatIds: List<String>,
        chatNames: List<String>,
        chatTypes: List<String>,
        avatarUrls: List<String?>,
        latestMsgs: List<String>,
        latestMsgTimes: List<Long>
    ) {
        require(chatIds.size == chatNames.size &&
                chatNames.size == chatTypes.size &&
                chatTypes.size == avatarUrls.size &&
                avatarUrls.size == latestMsgs.size &&
                latestMsgs.size == latestMsgTimes.size) {
            "所有参数列表的长度必须相同"
        }

        // 创建带索引的列表用于排序
        val items = chatIds.indices.map { index ->
            ChatItem(
                chatId = chatIds[index],
                chatName = chatNames[index],
                chatType = chatTypes[index],
                avatarUrl = avatarUrls[index],
                latestMsg = latestMsgs[index],
                latestMsgTime = latestMsgTimes[index]
            )
        }

        // 按时间排序
        val sortedItems = items.sortedByDescending { it.latestMsgTime }

        // 批量操作优化：先删除所有存在的项，再添加新项
        val existingIds = _chatItems.map { it.chatId }.toSet()
        val newItems = sortedItems.filter { it.chatId !in existingIds }
        val updateItems = sortedItems.filter { it.chatId in existingIds }

        // 移除所有需要更新的项
        updateItems.forEach { item ->
            _chatItems.removeAll { it.chatId == item.chatId }
        }

        // 添加所有新项和更新项到顶部
        _chatItems.addAll(0, sortedItems)

        android.util.Log.d("ChatViewModel", "批量添加: 新增 ${newItems.size}, 更新 ${updateItems.size}")
    }

    /**
     * 批量添加聊天项（使用可变参数）
     */
    fun addChatItems(vararg items: ChatItemParams) {
        val sortedItems = items.sortedByDescending { it.latestMsgTime }
        addChatItems(
            chatIds = sortedItems.map { it.chatId },
            chatNames = sortedItems.map { it.chatName },
            chatTypes = sortedItems.map { it.chatType },
            avatarUrls = sortedItems.map { it.avatarUrl },
            latestMsgs = sortedItems.map { it.latestMsg },
            latestMsgTimes = sortedItems.map { it.latestMsgTime }
        )
    }

    /**
     * 删除单个聊天项
     * @param chatId 要删除的聊天ID
     */
    fun delChatItem(chatId: String) {
        val removed = _chatItems.removeAll { it.chatId == chatId }
        if (removed) {
            android.util.Log.d("ChatViewModel", "删除聊天项: $chatId")
        }
    }

    /**
     * 批量删除聊天项
     * @param chatIds 要删除的聊天ID列表
     */
    fun delChatItems(chatIds: List<String>) {
        val removed = _chatItems.removeAll { it.chatId in chatIds }
        if (removed) {
            android.util.Log.d("ChatViewModel", "批量删除: ${chatIds.size} 个聊天项")
        }
    }

    /**
     * 批量删除聊天项（使用可变参数）
     * @param chatIds 要删除的聊天ID
     */
    fun delChatItems(vararg chatIds: String) {
        delChatItems(chatIds.toList())
    }

    /**
     * 删除指定类型的所有聊天项
     * @param chatType 聊天类型（private/group）
     */
    fun delChatItemsByType(chatType: String) {
        val removed = _chatItems.removeAll { it.chatType == chatType }
        if (removed) {
            android.util.Log.d("ChatViewModel", "删除类型 $chatType 的所有聊天项")
        }
    }

    /**
     * 删除指定时间之前的所有聊天项
     * @param beforeTime 时间戳，删除早于此时间的聊天
     */
    fun delChatItemsBeforeTime(beforeTime: Long) {
        val removed = _chatItems.removeAll { it.latestMsgTime < beforeTime }
        if (removed) {
            android.util.Log.d("ChatViewModel", "删除 $beforeTime 之前的聊天项")
        }
    }

    /**
     * 更新特定聊天项的最新消息
     * @param chatId 聊天ID
     * @param latestMsg 最新消息内容
     * @param latestMsgTime 最新消息时间（可选，默认当前时间）
     */
    fun updateLatestMessage(
        chatId: String,
        latestMsg: String,
        latestMsgTime: Long = System.currentTimeMillis()
    ) {
        val index = _chatItems.indexOfFirst { it.chatId == chatId }
        if (index != -1) {
            val item = _chatItems[index]
            val updatedItem = item.copy(
                latestMsg = latestMsg,
                latestMsgTime = latestMsgTime
            )

            // 如果在顶部，直接更新
            if (index == 0) {
                _chatItems[0] = updatedItem
            } else {
                // 如果不在顶部，移除原项并添加到顶部
                _chatItems.removeAt(index)
                _chatItems.add(0, updatedItem)
            }

            android.util.Log.d("ChatViewModel", "更新消息: $chatId")
        } else {
            android.util.Log.w("ChatViewModel", "更新失败: 找不到聊天项 $chatId")
        }
    }

    /**
     * 清空所有聊天项
     */
    fun clearAll() {
        _chatItems.clear()
        android.util.Log.d("ChatViewModel", "清空所有聊天项")
    }

    /**
     * 获取特定聊天项
     */
    fun getChatItem(chatId: String): ChatItem? {
        return _chatItems.find { it.chatId == chatId }
    }

    /**
     * 检查是否包含特定聊天项
     */
    fun containsChatItem(chatId: String): Boolean {
        return _chatItems.any { it.chatId == chatId }
    }

    /**
     * 获取聊天项数量
     */
    fun getItemCount(): Int = _chatItems.size
    
    /**
     * 从服务器获取对话列表
     */
    @OptIn(ExperimentalSerializationApi::class)
    fun fetchConversationList() {
        val token = TokenManager.get()
        if (token.isBlank()) {
            android.util.Log.e("ChatViewModel", "Token is blank, cannot fetch conversation list")
            return
        }
        
        val call = ApiClient.apiService.getConversationList(token)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        try {
                            val data = responseBody.bytes()
                            val conversationList = ProtoBuf.decodeFromByteArray(ConversationListResponse.serializer(), data)
                            
                            if (conversationList.status?.code == 1UL) {
                                // 清空现有列表并添加新的对话列表
                                clearAll()
                                
                                // 反向处理数据，让最底部的数据（最新的）最后添加，从而显示在顶部
                                conversationList.data.reversed().forEach { conversationData ->
                                    // 根据chatType确定chatType字符串
                                    val chatTypeStr = when (conversationData.chatType.toInt()) {
                                        1 -> "private"  // 用户
                                        2 -> "group"   // 群聊
                                        3 -> "robot"   // 机器人
                                        else -> "unknown"
                                    }
                                    
                                    addChatItem(
                                        chatId = conversationData.chatId,
                                        chatName = conversationData.name,
                                        chatType = chatTypeStr,
                                        avatarUrl = conversationData.avatarUrl,
                                        latestMsg = conversationData.chatContent,
                                        latestMsgTime = conversationData.timestampMs.toLong()
                                    )
                                }
                                
                                android.util.Log.d("ChatViewModel", "成功获取 ${conversationList.data.size} 个对话")
                            } else {
                                android.util.Log.e("ChatViewModel", "API error: ${conversationList.status?.msg}")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("ChatViewModel", "Failed to parse protobuf response: ${e.message}", e)
                        }
                    }
                } else {
                    android.util.Log.e("ChatViewModel", "Response failed: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                android.util.Log.e("ChatViewModel", "Failed to fetch conversation list: ${t.message}", t)
            }
        })
    }
}

/**
 * 聊天项参数类，用于批量添加
 */
data class ChatItemParams(
    val chatId: String,
    val chatName: String,
    val chatType: String,
    val avatarUrl: String?,
    val latestMsg: String,
    val latestMsgTime: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = viewModel()
) {
    // 在首次加载时获取对话列表
    androidx.compose.runtime.LaunchedEffect(key1 = Unit) {
        viewModel.fetchConversationList()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("聊天") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ChatList(
                chatItems = viewModel.chatItems,
                onItemClick = { chatItem ->
                    // 处理点击事件，导航到消息详情
                    android.util.Log.d("ChatScreen", "点击聊天项: ${chatItem.chatName}")
                    // 导航到消息详情页面，传递chat-id、chat-type和chat-name参数
                    val encodedChatName = java.net.URLEncoder.encode(
                        if (chatItem.chatName.length > 12) chatItem.chatName.take(12) + "..." else chatItem.chatName, 
                        "UTF-8"
                    )
                    navController.navigate("message?chat-id=${chatItem.chatId}&chat-type=${chatItem.chatType}&chat-name=$encodedChatName")
                }
            )
        }
    }
}

@Composable
fun ChatList(
    chatItems: SnapshotStateList<ChatItem>,
    onItemClick: (ChatItem) -> Unit
) {
    // 添加日志以便调试
    android.util.Log.d("ChatList", "渲染聊天列表, 项目数量: ${chatItems.size}")

    LazyColumn {
        items(
            items = chatItems,
            key = { it.chatId }
        ) { chatItem ->
            ChatItemView(
                chatItem = chatItem,
                onClick = { onItemClick(chatItem) }
            )
        }
    }
}

@Composable
fun ChatItemView(
    chatItem: ChatItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像（带加载失败处理）
        ChatAvatar(
            avatarUrl = chatItem.avatarUrl,
            chatName = chatItem.chatName,
            chatType = chatItem.chatType,
            modifier = Modifier.size(48.dp)
        )

        // 聊天信息
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 聊天名称
                Text(
                    text = chatItem.chatName.ifBlank { "未命名" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 时间
                Text(
                    text = formatTime(chatItem.latestMsgTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 最新消息
            Text(
                text = chatItem.latestMsg.ifBlank { "暂无消息" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ChatAvatar(
    avatarUrl: String?,
    chatName: String,
    chatType: String,
    modifier: Modifier = Modifier
) {
    var loadFailed by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val displayName = chatName.ifBlank { "未命名" }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .then(Modifier.size(48.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrEmpty() && !loadFailed) {
            // 尝试加载网络图片
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(avatarUrl)
                    .addHeader("Referer", "https://www.yhchat.com")
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = {
                    loadFailed = true
                    android.util.Log.d("ChatAvatar", "加载头像失败: $avatarUrl")
                },
                onSuccess = {
                    loadFailed = false
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // 加载失败或没有头像URL时显示备选
            if (displayName.isNotBlank()) {
                // 显示名字第一个字
                val firstChar = displayName.trim().firstOrNull()?.toString() ?: "?"
                
                // 根据chatType设置不同的背景色
                val backgroundColor = when (chatType) {
                    "private" -> Color.Blue
                    "group" -> Color.Green
                    "robot" -> Color.Magenta
                    else -> Color.Gray
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .then(Modifier.background(backgroundColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = firstChar,
                        color = Color.White,
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                // 名字为空或全空白，显示Person图标
                val backgroundColor = when (chatType) {
                    "private" -> Color.Blue
                    "group" -> Color.Green
                    "robot" -> Color.Magenta
                    else -> Color.Gray
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .then(Modifier.background(backgroundColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}




// 时间格式化函数
fun formatTime(timestamp: Long): String {
    val now = Calendar.getInstance()
    val messageTime = Calendar.getInstance().apply { timeInMillis = timestamp }

    val today = now.get(Calendar.DAY_OF_YEAR)
    val messageDay = messageTime.get(Calendar.DAY_OF_YEAR)
    val yearDiff = now.get(Calendar.YEAR) - messageTime.get(Calendar.YEAR)

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = timeFormat.format(Date(timestamp))

    return when {
        yearDiff == 0 -> {
            when (today - messageDay) {
                0 -> timeStr
                1 -> "昨天 $timeStr"
                2 -> "前天 $timeStr"
                in 3..6 -> {
                    val dayOfWeek = when (messageTime.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.MONDAY -> "星期一"
                        Calendar.TUESDAY -> "星期二"
                        Calendar.WEDNESDAY -> "星期三"
                        Calendar.THURSDAY -> "星期四"
                        Calendar.FRIDAY -> "星期五"
                        Calendar.SATURDAY -> "星期六"
                        Calendar.SUNDAY -> "星期日"
                        else -> ""
                    }
                    "$dayOfWeek $timeStr"
                }
                else -> {
                    val dateFormat = SimpleDateFormat("M月d日", Locale.getDefault())
                    "${dateFormat.format(Date(timestamp))} $timeStr"
                }
            }
        }
        else -> {
            val dateFormat = SimpleDateFormat("yyyy年M月d日", Locale.getDefault())
            "${dateFormat.format(Date(timestamp))} $timeStr"
        }
    }
}

// 生成示例数据
private fun generateSampleChatItems(): List<ChatItem> {
    val now = System.currentTimeMillis()
    val oneHour = 3600000
    val oneDay = 86400000

    return listOf(
        ChatItem(
            chatId = "1",
            chatName = "张三",
            chatType = "group",
            avatarUrl = null, // 测试无头像情况
            latestMsg = "好的，我们明天见",
            latestMsgTime = now - oneHour
        ),
        ChatItem(
            chatId = "2",
            chatName = "项目讨论组",
            chatType = "group",
            avatarUrl = "https://example.com/avatar.jpg", // 测试失败头像
            latestMsg = "李四：我完成了任务",
            latestMsgTime = now - (oneDay + oneHour)
        ),
        ChatItem(
            chatId = "3",
            chatName = "   ", // 测试空白名字
            chatType = "user",
            avatarUrl = null,
            latestMsg = "周末一起去爬山吗？",
            latestMsgTime = now - (2 * oneDay + oneHour)
        )
    )
}