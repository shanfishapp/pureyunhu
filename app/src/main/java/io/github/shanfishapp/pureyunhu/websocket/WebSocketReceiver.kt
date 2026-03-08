package io.github.shanfishapp.pureyunhu.websocket

import android.util.Log
import com.google.gson.Gson
import io.github.shanfishapp.pureyunhu.api.ApiClient
import io.github.shanfishapp.pureyunhu.managers.MessageManager
import io.github.shanfishapp.pureyunhu.models.BotInfoResponse
import io.github.shanfishapp.pureyunhu.models.GroupInfoResponse
import io.github.shanfishapp.pureyunhu.models.UserHomepageInfo
import io.github.shanfishapp.pureyunhu.proto.*
import io.github.shanfishapp.pureyunhu.ui.screens.ChatViewModel
import io.github.shanfishapp.pureyunhu.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.ByteString
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody

@OptIn(ExperimentalSerializationApi::class)
fun WebSocketReceiver(
    bytes: ByteString,
    viewModel: ChatViewModel
) {
    try {
        Log.d("WebSocketReceiver", "开始处理WebSocket消息，原始数据长度: ${bytes.size}")
        val message: WebSocketMessage = ProtoBuf.decodeFromByteArray(bytes.toByteArray())
        Log.d("WebSocketReceiver", "解码消息info: cmd=${message.info.cmd}, seq=${message.info.seq}")
        
        if (message.info.cmd == "push_message") {
            Log.d("WebSocketReceiver", "检测到推送消息命令")
            val data: PushMessage = ProtoBuf.decodeFromByteArray(bytes.toByteArray())
            Log.d("WebSocketReceiver", "解析PushMessage成功")

            data.data?.let { messageData ->
                messageData.msg?.let { msg ->
                    Log.d("WebSocketReceiver", "处理消息: chatId=${msg.chatId}, content=${msg.content.text}, contentType=${msg.contentType}, sender=${msg.sender?.name}")
                    
                    // 在主线程上下文中更新UI
                    CoroutineScope(Dispatchers.Main).launch {
                        Log.d("WebSocketReceiver", "在主线程中更新UI")
                        // 检查聊天项是否已存在
                        val existingChatItem = viewModel.getChatItem(msg.chatId)
                        val token = TokenManager.get()
                        
                        Log.d("WebSocketReceiver", "将消息添加到MessageManager")
                        // 添加消息到MessageManager，这样MessageScreen可以显示消息
                        addMessageToManager(msg)
                        
                        Log.d("WebSocketReceiver", "消息添加到MessageManager完成")
                        
                        if (existingChatItem != null) {
                            Log.d("WebSocketReceiver", "聊天项已存在，更新最新消息")
                            // 如果聊天项已存在，只更新消息内容和时间戳
                            // 使用发送者名称和头像，而不是从API获取
                            when (msg.contentType.toInt()) {
                                1 -> {
                                    viewModel.updateLatestMessage(
                                        msg.chatId,
                                        msg.content.text,
                                        msg.timestamp.toLong()
                                    )
                                }
                                2 -> {
                                    viewModel.updateLatestMessage(
                                        msg.chatId,
                                        "MarkDown 消息",
                                        msg.timestamp.toLong()
                                    )
                                }
                                8 -> {
                                    viewModel.updateLatestMessage(
                                        msg.chatId,
                                        "HTML 消息",
                                        msg.timestamp.toLong()
                                    )
                                }
                            }

                        } else {
                            Log.d("WebSocketReceiver", "聊天项不存在，根据chatType获取相关信息")
                            // 如果聊天项不存在，根据chatType获取相关信息
                            if (msg.sender!!.chatType.toInt() == 3) {
                                // 创建请求体
                                val gson = Gson()
                                val requestBody = mapOf("botId" to msg.sender.chatId)
                                val json = gson.toJson(requestBody)
                                val body = json.toRequestBody("application/json".toMediaType())

                                val call = ApiClient.apiService.getBotInfo(body, token)
                                call.enqueue(object : Callback<ResponseBody> {
                                    override fun onResponse(
                                        call: Call<ResponseBody>,
                                        response: Response<ResponseBody>
                                    ) {
                                        Log.d("WebSocketReceiver", "获取机器人信息响应: ${response.code()}")
                                        if (response.isSuccessful) {
                                            val responseBody = response.body()
                                            if (responseBody != null) {
                                                try {
                                                    val botInfoResponse = gson.fromJson(
                                                        responseBody.string(),
                                                        BotInfoResponse::class.java
                                                    )
                                                    Log.d("WebSocketReceiver", "解析机器人信息: code=${botInfoResponse.code}")
                                                    if (botInfoResponse.code == 1) {
                                                        val bot = botInfoResponse.data.bot
                                                        val name = bot.nickname
                                                        val avatarUrl = bot.avatarUrl
                                                        Log.d("WebSocketReceiver", "获取机器人信息成功: $name")

                                                        when (msg.contentType.toInt()) {
                                                            1 -> {
                                                                viewModel.addChatItem(
                                                                    msg.sender.chatId,
                                                                    name,
                                                                    "robot",
                                                                    avatarUrl,
                                                                    msg.content.text,
                                                                    msg.timestamp.toLong()
                                                                )
                                                            }

                                                            2 -> {
                                                                viewModel.addChatItem(
                                                                    msg.sender.chatId,
                                                                    name,
                                                                    "robot",
                                                                    avatarUrl,
                                                                    "MarkDown 消息",
                                                                    msg.timestamp.toLong()
                                                                )
                                                            }

                                                            8 -> {
                                                                viewModel.addChatItem(
                                                                    msg.sender.chatId,
                                                                    name,
                                                                    "robot",
                                                                    avatarUrl,
                                                                    "HTML 消息",
                                                                    msg.timestamp.toLong()
                                                                )
                                                            }

                                                            else -> {
                                                                viewModel.addChatItem(
                                                                    msg.sender.chatId,
                                                                    name,
                                                                    "robot",
                                                                    avatarUrl,
                                                                    msg.content.text,
                                                                    msg.timestamp.toLong()
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        Log.d("WebSocketReceiver", "API返回错误码: ${botInfoResponse.code}")
                                                        // API错误时使用默认值
                                                        addChatItemWithDefaults(viewModel, msg)
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e(
                                                        "WebSocketReceiver",
                                                        "解析机器人信息失败",
                                                        e
                                                    )
                                                    addChatItemWithDefaults(viewModel, msg)
                                                }
                                            }
                                        } else {
                                            Log.e("WebSocketReceiver", "获取机器人信息失败，响应码: ${response.code()}")
                                            // 获取失败时使用默认值
                                            addChatItemWithDefaults(viewModel, msg)
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<ResponseBody>,
                                        t: Throwable
                                    ) {
                                        Log.e("WebSocketReceiver", "获取机器人信息失败", t)
                                        // 获取失败时使用默认值
                                        addChatItemWithDefaults(viewModel, msg)
                                    }
                                })
                            } else {
                            when (msg.chatType.toInt()) {
                                1 -> { // 用户
                                    Log.d("WebSocketReceiver", "处理用户消息，获取用户信息")
                                    // 使用现有方法获取用户信息
                                    val call = ApiClient.apiService.getUserHomepageInfo(
                                        token = token,
                                        userId = msg.chatId
                                    )
                                    call.enqueue(object : Callback<UserHomepageInfo> {
                                        override fun onResponse(
                                            call: Call<UserHomepageInfo>,
                                            response: Response<UserHomepageInfo>
                                        ) {
                                            Log.d("WebSocketReceiver", "获取用户信息响应: ${response.code()}")
                                            if (response.isSuccessful) {
                                                val userInfo = response.body()
                                                val name = userInfo?.data?.user?.nickname ?: "用户"
                                                val avatarUrl = userInfo?.data?.user?.avatarUrl
                                                Log.d("WebSocketReceiver", "获取用户信息成功: $name")

                                                when (msg.contentType.toInt()) {
                                                    1 -> {
                                                        viewModel.addChatItem(
                                                            msg.chatId,
                                                            name,
                                                            "private",
                                                            avatarUrl,
                                                            msg.content.text,
                                                            msg.timestamp.toLong()
                                                        )
                                                    }

                                                    2 -> {
                                                        viewModel.addChatItem(
                                                            msg.chatId,
                                                            name,
                                                            "private",
                                                            avatarUrl,
                                                            "MarkDown 消息",
                                                            msg.timestamp.toLong()
                                                        )
                                                    }

                                                    8 -> {
                                                        viewModel.addChatItem(
                                                            msg.chatId,
                                                            name,
                                                            "private",
                                                            avatarUrl,
                                                            "HTML 消息",
                                                            msg.timestamp.toLong()
                                                        )
                                                    }

                                                    else -> {
                                                        viewModel.addChatItem(
                                                            msg.chatId,
                                                            name,
                                                            "private",
                                                            avatarUrl,
                                                            msg.content.text,
                                                            msg.timestamp.toLong()
                                                        )
                                                    }
                                                }
                                            } else {
                                                Log.e("WebSocketReceiver", "获取用户信息失败，响应码: ${response.code()}")
                                                // 如果获取用户信息失败，使用默认值
                                                addChatItemWithDefaults(viewModel, msg)
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<UserHomepageInfo>,
                                            t: Throwable
                                        ) {
                                            Log.e("WebSocketReceiver", "获取用户信息失败", t)
                                            // 获取失败时使用默认值
                                            addChatItemWithDefaults(viewModel, msg)
                                        }
                                    })
                                }

                                2 -> { // 群聊
                                    Log.d("WebSocketReceiver", "处理群聊消息，获取群聊信息")
                                    // 创建请求体
                                    val gson = Gson()
                                    val requestBody = mapOf("groupId" to msg.chatId)
                                    val json = gson.toJson(requestBody)
                                    val body = json.toRequestBody("application/json".toMediaType())

                                    val call = ApiClient.apiService.getGroupInfo(body, token)
                                    call.enqueue(object : Callback<ResponseBody> {
                                        override fun onResponse(
                                            call: Call<ResponseBody>,
                                            response: Response<ResponseBody>
                                        ) {
                                            Log.d("WebSocketReceiver", "获取群聊信息响应: ${response.code()}")
                                            if (response.isSuccessful) {
                                                val responseBody = response.body()
                                                if (responseBody != null) {
                                                    try {
                                                        val groupInfoResponse = gson.fromJson(
                                                            responseBody.string(),
                                                            GroupInfoResponse::class.java
                                                        )
                                                        Log.d("WebSocketReceiver", "解析群聊信息: code=${groupInfoResponse.code}")
                                                        if (groupInfoResponse.code == 1) {
                                                            val group = groupInfoResponse.data.group
                                                            val name = group.name
                                                            val avatarUrl = group.avatarUrl
                                                            Log.d("WebSocketReceiver", "获取群聊信息成功: $name")

                                                            when (msg.contentType.toInt()) {
                                                                1 -> {
                                                                    viewModel.addChatItem(
                                                                        msg.chatId,
                                                                        name,
                                                                        "group",
                                                                        avatarUrl,
                                                                        msg.content.text,
                                                                        msg.timestamp.toLong()
                                                                    )
                                                                }

                                                                2 -> {
                                                                    viewModel.addChatItem(
                                                                        msg.chatId,
                                                                        name,
                                                                        "group",
                                                                        avatarUrl,
                                                                        "MarkDown 消息",
                                                                        msg.timestamp.toLong()
                                                                    )
                                                                }

                                                                8 -> {
                                                                    viewModel.addChatItem(
                                                                        msg.chatId,
                                                                        name,
                                                                        "group",
                                                                        avatarUrl,
                                                                        "HTML 消息",
                                                                        msg.timestamp.toLong()
                                                                    )
                                                                }

                                                                else -> {
                                                                    viewModel.addChatItem(
                                                                        msg.chatId,
                                                                        name,
                                                                        "group",
                                                                        avatarUrl,
                                                                        msg.content.text,
                                                                        msg.timestamp.toLong()
                                                                    )
                                                                }
                                                            }
                                                        } else {
                                                            Log.d("WebSocketReceiver", "群聊API返回错误码: ${groupInfoResponse.code}")
                                                            // API错误时使用默认值
                                                            addChatItemWithDefaults(viewModel, msg)
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.e(
                                                            "WebSocketReceiver",
                                                            "解析群聊信息失败",
                                                            e
                                                        )
                                                        addChatItemWithDefaults(viewModel, msg)
                                                    }
                                                }
                                            } else {
                                                Log.e("WebSocketReceiver", "获取群聊信息失败，响应码: ${response.code()}")
                                                // 获取失败时使用默认值
                                                addChatItemWithDefaults(viewModel, msg)
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<ResponseBody>,
                                            t: Throwable
                                        ) {
                                            Log.e("WebSocketReceiver", "获取群聊信息失败", t)
                                            // 获取失败时使用默认值
                                            addChatItemWithDefaults(viewModel, msg)
                                        }
                                    })
                                }


                                else -> {
                                    Log.d("WebSocketReceiver", "未知的chatType: ${msg.chatType}，使用默认值")
                                    // 如果chatType不是1、2、3，则使用默认方式添加
                                    addChatItemWithDefaults(viewModel, msg)
                                }
                            }
                            }
                        }
                    }
                }
            }
        } else {
            Log.d("WebSocketReceiver", "接收到的消息不是push_message类型: ${message.info.cmd}")
        }
    } catch (e: Exception) {
        Log.e("WebSocketReceiver", "处理WebSocket消息时出错", e)
    }
}

// 添加消息到MessageManager
private fun addMessageToManager(msg: PushMessage.Msg) {
    Log.d("WebSocketReceiver", "准备添加消息到MessageManager: chatId=${msg.chatId}, text=${msg.content.text}")
    // 确定是否是当前用户的消息 (WebSocket推送的消息通常是别人发来的消息)
    // 如果发送者的ID与当前用户ID匹配，则isMe为true，否则为false
    // 这里暂时设置为false，因为从WebSocket接收的消息通常不是当前用户发送的
    val isMe = msg.sender!!.chatId == msg.recvId
    
    MessageManager.addMessageTextItems(
        chatId = msg.chatId,
        chatType = msg.chatType.toString(),
        msgId = msg.msgId,
        text = msg.content.text,
        senderId = msg.sender?.chatId ?: "unknown",
        senderNickName = msg.sender?.name ?: "未知用户",
        senderType = when (msg.chatType.toInt()) {
            1 -> "private"
            2 -> "group"
            3 -> "robot"
            else -> "unknown"
        },
        senderAvatarUrl = msg.sender?.avatarUrl ?: "",
        isMe = isMe
    )
    Log.d("WebSocketReceiver", "消息已添加到MessageManager")
}

// 辅助函数：使用默认值添加聊天项
private fun addChatItemWithDefaults(viewModel: ChatViewModel, msg: PushMessage.Msg) {
    when (msg.contentType.toInt()) {
        1 -> {
            viewModel.addChatItem(
                msg.chatId,
                when (msg.chatType.toInt()) {
                    1 -> "用户"
                    2 -> "群组"
                    3 -> "机器人"
                    else -> "未知"
                },
                when (msg.chatType.toInt()) {
                    1 -> "private"
                    2 -> "group"
                    3 -> "robot"
                    else -> "unknown"
                },
                msg.sender?.avatarUrl,
                msg.content.text,
                msg.timestamp.toLong()
            )
        }
        2 -> {
            viewModel.addChatItem(
                msg.chatId,
                when (msg.chatType.toInt()) {
                    1 -> "用户"
                    2 -> "群组"
                    3 -> "机器人"
                    else -> "未知"
                },
                when (msg.chatType.toInt()) {
                    1 -> "private"
                    2 -> "group"
                    3 -> "robot"
                    else -> "unknown"
                },
                msg.sender?.avatarUrl,
                "MarkDown 消息",
                msg.timestamp.toLong()
            )
        }
        8 -> {
            viewModel.addChatItem(
                msg.chatId,
                when (msg.chatType.toInt()) {
                    1 -> "用户"
                    2 -> "群组"
                    3 -> "机器人"
                    else -> "未知"
                },
                when (msg.chatType.toInt()) {
                    1 -> "private"
                    2 -> "group"
                    3 -> "robot"
                    else -> "unknown"
                },
                msg.sender?.avatarUrl,
                "HTML 消息",
                msg.timestamp.toLong()
            )
        }
        else -> {
            viewModel.addChatItem(
                msg.chatId,
                when (msg.chatType.toInt()) {
                    1 -> "用户"
                    2 -> "群组"
                    3 -> "机器人"
                    else -> "未知"
                },
                when (msg.chatType.toInt()) {
                    1 -> "private"
                    2 -> "group"
                    3 -> "robot"
                    else -> "unknown"
                },
                msg.sender?.avatarUrl,
                msg.content.text,
                msg.timestamp.toLong()
            )
        }
    }
}