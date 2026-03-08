package io.github.shanfishapp.pureyunhu.websocket

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * 基于 OkHttp 的基础 WebSocket 客户端
 *
 * 使用示例:
 * <pre>
 * WebSocketClient client = new WebSocketClient("wss://echo.websocket.org");
 * client.setListener(new WebSocketClient.WebSocketListener() {
 *     public void onMessage(String text) {
 *         System.out.println("收到消息: " + text);
 *     }
 * });
 * client.connect();
 * client.send("Hello WebSocket!");
 * </pre>
 */
class WebSocketClient @JvmOverloads constructor(
    private val url: String,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS) // 心跳间隔
        .retryOnConnectionFailure(true) // 连接失败时重试
        .build()
) {
    private var webSocket: WebSocket? = null
    private var listener: WebSocketListener? = null
    private var scheduledExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    /**
     * 是否已连接
     */
    var isConnected: Boolean = false
        private set

    /**
     * 是否已打开（连接中或已连接）
     * 这是原始代码中有的属性
     */
    var isOpen: Boolean = false
        private set

    // 重连相关属性
    private var reconnecting: Boolean = false
    private var reconnectCount: Int = 0

    init {
        Log.d("WebSocketClient", "WebSocketClient created with url: $url")
        Log.d("WebSocketClient", "Initial state - isOpen: $isOpen, isConnected: $isConnected")
    }

    /**
     * 设置 WebSocket 事件监听器
     * @param listener 监听器
     */
    fun setListener(listener: WebSocketListener?) {
        Log.d("WebSocketClient", "Listener set: $listener")
        this.listener = listener
    }

    /**
     * 连接 WebSocket 服务器
     */
    fun connect() {
        Log.d("WebSocketClient", "connect() called with url: $url")
        Log.d("WebSocketClient", "Current state before connect - isOpen: $isOpen, isConnected: $isConnected")

        // 如果已经打开，先关闭
        if (isOpen) {
            Log.d("WebSocketClient", "WebSocket is already open, closing it first")
            close(1000, "Reconnecting")
        }

        // 设置为打开状态（连接中）
        isOpen = true
        Log.d("WebSocketClient", "isOpen set to true (connecting...)")

        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "PureYunHu-Android") // 添加用户代理
            .build()

        Log.d("WebSocketClient", "Creating new WebSocket connection...")

        client.newWebSocket(request, object : okhttp3.WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocketClient", "✅ onOpen: Connection established!")
                Log.d("WebSocketClient", "Response: ${response.code} ${response.message}")

                this@WebSocketClient.webSocket = webSocket
                isConnected = true
                // isOpen 继续保持 true
                reconnecting = false // 重置重连标志
                reconnectCount = 0   // 重置重连计数

                Log.d("WebSocketClient", "State updated - isOpen: $isOpen, isConnected: $isConnected, reconnecting: $reconnecting, reconnectCount: $reconnectCount")

                // 记录协议和扩展信息
                val protocol = response.header("Sec-WebSocket-Protocol")
                val extensions = response.header("Sec-WebSocket-Extensions")
                Log.d("WebSocketClient", "Protocol: $protocol, Extensions: $extensions")

                listener?.onOpen(response)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocketClient", "📩 onMessage: Received text message")
                Log.d("WebSocketClient", "Message content: $text")
                Log.d("WebSocketClient", "Current state - isOpen: $isOpen, isConnected: $isConnected")

                // 将文本消息转换为 ByteString 以兼容现有接口
                listener?.onMessage(text.encodeUtf8())

                // 同时也调用文本消息监听（如果接口有的话）
                if (listener is WebSocketListener) {
                    (listener as WebSocketListener).onMessage(text)
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d("WebSocketClient", "📩 onMessage: Received binary message, size: ${bytes.size} bytes")
                Log.d("WebSocketClient", "Current state - isOpen: $isOpen, isConnected: $isConnected")

                // 尝试将二进制数据解析为文本（如果是文本的话）
                try {
                    val text = bytes.utf8()
                    Log.d("WebSocketClient", "Binary message as text: $text")
                } catch (e: Exception) {
                    Log.d("WebSocketClient", "Binary message is not UTF-8 text")
                }

                listener?.onMessage(bytes)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocketClient", "🔒 onClosed: code=$code, reason=$reason")
                isConnected = false
                isOpen = false
                this@WebSocketClient.webSocket = null

                Log.d("WebSocketClient", "State updated - isOpen: $isOpen, isConnected: $isConnected")
                listener?.onClosed(code, reason)
                
                // 开始自动重连
                startReconnect()
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocketClient", "🔑 onClosing: code=$code, reason=$reason")
                Log.d("WebSocketClient", "Current state - isOpen: $isOpen, isConnected: $isConnected")

                listener?.onClosing(code, reason)
                // 通常在这里可以发送关闭帧，但 OkHttp 会自动处理
                webSocket.close(code, reason)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocketClient", "❌ onFailure: ${t.message}", t)

                if (response != null) {
                    Log.e("WebSocketClient", "Response code: ${response.code}")
                    Log.e("WebSocketClient", "Response message: ${response.message}")
                }

                isConnected = false
                isOpen = false
                this@WebSocketClient.webSocket = null

                Log.e("WebSocketClient", "State updated after failure - isOpen: $isOpen, isConnected: $isConnected")
                listener?.onFailure(t, response)
                
                // 开始自动重连
                startReconnect()
            }
        })

        Log.d("WebSocketClient", "connect() completed, connection is in progress asynchronously")
        Log.d("WebSocketClient", "Final state after connect call - isOpen: $isOpen, isConnected: $isConnected")

        // 注意：不要调用 client.dispatcher.executorService.shutdown()！
        // 这会导致线程池关闭，WebSocket无法正常工作
    }

    /**
     * 启动自动重连机制
     */
    private fun startReconnect() {
        // 检查是否已经在重连中，避免重复重连
        if (reconnecting) {
            Log.d("WebSocketClient", "Reconnection already in progress, skipping...")
            return
        }
        
        if (reconnectCount > 0) {
            Log.d("WebSocketClient", "尝试重连，第 ${reconnectCount} 次...")
        } else {
            Log.d("WebSocketClient", "连接断开，开始自动重连...")
        }
        
        reconnecting = true
        reconnectCount++
        Log.d("WebSocketClient", "重连次数: $reconnectCount")

        // 使用ScheduledExecutorService延迟5秒后重连
        scheduledExecutor.schedule({
            Log.d("WebSocketClient", "开始执行重连...")
            connect()
        }, 5, TimeUnit.SECONDS)
    }

    /**
     * 发送文本消息
     * @param message 消息内容
     * @return 是否发送成功
     */
    fun send(message: String): Boolean {
        Log.d("WebSocketClient", "send() called, isOpen=$isOpen, isConnected=$isConnected")
        Log.d("WebSocketClient", "Message to send: $message")

        return if (webSocket != null && isConnected) {
            val result = webSocket!!.send(message)
            Log.d("WebSocketClient", "send result: $result")
            result
        } else {
            Log.e("WebSocketClient", "❌ Cannot send message - WebSocket is not connected")
            Log.e("WebSocketClient", "webSocket is null: ${webSocket == null}, isConnected: $isConnected, isOpen: $isOpen")
            false
        }
    }

    /**
     * 发送二进制消息
     * @param bytes 二进制数据
     * @return 是否发送成功
     */
    fun send(bytes: ByteString): Boolean {
        Log.d("WebSocketClient", "send(bytes) called, size=${bytes.size} bytes, isOpen=$isOpen, isConnected=$isConnected")

        return if (webSocket != null && isConnected) {
            val result = webSocket!!.send(bytes)
            Log.d("WebSocketClient", "send bytes result: $result")
            result
        } else {
            Log.e("WebSocketClient", "❌ Cannot send bytes - WebSocket is not connected")
            false
        }
    }

    /**
     * 关闭连接
     * @param code 关闭状态码，通常为 1000 (正常关闭)
     * @param reason 关闭原因
     */
    @JvmOverloads
    fun close(code: Int = 1000, reason: String? = "Normal closure") {
        Log.d("WebSocketClient", "close() called, code=$code, reason=$reason")
        Log.d("WebSocketClient", "Current state before close - isOpen: $isOpen, isConnected: $isConnected")

        if (webSocket != null) {
            Log.d("WebSocketClient", "Closing WebSocket connection")
            webSocket!!.close(code, reason)
            webSocket = null
            isConnected = false
            isOpen = false
            reconnecting = false // 停止重连
            Log.d("WebSocketClient", "WebSocket closed, state updated - isOpen: $isOpen, isConnected: $isConnected, reconnecting: $reconnecting")
        } else {
            Log.d("WebSocketClient", "WebSocket already null, setting states to false")
            isConnected = false
            isOpen = false
            reconnecting = false
        }
    }

    /**
     * 获取当前连接状态
     */
    fun getConnectionState(): String {
        return when {
            isOpen && isConnected -> "Connected"
            isOpen && !isConnected -> "Connecting"
            !isOpen && isConnected -> "Inconsistent State (Error)"
            else -> "Disconnected"
        }
    }

    /**
     * 取消当前连接（立即关闭）
     */
    fun cancel() {
        Log.d("WebSocketClient", "cancel() called")
        Log.d("WebSocketClient", "Current state before cancel - isOpen: $isOpen, isConnected: $isConnected")

        webSocket?.cancel()
        webSocket = null
        isConnected = false
        isOpen = false
        reconnecting = false // 停止重连

        Log.d("WebSocketClient", "State after cancel - isOpen: $isOpen, isConnected: $isConnected, reconnecting: $reconnecting")
    }

    /**
     * 销毁资源，清理定时任务
     */
    fun destroy() {
        Log.d("WebSocketClient", "destroy() called")
        cancel()
        scheduledExecutor.shutdown()
        try {
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            scheduledExecutor.shutdownNow()
            Thread.currentThread().interrupt()
        }
        Log.d("WebSocketClient", "WebSocketClient destroyed, scheduledExecutor shut down")
    }

    /**
     * WebSocket 事件监听器接口
     */
    interface WebSocketListener {
        /**
         * 连接打开时回调
         */
        fun onOpen(response: Response?) {}

        /**
         * 收到二进制消息时回调
         */
        fun onMessage(bytes: ByteString) {}

        /**
         * 收到文本消息时回调（可选）
         */
        fun onMessage(text: String) {}

        /**
         * 连接关闭时回调
         */
        fun onClosed(code: Int, reason: String?) {}

        /**
         * 连接正在关闭时回调
         */
        fun onClosing(code: Int, reason: String?) {}

        /**
         * 连接失败时回调
         */
        fun onFailure(t: Throwable?, response: Response?) {}
    }
}