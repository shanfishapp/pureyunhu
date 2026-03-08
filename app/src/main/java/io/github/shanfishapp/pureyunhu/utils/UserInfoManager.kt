package io.github.shanfishapp.pureyunhu.utils

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import io.github.shanfishapp.pureyunhu.api.ApiClient
import io.github.shanfishapp.pureyunhu.models.GetUserInfo
import io.github.shanfishapp.pureyunhu.models.UserHomepageInfo
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

    private var _userHomepageInfo = mutableStateOf<UserHomepageInfo.UserHomepageUser?>(null)
    val userHomepageInfo get() = _userHomepageInfo.value

    private var _isLoading = mutableStateOf(false)
    val isLoading get() = _isLoading.value

    private var _hasFetched = false // 标记是否已经拉取过用户信息
    private var _hasFetchedHomepage = false // 标记是否已经拉取过用户主页信息

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
                    
                    // 在获取到用户信息后，获取用户主页信息
                    response.body()?.data?.user?.userId?.let { userId ->
                        getUserHomepageInfo(userId) { homepageInfo ->
                            Log.d("UserInfoManager", "User homepage info fetched successfully")
                        }
                    }
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
     * 获取用户主页信息 - 用于缓存自己的用户主页信息
     */
    fun getUserHomepageInfo(userId: String, onResult: (UserHomepageInfo.UserHomepageUser?) -> Unit) {
        // 如果已经拉取过用户主页信息，直接返回缓存的值
        if (_hasFetchedHomepage && userId == _userHomepageInfo.value?.userId) {
            onResult(_userHomepageInfo.value)
            return
        }

        // 检查是否有token
        val token = TokenManager.get()
        if (token.isBlank()) {
            Log.e("UserInfoManager", "No token found, cannot fetch user homepage info")
            onResult(null)
            return
        }

        // 使用Retrofit发起异步请求
        ApiClient.apiService.getUserHomepageInfo(token, userId).enqueue(object : Callback<UserHomepageInfo> {
            override fun onResponse(call: Call<UserHomepageInfo>, response: Response<UserHomepageInfo>) {
                if (response.isSuccessful && response.body() != null && response.body()?.code == 1) {
                    // 检查用户是否存在（根据API文档，不存在的用户可能返回特殊数据）
                    val homepageUser = response.body()?.data?.user
                    if (homepageUser != null) {
                        // 缓存自己的主页信息（当userId与当前登录用户ID匹配时）
                        if (userId == _userInfo.value?.data?.user?.userId) {
                            _userHomepageInfo.value = homepageUser
                            _hasFetchedHomepage = true
                        }
                        Log.d("UserInfoManager", "User homepage info fetched successfully for user: $userId")
                    } else {
                        Log.e("UserInfoManager", "User does not exist or invalid data")
                    }
                    onResult(homepageUser)
                } else {
                    Log.e("UserInfoManager", "Failed to fetch user homepage info: ${response.code()}")
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<UserHomepageInfo>, t: Throwable) {
                Log.e("UserInfoManager", "Network request failed for user homepage: ${t.message}")
                onResult(null)
            }
        })
    }

    /**
     * 获取缓存的用户主页信息
     */
    fun getCachedUserHomepageInfo(): UserHomepageInfo.UserHomepageUser? {
        return _userHomepageInfo.value
    }

    /**
     * 重置用户信息 - 当用户登出时调用
     */
    fun reset() {
        _userInfo.value = null
        _userHomepageInfo.value = null
        _hasFetched = false
        _hasFetchedHomepage = false
    }

    /**
     * 强制刷新用户信息
     */
    fun refreshUserInfo(onResult: (GetUserInfo?) -> Unit) {
        _hasFetched = false
        _hasFetchedHomepage = false
        getUserInfo(onResult)
    }
}