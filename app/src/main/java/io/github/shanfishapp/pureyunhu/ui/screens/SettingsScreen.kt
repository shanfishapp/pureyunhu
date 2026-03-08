package io.github.shanfishapp.pureyunhu.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import io.github.shanfishapp.pureyunhu.data.ListSetting
import io.github.shanfishapp.pureyunhu.data.NavigateSetting
import io.github.shanfishapp.pureyunhu.data.SwitchSetting
import io.github.shanfishapp.pureyunhu.ui.components.ClickSettingItem
import io.github.shanfishapp.pureyunhu.ui.components.ListSettingItem
import io.github.shanfishapp.pureyunhu.ui.components.NavigateSettingItem
import io.github.shanfishapp.pureyunhu.ui.components.SettingItem
import io.github.shanfishapp.pureyunhu.ui.components.SwitchSettingItem
import io.github.shanfishapp.pureyunhu.ui.theme.ThemeManager
import io.github.shanfishapp.pureyunhu.ui.theme.ThemeMode

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    // 使用一个状态来跟踪是否正在导航，防止重复点击
    var isNavigating by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val themeOptions = listOf("跟随系统", "浅色模式", "深色模式")
    val languageOptions = listOf("简体中文", "English", "日本語", "한국어")
    val currentThemeIndex = when (themeManager.loadThemeMode()) {
        ThemeMode.SYSTEM -> 0
        ThemeMode.LIGHT -> 1
        ThemeMode.DARK -> 2
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // 防止重复点击
                            if (!isNavigating) {
                                isNavigating = true
                                navController.navigateUp()
                            }
                        },
                        enabled = !isNavigating // 当正在导航时禁用按钮
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // 通用设置组
            item {
                Text(
                    text = "通用",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // 版本信息
            item {
                ClickSettingItem(
                    setting = SettingItem(
                        key = "extension",
                        title = "扩展",
                        subTitle = "开启官方扩展",
                        icon = Icons.Default.Extension
                    ),
                    onClick = {
                        navController.navigate("extension")
                    }
                )
            }

            // 主题选择
            item {
                ListSettingItem(
                    setting = ListSetting(
                        key = "theme",
                        title = "主题",
                        subTitle = "选择应用主题模式",
                        items = themeOptions,
                        icon = Icons.Default.BrightnessMedium
                    ),
                    onItemSelected = { index, _ ->
                        val selectedThemeMode = when (index) {
                            0 -> ThemeMode.SYSTEM
                            1 -> ThemeMode.LIGHT
                            2 -> ThemeMode.DARK
                            else -> ThemeMode.SYSTEM
                        }
                        themeManager.saveThemeMode(selectedThemeMode)
                    }
                )
            }

            // 通知设置组
            item {
                Text(
                    text = "通知",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // 通知开关
            item {
                SwitchSettingItem(
                    setting = SwitchSetting(
                        key = "notification_enabled",
                        title = "启用通知",
                        subTitle = "接收应用推送通知",
                        icon = Icons.Default.Notifications,
                        defaultValue = true
                    )
                )
            }

            // 声音开关
            item {
                SwitchSettingItem(
                    setting = SwitchSetting(
                        key = "notification_sound",
                        title = "通知声音",
                        subTitle = "收到通知时播放声音",
                        icon = Icons.AutoMirrored.Filled.VolumeUp
                    )
                )
            }

            // 震动开关
            item {
                SwitchSettingItem(
                    setting = SwitchSetting(
                        key = "notification_vibrate",
                        title = "震动",
                        subTitle = "收到通知时震动",
                        icon = Icons.Default.Vibration
                    )
                )
            }

            // 存储设置组
            item {
                Text(
                    text = "存储",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // 自动存储开关
            item {
                SwitchSettingItem(
                    setting = SwitchSetting(
                        key = "auto_save",
                        title = "自动存储",
                        subTitle = "自动保存编辑内容",
                        icon = Icons.Default.Save,
                        defaultValue = true
                    )
                )
            }

            // 存储位置
            item {
                NavigateSettingItem(
                    setting = NavigateSetting(
                        key = "save_location",
                        title = "存储位置",
                        subTitle = "选择文件保存路径",
                        content = "内部存储",
                        icon = Icons.Default.Folder
                    ),
                    onClick = {
                        // 导航到存储位置选择界面
                        navController.navigate("save_location")
                    }
                )
            }

            // 缓存管理
            item {
                NavigateSettingItem(
                    setting = NavigateSetting(
                        key = "cache",
                        title = "缓存管理",
                        subTitle = "清理应用缓存",
                        content = "128 MB",
                        icon = Icons.Default.Storage
                    ),
                    onClick = {
                        // 导航到缓存管理界面
                        navController.navigate("cache_manager")
                    }
                )
            }

            // 关于
            item {
                Text(
                    text = "关于",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // 版本信息
            item {
                ClickSettingItem(
                    setting = SettingItem(
                        key = "version",
                        title = "关于应用",
                        subTitle = "关于 PureYunhu",
                        icon = Icons.Default.Info
                    ),
                    onClick = {
                        navController.navigate("about")
                    }
                )
            }
        }
    }
}