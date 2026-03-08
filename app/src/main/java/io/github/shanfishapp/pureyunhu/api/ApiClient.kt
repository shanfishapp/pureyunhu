package io.github.shanfishapp.pureyunhu.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://chat-go.jwzhd.com/" // 保留基础URL，但API端点使用完整URL

    // 1. 配置 OkHttpClient
    val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            // 设置日志级别为 BODY，可以打印出完整的请求和响应数据
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(logging) // 添加日志拦截器
            .connectTimeout(30, TimeUnit.SECONDS) // 连接超时
            .readTimeout(30, TimeUnit.SECONDS)    // 读取超时
            .writeTimeout(30, TimeUnit.SECONDS)   // 写入超时
            .build()
    }

    // 2. 配置 Retrofit 实例
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // 关联 OkHttpClient
            .addConverterFactory(GsonConverterFactory.create()) // 使用Gson转换器
            .build()
    }

    // 3. 提供一个通用方法来创建API服务
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
    val apiService: ApiServices = retrofit.create(ApiServices::class.java)
}