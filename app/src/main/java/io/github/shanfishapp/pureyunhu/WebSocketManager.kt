package io.github.shanfishapp.pureyunhu

import android.util.Log
import io.github.shanfishapp.pureyunhu.managers.MessageManager
import io.github.shanfishapp.pureyunhu.models.WsLoginRequest
import io.github.shanfishapp.pureyunhu.ui.screens.ChatViewModel
import io.github.shanfishapp.pureyunhu.utils.TokenManager
import io.github.shanfishapp.pureyunhu.utils.UserInfoManager
import io.github.shanfishapp.pureyunhu.websocket.WebSocketClient
import io.github.shanfishapp.pureyunhu.websocket.WebSocketReceiver
import kotlinx.serialization.json.Json
import okhttp3.Response
import okio.ByteString
import java.util.UUID

object WebSocketManager {
    private var client: WebSocketClient? = null
    private var heartbeatTimer: java.util.Timer? = null
    private var viewModel: ChatViewModel? = null
    private var isStarted = false

    fun initialize(viewModel: ChatViewModel) {
        if (!isStarted) {
            this.viewModel = viewModel
            startWebSocket()
            isStarted = true
        } else {
            // 如果已经启动，更新viewModel
            this.viewModel = viewModel
        }
    }

    private fun startWebSocket() {
        client = WebSocketClient("wss://chat-ws-go.jwzhd.com/ws")

        client?.setListener(object : WebSocketClient.WebSocketListener {

            override fun onMessage(bytes: ByteString) {
                if (viewModel != null) {
                    WebSocketReceiver(bytes, viewModel!!)
                } else {
                    Log.w("WebSocketManager", "ViewModel is null, cannot process message")
                }
            }

            override fun onOpen(response: Response?) {
                // 启动一个线程来等待用户信息加载
                Thread {
                    var userHomepageInfo = UserInfoManager.getCachedUserHomepageInfo()
                    var waitCount = 0
                    val maxWaitTime = 30 // 最多等待30秒

                    // 循环等待用户信息，每秒检查一次
                    while (userHomepageInfo == null && waitCount < maxWaitTime) {
                        Thread.sleep(1000) // 等待1秒
                        waitCount++
                        userHomepageInfo = UserInfoManager.getCachedUserHomepageInfo()
                        Log.d("WebSocketManager", "等待用户信息加载... 已等待 ${waitCount} 秒")
                    }

                    // 检查是否获取到用户信息
                    if (userHomepageInfo == null) {
                        Log.e("WebSocketManager", "等待超时（${maxWaitTime}秒），用户信息仍未加载完成，关闭WebSocket连接")
                        client?.close(1001, "User info not available after timeout")
                        return@Thread
                    }

                    Log.d("WebSocketManager", "用户信息加载成功，开始WebSocket登录")

                    // 发送登录数据
                    val loginData = WsLoginRequest(
                        UUID.randomUUID().toString(),
                        "login",
                        WsLoginRequest.Data(
                            userHomepageInfo.userId,
                            TokenManager.get(),
                            "android",
                            "pureyunhu"
                        )
                    )
                    client?.send(Json.encodeToString(loginData))

                    // 启动心跳
                    heartbeatTimer = java.util.Timer()
                    heartbeatTimer?.schedule(object : java.util.TimerTask() {
                        override fun run() {
                            if (client?.isOpen == true) {
                                val heartbeatData: Map<String, String> = mapOf(
                                    "seq" to (System.currentTimeMillis().toString() + (1000..9999).random()),
                                    "cmd" to "heartbeat",
                                    "data" to "{}"  // 将空Map转为JSON字符串
                                )
                                client?.send(Json.encodeToString(heartbeatData))
                            } else {
                                heartbeatTimer?.cancel()
                            }
                        }
                    }, 30000, 30000)
                }.start()
            }

            override fun onClosed(code: Int, reason: String?) {
                heartbeatTimer?.cancel()
                Log.d("WebSocketManager", "WebSocket连接已关闭: $code, $reason")
                isStarted = false // 重置启动状态，允许重新连接
            }

            override fun onFailure(t: Throwable?, response: Response?) {
                heartbeatTimer?.cancel()
                Log.e("WebSocketManager", "WebSocket连接失败", t)
                isStarted = false // 重置启动状态，允许重新连接
            }
        })

        client?.connect()
    }

    fun sendMessage(message: String): Boolean {
        return client?.send(message) ?: false
    }

    fun isConnected(): Boolean {
        return client?.isConnected ?: false
    }

    fun close() {
        client?.close()
        heartbeatTimer?.cancel()
        isStarted = false
    }
}