package io.github.shanfishapp.pureyunhu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(navController: NavController) {
    val tabList = listOf("好友", "群聊", "机器人")
    val pagerState = rememberPagerState(pageCount = { tabList.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "通讯录",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 快捷操作卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column {
                    ContactScreenItem(
                        icon = Icons.Default.PersonAddAlt1,
                        text = "好友申请",
                        count = 99,
                        showDivider = true,
                        onClick = { /* 导航到好友申请页面 */ }
                    )
                    ContactScreenItem(
                        icon = Icons.Default.ContactMail,
                        text = "分享我的名片",
                        count = 0,
                        showDivider = false,
                        onClick = { /* 分享名片功能 */ }
                    )
                }
            }

            // 标签栏
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabList.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                title,
                                fontWeight = if (pagerState.currentPage == index)
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // 内容区域
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> FriendListContent()
                    1 -> GroupListContent()
                    2 -> RobotListContent()
                }
            }
        }
    }
}

@Composable
fun ContactScreenItem(
    icon: ImageVector,
    text: String,
    count: Int,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { onClick() }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左侧图标和文字
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = text,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 右侧徽章和箭头
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (count > 0) {
                    Badge(
                        containerColor = Color.Red,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Red)
                    ) {
                        Text(
                            text = if (count > 99) "99+" else count.toString(),
                            color = Color.White,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "进入",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

// 模拟数据类
data class Contact(
    val id: String,
    val name: String,
    val avatar: ImageVector,
    val status: String? = null,
    val unreadCount: Int = 0
)

@Composable
fun FriendListContent() {
    val friends = listOf(
        Contact("1", "张三", Icons.Default.Person, "在线"),
        Contact("2", "李四", Icons.Default.Person, "离线"),
        Contact("3", "王五", Icons.Default.Person, "忙碌", 3),
        // 更多好友...
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(friends) { friend ->
            ContactListItem(
                contact = friend,
                onClick = { /* 跳转到聊天 */ }
            )
        }
    }
}

@Composable
fun GroupListContent() {
    val groups = listOf(
        Contact("1", "家庭群", Icons.Default.Group, "5条新消息", 5),
        Contact("2", "同事群", Icons.Default.Group, "3条新消息", 3),
        Contact("3", "兴趣小组", Icons.Default.Group, "2条新消息"),
        // 更多群聊...
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(groups) { group ->
            ContactListItem(
                contact = group,
                onClick = { /* 跳转到群聊 */ }
            )
        }
    }
}

@Composable
fun RobotListContent() {
    val robots = listOf(
        Contact("1", "小助手", Icons.Default.SmartToy, "在线"),
        Contact("2", "天气机器人", Icons.Default.SmartToy, "离线"),
        Contact("3", "新闻推送", Icons.Default.SmartToy, "在线", 1),
        // 更多机器人...
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(robots) { robot ->
            ContactListItem(
                contact = robot,
                onClick = { /* 与机器人交互 */ }
            )
        }
    }
}

@Composable
fun ContactListItem(
    contact: Contact,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = contact.avatar,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 联系人信息
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = contact.name,
                fontWeight = FontWeight.Medium,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                color = MaterialTheme.colorScheme.onSurface
            )

            contact.status?.let {
                Text(
                    text = it,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 未读消息数
        if (contact.unreadCount > 0) {
            Badge(
                containerColor = Color.Red,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Red)
            ) {
                Text(
                    text = if (contact.unreadCount > 99) "99+"
                    else contact.unreadCount.toString(),
                    color = Color.White,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}