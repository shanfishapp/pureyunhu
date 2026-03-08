package io.github.shanfishapp.pureyunhu

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Contacts
import kotlinx.serialization.json.Json
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.shanfishapp.pureyunhu.models.*
import io.github.shanfishapp.pureyunhu.ui.screens.AboutScreen
import io.github.shanfishapp.pureyunhu.ui.screens.ChatScreen
import io.github.shanfishapp.pureyunhu.ui.screens.ChatViewModel
import io.github.shanfishapp.pureyunhu.ui.screens.CommunityScreen
import io.github.shanfishapp.pureyunhu.ui.screens.ContactScreen
import io.github.shanfishapp.pureyunhu.ui.screens.ExtensionScreen
import io.github.shanfishapp.pureyunhu.ui.screens.LoginScreen
import io.github.shanfishapp.pureyunhu.ui.screens.MessageScreen
import io.github.shanfishapp.pureyunhu.ui.screens.PersonScreen
import io.github.shanfishapp.pureyunhu.ui.screens.RecommendScreen
import io.github.shanfishapp.pureyunhu.ui.screens.SettingsScreen
import io.github.shanfishapp.pureyunhu.ui.theme.PureYunhuTheme
import io.github.shanfishapp.pureyunhu.ui.theme.ThemeManager
import io.github.shanfishapp.pureyunhu.ui.theme.ThemeMode
import io.github.shanfishapp.pureyunhu.ui.theme.rememberThemeState
import io.github.shanfishapp.pureyunhu.utils.TokenManager
import io.github.shanfishapp.pureyunhu.utils.UserInfoManager
import io.github.shanfishapp.pureyunhu.websocket.WebSocketClient
import io.github.shanfishapp.pureyunhu.websocket.WebSocketReceiver
import okhttp3.Response
import okio.ByteString
import java.util.UUID

class MainActivity : ComponentActivity() {
    private val viewModel: ChatViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenManager.init(this)
        enableEdgeToEdge()
        setContent {
            val themeManager = remember { ThemeManager(this) }
            val currentThemeMode = remember { mutableStateOf(themeManager.loadThemeMode()) }
            val themeState = rememberThemeState(
                initialMode = currentThemeMode.value,
                onModeChange = { mode ->
                    themeManager.saveThemeMode(mode)
                    currentThemeMode.value = mode
                }
            )
            
            PureYunhuTheme(
                darkTheme = themeState.isDarkTheme(isSystemInDarkTheme())
            ) {
                AppNavigation(chatViewModel = viewModel)
            }
        }
        WebSocketManager.initialize(viewModel)
        
        // 如果有token，应用启动时获取一次用户信息
        if (TokenManager.hasToken()) {
            UserInfoManager.getUserInfo { userInfo ->
                // 应用启动时获取用户信息，后续不再重复获取
            }
        }
    }
}

// 导航项数据类
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null
) {
    object Chat : BottomNavItem(
        route = "chat",
        title = "聊天",
        icon = Icons.AutoMirrored.Filled.Message
    )

    object Contact : BottomNavItem(
        route = "contact",
        title = "朋友",
        icon = Icons.Default.Group
    )

    object Community : BottomNavItem(
        route = "community",
        title = "社区",
        icon = Icons.Default.Contacts
    )

    object Recommend : BottomNavItem(
        route = "recommend",
        title = "推荐",
        icon = Icons.Default.Stars
    )

    object Profile : BottomNavItem(
        route = "person",
        title = "我的",
        icon = Icons.Default.Person
    )
}

// 导航项列表
val bottomNavItems = listOf(
    BottomNavItem.Chat,
    BottomNavItem.Contact,
    BottomNavItem.Community,
    BottomNavItem.Recommend,
    BottomNavItem.Profile
)

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel? = null
) {
    val navController = rememberNavController()

    // 检查是否有token，决定启动页面
    val startDestination = if (TokenManager.hasToken()) "chat" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        // 登录页面
        composable("login") {
            LoginScreen(navController) {
                // 登录成功后导航到聊天页面
                navController.navigate("chat") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }

        // 聊天页面
        composable("chat") {
            MainScaffold(
                navController = navController,
                showBottomBar = true,
                content = {
                    if (chatViewModel != null) {
                        ChatScreen(navController, chatViewModel)
                    } else {
                        ChatScreen(navController)
                    }
                }
            )
        }

        // 朋友页面
        composable("contact") {
            MainScaffold(
                navController = navController,
                showBottomBar = true,
                content = { ContactScreen(navController) }
            )
        }

        // 社区页面
        composable("community") {
            MainScaffold(
                navController = navController,
                showBottomBar = true,
                content = { CommunityScreen(navController) }
            )
        }

        // 推荐页面
        composable("recommend") {
            MainScaffold(
                navController = navController,
                showBottomBar = true,
                content = { RecommendScreen(navController) }
            )
        }

        // 个人页面
        composable("person") {
            MainScaffold(
                navController = navController,
                showBottomBar = true,
                content = { PersonScreen(navController) }
            )
        }

        // 设置页面
        composable("settings") {
            MainScaffold(
                navController = navController,
                showBottomBar = false, // settings 页面不显示底部导航
                content = { SettingsScreen(navController) }
            )
        }
        composable("about") {
            MainScaffold(
                navController = navController,
                showBottomBar = false,
                content = { AboutScreen(navController) }
            )
        }
        composable("extension") {
            MainScaffold(
                navController = navController,
                showBottomBar = false,
                content = { ExtensionScreen(navController) }
            )
        }
        
        // 消息页面，支持传递chat-id和chat-type参数
        composable(
            "message?chat-id={chatId}&chat-type={chatType}&chat-name={chatName}",
            arguments = listOf(
                androidx.navigation.navArgument("chatId") { defaultValue = "" },
                androidx.navigation.navArgument("chatType") { defaultValue = "" } ,
                androidx.navigation.navArgument("chatName") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val chatType = backStackEntry.arguments?.getString("chatType") ?: ""
            val chatName = backStackEntry.arguments?.getString("chatName") ?: ""
            MainScaffold(
                navController = navController,
                showBottomBar = false, // 消息页面不显示底部导航
                content = { MessageScreen(navController, chatId, chatType, chatName) }
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScaffold(
    navController: NavHostController,
    showBottomBar: Boolean,
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        }
    ) { Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            content()
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    // 获取当前导航的路由，用于高亮选中的项
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    // 导航到选中的页面
                    navController.navigate(item.route) {
                        // 弹出到导航图的起始目的地，避免重复创建
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // 避免多次点击创建多个实例
                        launchSingleTop = true
                        // 恢复之前保存的状态
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (currentRoute == item.route) {
                            // 如果选中，优先使用选中图标，否则使用普通图标
                            item.selectedIcon ?: item.icon
                        } else {
                            item.icon
                        },
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}