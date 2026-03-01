package io.github.shanfishapp.pureyunhu.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import io.github.shanfishapp.pureyunhu.api.ApiClient
import io.github.shanfishapp.pureyunhu.models.GeneralResponse
import io.github.shanfishapp.pureyunhu.models.GetUserInfo
import io.github.shanfishapp.pureyunhu.models.LogOutRequest
import io.github.shanfishapp.pureyunhu.utils.AsyncImageRefer
import io.github.shanfishapp.pureyunhu.utils.TokenManager
import io.github.shanfishapp.pureyunhu.utils.UserInfoManager
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonScreen(navController: NavController, modifier: Modifier = Modifier) {
    var isLoadingProfileInfo by remember { mutableStateOf(UserInfoManager.isLoading) }
    var userData by remember { mutableStateOf(UserInfoManager.userInfo) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoggingOut by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 使用LaunchedEffect监听用户信息变化
    LaunchedEffect(Unit) {
        UserInfoManager.getUserInfo { userInfo ->
            userData = userInfo
            isLoadingProfileInfo = UserInfoManager.isLoading
        }
    }

    // 监听UserInfoManager的状态变化
    LaunchedEffect(UserInfoManager.userInfo, UserInfoManager.isLoading) {
        userData = UserInfoManager.userInfo
        isLoadingProfileInfo = UserInfoManager.isLoading
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("个人信息") },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate("settings")
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoadingProfileInfo) {
                Card(
                    modifier = Modifier
                        .height(160.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
                            Text("加载中...")
                        }
                    }
                }
            } else if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .height(160.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("错误: $errorMessage")
                    }
                }
            } else if (userData != null) {
                UserProfileCard(userData = userData!!.data.user)
            }
            Card(
                modifier = Modifier.height(120.dp).fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp),  // 总体宽度固定为140.dp
                shape = RoundedCornerShape(16.dp),  // 外部卡片圆角
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),  // 内部间距
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 功能卡片行
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 每周抽奖 - 无背景色卡片
                        TransparentFunctionCard(
                            icon = Icons.Default.CardGiftcard,
                            title = "每周抽奖",  // 手动换行，适应小宽度
                            modifier = Modifier.weight(1f)
                        )

                        // 邀请好友 - 无背景色卡片
                        TransparentFunctionCard(
                            icon = Icons.Default.PersonAdd,
                            title = "邀请好友",
                            modifier = Modifier.weight(1f)
                        )

                        // 金币有礼 - 无背景色卡片
                        TransparentFunctionCard(
                            icon = Icons.Default.AttachMoney,
                            title = "金币有礼",
                            modifier = Modifier.weight(1f)
                        )

                        // 任务中心 - 无背景色卡片
                        TransparentFunctionCard(
                            icon = Icons.Default.Assignment,
                            title = "任务中心",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            // 退出登录按钮
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(48.dp),
                onClick = {
                    isLoggingOut = true

                    // 获取token
                    val token = TokenManager.get() ?: ""

                    // 调用退出登录接口
                    ApiClient.apiService.logOut(
                        logOutRequest = LogOutRequest("pureyunhu"),
                        token = token
                    ).enqueue(object : Callback<GeneralResponse> {
                        override fun onResponse(
                            call: Call<GeneralResponse>,
                            response: Response<GeneralResponse>
                        ) {
                            isLoggingOut = false

                            if (response.isSuccessful) {
                                // 显示成功消息
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("退出登录成功")
                                }
                            } else {
                                // 显示失败消息
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("退出登录失败: ${response.code()}")
                                }
                            }

                            // 无论成功失败，都需要清除本地数据和跳转到登录页
                            performLogout(navController)
                        }

                        override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                            isLoggingOut = false

                            // 显示网络错误消息
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("网络错误: ${t.message}")
                            }

                            // 网络失败也清除本地数据和跳转到登录页
                            performLogout(navController)
                        }
                    })
                },
                enabled = !isLoggingOut,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF851717),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                if (isLoggingOut) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "退出登录",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("退出登录")
                }
            }
        }
    }
}

/**
 * 执行退出登录的清理操作
 */
private fun performLogout(navController: NavController) {
    // 清除用户信息
    UserInfoManager.reset()

    // 清除token
    TokenManager.clear()

    // 跳转到登录页
    navController.navigate("login") {
        // 清除返回栈，防止用户按返回键回到个人信息页
        popUpTo("person") { inclusive = true }
        // 启动新的任务栈
        launchSingleTop = true
    }
}

@Composable
fun UserProfileCard(userData: GetUserInfo.GetUserInfoUser) {
    Card(
        modifier = Modifier
            .height(160.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            AsyncImageRefer(
                imageUrl = userData.avatarUrl,
                contentDescription = "用户头像",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
            )

            // 昵称和ID信息
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = userData.nickname,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "ID: ${userData.userId}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// 透明背景的卡片组件
@Composable
fun TransparentFunctionCard(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(80.dp),  // 固定高度
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent  // 设置背景色为透明
        ),
        elevation = CardDefaults.cardElevation(0.dp)  // 去除阴影
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),  // 小尺寸图标
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = title, fontSize=12.sp)
        }
    }
}