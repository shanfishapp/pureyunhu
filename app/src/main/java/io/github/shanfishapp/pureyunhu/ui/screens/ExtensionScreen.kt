package io.github.shanfishapp.pureyunhu.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.MarkChatRead
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.shanfishapp.pureyunhu.data.SwitchSetting
import io.github.shanfishapp.pureyunhu.ui.components.SwitchSettingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("扩展") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigateUp()
                        }
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
            item {
                SwitchSettingItem(
                    SwitchSetting(
                        key = "mention_all",
                        title = "艾特全体成员",
                        subTitle = "开启艾特全体成员(第三方实现)",
                        defaultValue = false,
                        icon = Icons.Default.AlternateEmail
                    )
                )
            }
            item {
                SwitchSettingItem(
                    SwitchSetting(
                        key = "add_same_message",
                        title = "消息加一功能",
                        subTitle = "相同消息显示+1按钮(第三方实现)",
                        defaultValue = false,
                        icon = Icons.Default.MarkChatRead
                    )
                )
            }
        }
    }
}