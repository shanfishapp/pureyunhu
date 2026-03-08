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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
    var userHomepageData by remember { mutableStateOf(UserInfoManager.getCachedUserHomepageInfo()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoggingOut by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 使用LaunchedEffect监听用户信息变化
    LaunchedEffect(Unit) {
        UserInfoManager.getUserInfo { userInfo ->
            userData = userInfo
            isLoadingProfileInfo = UserInfoManager.isLoading
            
            // 获取缓存的用户主页信息
            userHomepageData = UserInfoManager.getCachedUserHomepageInfo()
        }
    }

    // 监听UserInfoManager的状态变化
    LaunchedEffect(UserInfoManager.userInfo, UserInfoManager.isLoading, UserInfoManager.getCachedUserHomepageInfo()) {
        userData = UserInfoManager.userInfo
        isLoadingProfileInfo = UserInfoManager.isLoading
        userHomepageData = UserInfoManager.getCachedUserHomepageInfo()
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
                        .height(120.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
                            Text("加载中...")
                        }
                    }
                }
                
                // 加载状态下的统计卡片
                Card(
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("加载中...")
                    }
                }
            } else if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
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
                
                // 错误状态下的统计卡片
                Card(
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("无法加载统计数据")
                    }
                }
            } else if (userData != null) {
                UserProfileCard(
                    basicUserData = userData!!.data.user,
                    homepageUserData = userHomepageData
                )
                
                UserStatsCard(
                    homepageUserData = userHomepageData,
                    basicUserData = userData!!.data.user
                )
            }
            Card(
                modifier = Modifier.height(120.dp).fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
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
                            title = "每周抽奖",
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
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            title = "任务中心",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .height(144.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(end = 10.dp)
                ) {
                    Text("修改个人资料", fontSize = 13.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp))
                    Icon(Icons.Default.ChevronRight, "跳转", modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(end = 10.dp)
                ) {
                    Text("进入机器人控制台", fontSize = 13.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp))
                    Icon(Icons.Default.ChevronRight, "跳转", modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(end = 10.dp)
                ) {
                    Text("PureYunhu 官方用户群", fontSize = 13.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp))
                    Icon(Icons.Default.ChevronRight, "跳转", modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp))
                }
            }

            // 退出登录按钮
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(48.dp),
                onClick = {
                    showLogoutDialog = true
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

    // 退出登录确认弹窗
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },
            title = {
                Text(
                    text = "退出登录",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "确定要退出登录吗？",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        performLogoutWithApi(navController, snackbarHostState, coroutineScope) { loading ->
                            isLoggingOut = loading
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF851717)
                    )
                ) {
                    Text("确定", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                    }
                ) {
                    Text("取消")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * 执行退出登录的API调用和清理操作
 */
private fun performLogoutWithApi(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onLoadingChange: (Boolean) -> Unit
) {
    onLoadingChange(true)

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
            onLoadingChange(false)

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
            onLoadingChange(false)

            // 显示网络错误消息
            coroutineScope.launch {
                snackbarHostState.showSnackbar("网络错误: ${t.message}")
            }

            // 网络失败也清除本地数据和跳转到登录页
            performLogout(navController)
        }
    })
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
fun UserProfileCard(
    basicUserData: GetUserInfo.GetUserInfoUser,
    homepageUserData: io.github.shanfishapp.pureyunhu.models.UserHomepageInfo.UserHomepageUser?
) {
    Card(
        modifier = Modifier
            .height(120.dp)
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
                imageUrl = basicUserData.avatarUrl,
                contentDescription = "用户头像",
                modifier = Modifier
                    .size(80.dp)
            )

            // 昵称和ID信息
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 显示昵称，VIP用户为金色
                    Text(
                        text = basicUserData.nickname,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (homepageUserData?.isVip == 1) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
                    )
                    
                    // 如果是VIP用户，显示VIP图标
                    if (homepageUserData?.isVip == 1) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney, // 使用金币图标表示VIP
                            contentDescription = "VIP",
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(20.dp),
                            tint = Color(0xFFFFD700) // 金色
                        )
                    }
                }

                Text(
                    text = "ID: ${basicUserData.userId}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun UserStatsCard(homepageUserData: io.github.shanfishapp.pureyunhu.models.UserHomepageInfo.UserHomepageUser?, basicUserData: GetUserInfo.GetUserInfoUser) {
    Card(
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 在线天数
            StatItem(
                value = homepageUserData?.onLineDay.toString() ?: "0",
                label = "在线天数",
                modifier = Modifier.weight(1f)
            )
            
            // 连续在线天数
            StatItem(
                value = homepageUserData?.continuousOnLineDay.toString() ?: "0",
                label = "连续在线",
                modifier = Modifier.weight(1f)
            )

            // 金币数量
            StatItem(
                value = basicUserData.goldCoinAmount.toString(),
                label = "金币",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
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
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(0.dp)
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
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = title, fontSize = 12.sp)
        }
    }
}