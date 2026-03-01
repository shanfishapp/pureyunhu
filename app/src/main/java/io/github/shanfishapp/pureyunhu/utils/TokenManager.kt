package io.github.shanfishapp.pureyunhu.utils

import android.content.Context
import java.io.File
import java.io.IOException

/**
 * 简单的Token管理器
 * 自动在应用内部目录创建和管理token文件
 */
object TokenManager {

    // 内部token文件名
    private const val TOKEN_FILE_NAME = "auth_token.dat"

    // token存储目录路径
    private lateinit var tokenDirPath: String

    // token文件对象
    private lateinit var tokenFile: File

    /**
     * 初始化TokenManager
     * 需要在Application启动时调用
     */
    fun init(context: Context) {
        tokenDirPath = context.filesDir.absolutePath
        tokenFile = File(tokenDirPath, TOKEN_FILE_NAME)
        ensureTokenFile()
    }

    /**
     * 确保token文件存在
     */
    private fun ensureTokenFile() {
        try {
            if (!tokenFile.exists()) {
                val parentDir = tokenFile.parentFile
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs()
                }
                tokenFile.createNewFile()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 获取token
     * 如果没有token，返回空字符串
     */
    fun get(): String {
        return try {
            if (::tokenFile.isInitialized && tokenFile.exists()) {
                tokenFile.readText()
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 保存token
     */
    fun save(token: String) {
        try {
            ensureTokenFile()
            tokenFile.writeText(token)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 清除token
     */
    fun clear() {
        try {
            if (::tokenFile.isInitialized && tokenFile.exists()) {
                tokenFile.writeText("")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 检查是否有token
     */
    fun hasToken(): Boolean {
        return get().isNotBlank()
    }
}