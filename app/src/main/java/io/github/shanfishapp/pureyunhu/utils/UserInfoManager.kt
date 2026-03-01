package io.github.shanfishapp.pureyunhu.utils

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import io.github.shanfishapp.pureyunhu.api.ApiClient
import io.github.shanfishapp.pureyunhu.models.GetUserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 用户信息管理器 - 确保应用启动时只拉取一次用户信息
 */
object UserInfoManager {
    private var _userInfo = mutableStateOf<GetUserInfo?>(null)
    val userInfo get() = _userInfo.value

    private var _isLoading = mutableStateOf(false)
    val isLoading get() = _isLoading.value

    private var _hasFetched = false // 标记是否已经拉取过用户信息

    /**
     * 获取用户信息 - 只在尚未获取时发起请求
     */
    fun getUserInfo(onResult: (GetUserInfo?) -> Unit) {
        // 如果已经拉取过用户信息，直接返回缓存的值
        if (_hasFetched) {
            onResult(_userInfo.value)
            return
        }

        // 如果正在加载，也直接返回当前值
        if (_isLoading.value) {
            onResult(_userInfo.value)
            return
        }

        // 检查是否有token
        val token = TokenManager.get()
        if (token.isBlank()) {
            Log.e("UserInfoManager", "No token found, cannot fetch user info")
            onResult(null)
            return
        }

        _isLoading.value = true

        // 使用Retrofit发起异步请求
        ApiClient.apiService.getUserInfo(token).enqueue(object : Callback<GetUserInfo> {
            override fun onResponse(call: Call<GetUserInfo>, response: Response<GetUserInfo>) {
                if (response.isSuccessful && response.body() != null) {
                    _userInfo.value = response.body()
                    _hasFetched = true // 标记为已拉取
                    Log.d("UserInfoManager", "User info fetched successfully")
                } else {
                    Log.e("UserInfoManager", "Failed to fetch user info: ${response.code()}")
                }
                _isLoading.value = false
                onResult(_userInfo.value)
            }

            override fun onFailure(call: Call<GetUserInfo>, t: Throwable) {
                Log.e("UserInfoManager", "Network request failed: ${t.message}")
                _isLoading.value = false
                onResult(null)
            }
        })
    }

    /**
     * 重置用户信息 - 当用户登出时调用
     */
    fun reset() {
        _userInfo.value = null
        _hasFetched = false
    }

    /**
     * 强制刷新用户信息
     */
    fun refreshUserInfo(onResult: (GetUserInfo?) -> Unit) {
        _hasFetched = false
        getUserInfo(onResult)
    }
}