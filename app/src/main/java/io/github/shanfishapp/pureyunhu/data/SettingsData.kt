package io.github.shanfishapp.pureyunhu.data

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

// 设置项数据类
data class SettingItem(
    val key: String,
    val title: String,
    val subTitle: String? = null,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null
)

data class SwitchSetting(
    val key: String,
    val title: String,
    val subTitle: String? = null,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val defaultValue: Boolean = false
)

data class NavigateSetting(
    val key: String,
    val title: String,
    val subTitle: String? = null,
    val content: String? = null,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null
)

data class ListSetting(
    val key: String,
    val title: String,
    val subTitle: String? = null,
    val items: List<String>,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null
)

// SharedPreferences 管理类
class SettingsPreferences(private val context: Context) {
    companion object {
        private const val PREFS_NAME = "settings_prefs"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // 存储布尔值
    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    // 获取布尔值
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    // 存储字符串
    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    // 获取字符串
    fun getString(key: String, defaultValue: String = ""): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    // 存储整型
    fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    // 获取整型
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return prefs.getInt(key, defaultValue)
    }

    // 清除所有设置
    fun clear() {
        prefs.edit().clear().apply()
    }
}

// 可组合的 SettingsPreferences 提供者
@Composable
fun rememberSettingsPreferences(): SettingsPreferences {
    val context = LocalContext.current
    return remember { SettingsPreferences(context) }
}