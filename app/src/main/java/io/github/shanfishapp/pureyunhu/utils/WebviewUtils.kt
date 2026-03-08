package io.github.shanfishapp.pureyunhu.utils

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * WebviewUtils 工具类，用于处理 WebView 相关功能
 */
object WebviewUtils {
    
    /**
     * 运行 WebView 来显示指定 URL 的内容
     * 
     * @param url 要显示的 URL
     * @param context Android 上下文
     * @return 配置好的 WebView 实例
     */
    fun createWebView(context: Context, url: String): WebView {
        val webView = WebView(context)
        
        // 获取 WebView 设置
        val webSettings: WebSettings = webView.settings
        
        // 安全设置 - 重要：禁用 JavaScript 以防止 XSS 攻击
        webSettings.javaScriptEnabled = false
        webSettings.javaScriptCanOpenWindowsAutomatically = false
        
        // 禁用文件访问
        webSettings.allowFileAccess = false
        webSettings.allowContentAccess = false
        
        // 设置其他安全选项
        webSettings.allowFileAccessFromFileURLs = false
        webSettings.allowUniversalAccessFromFileURLs = false
        
        // 禁用表单自动填充
        webSettings.saveFormData = false
        
        // 禁用密码保存
        webSettings.savePassword = false
        
        // 设置用户代理
        webSettings.userAgentString = "PureYunhu-WebView/1.0"
        
        // 设置 WebView 客户端
        webView.webViewClient = WebViewClient()
        
        // 加载 URL
        webView.loadUrl(url)
        
        return webView
    }
    
    /**
     * 在 Jetpack Compose 中显示 WebView 的 Composable 函数
     * 
     * @param url 要显示的 URL
     * @param modifier 修饰符
     */
    @Composable
    fun WebviewContent(url: String, modifier: Modifier = Modifier) {
        AndroidView(
            factory = { context ->
                createWebView(context, url)
            },
            update = { webView ->
                // 如果 URL 发生变化，重新加载
                if (webView.url != url) {
                    webView.loadUrl(url)
                }
            },
            modifier = modifier.fillMaxSize()
        )
    }
}