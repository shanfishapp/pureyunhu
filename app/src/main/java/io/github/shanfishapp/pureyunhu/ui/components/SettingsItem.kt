package io.github.shanfishapp.pureyunhu.ui.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.github.shanfishapp.pureyunhu.data.*

// 1. Switch 设置项
@Composable
fun SwitchSettingItem(
    setting: SwitchSetting,
    modifier: Modifier = Modifier,
    onCheckedChange: ((Boolean) -> Unit)? = null
) {
    val prefs = rememberSettingsPreferences()
    var checked by remember(setting.key) {
        mutableStateOf(prefs.getBoolean(setting.key, setting.defaultValue))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧图标
            setting.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            // 标题和副标题
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = setting.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                setting.subTitle?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 开关
            Switch(
                checked = checked,
                onCheckedChange = { newValue ->
                    checked = newValue
                    prefs.putBoolean(setting.key, newValue)
                    onCheckedChange?.invoke(newValue)
                }
            )
        }
    }
}

// 2. 导航设置项
@Composable
fun NavigateSettingItem(
    setting: NavigateSetting,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧图标
            setting.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            // 标题和副标题
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = setting.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                setting.subTitle?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 右侧内容
            setting.content?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            // 右箭头
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// 3. 列表选择设置项
@Composable
fun ListSettingItem(
    setting: ListSetting,
    modifier: Modifier = Modifier,
    onItemSelected: ((Int, String) -> Unit)? = null
) {
    val prefs = rememberSettingsPreferences()
    var showDialog by remember { mutableStateOf(false) }
    var selectedIndex by remember(setting.key) {
        mutableStateOf(prefs.getInt(setting.key, 0))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { showDialog = true },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧图标
            setting.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            // 标题和副标题
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = setting.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                setting.subTitle?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 当前选中的值
            Text(
                text = setting.items[selectedIndex],
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )

            // 右箭头
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    // 选择对话框
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = setting.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    setting.items.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedIndex = index
                                    prefs.putInt(setting.key, index)
                                    onItemSelected?.invoke(index, item)
                                    showDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f),
                                color = if (index == selectedIndex)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )

                            if (index == selectedIndex) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = { showDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("取消")
                    }
                }
            }
        }
    }
}

@Composable
fun ClickSettingItem(
    setting: SettingItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,  // 点击回调
    showArrow: Boolean = true,  // 是否显示右箭头
    trailingContent: @Composable (() -> Unit)? = null  // 自定义右侧内容
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },  // 使用传入的 onClick
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧图标
            setting.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            // 标题和副标题
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = setting.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                setting.subTitle?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 右侧内容（优先使用自定义内容）
            if (trailingContent != null) {
                trailingContent()
            } else {
                // 显示内容文本
                setting.content?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                // 显示右箭头
                if (showArrow) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
// 更新 SettingItem 数据类
data class SettingItem(
    val key: String,
    val title: String,
    val subTitle: String? = null,
    val content: String? = null,  // 右侧显示的内容
    val icon: ImageVector? = null
)

// 外部调用方法
object SettingsManager {
    // 获取开关状态
    fun getSwitchState(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        return SettingsPreferences(context).getBoolean(key, defaultValue)
    }

    // 设置开关状态
    fun setSwitchState(context: Context, key: String, value: Boolean) {
        SettingsPreferences(context).putBoolean(key, value)
    }

    // 获取列表选中的索引
    fun getListSelectedIndex(context: Context, key: String, defaultValue: Int = 0): Int {
        return SettingsPreferences(context).getInt(key, defaultValue)
    }

    // 设置列表选中的索引
    fun setListSelectedIndex(context: Context, key: String, index: Int) {
        SettingsPreferences(context).putInt(key, index)
    }

    // 获取列表选中的值
    fun getListSelectedValue(context: Context, key: String, items: List<String>): String {
        val index = getListSelectedIndex(context, key)
        return if (index in items.indices) items[index] else items.firstOrNull() ?: ""
    }

    // 获取字符串设置
    fun getString(context: Context, key: String, defaultValue: String = ""): String {
        return SettingsPreferences(context).getString(key, defaultValue)
    }

    // 设置字符串
    fun setString(context: Context, key: String, value: String) {
        SettingsPreferences(context).putString(key, value)
    }

    // 清除所有设置
    fun clearAllSettings(context: Context) {
        SettingsPreferences(context).clear()
    }
}