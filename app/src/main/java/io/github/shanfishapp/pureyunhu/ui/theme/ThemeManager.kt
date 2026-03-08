// ui/theme/ThemeManager.kt
package io.github.shanfishapp.pureyunhu.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

@Stable
class ThemeState(
    initialMode: ThemeMode = ThemeMode.SYSTEM,
    private val onModeChange: (ThemeMode) -> Unit = {}
) {
    private var _mode by mutableStateOf(initialMode)

    val mode: ThemeMode
        get() = _mode

    fun setMode(newMode: ThemeMode) {
        _mode = newMode
        onModeChange(newMode)
    }

    fun isDarkTheme(systemIsDark: Boolean): Boolean {
        return when (mode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> systemIsDark
        }
    }
}

@Composable
fun rememberThemeState(
    initialMode: ThemeMode = ThemeMode.SYSTEM,
    onModeChange: (ThemeMode) -> Unit = {}
): ThemeState {
    return rememberSaveable(
        saver = Saver(
            save = { it.mode.name },
            restore = { ThemeState(ThemeMode.valueOf(it), onModeChange) }
        )
    ) {
        ThemeState(initialMode, onModeChange)
    }
}

// 用于保存和读取主题模式的工具类
class ThemeManager(private val context: Context) {
    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveThemeMode(themeMode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, themeMode.name).apply()
    }

    fun loadThemeMode(): ThemeMode {
        val modeName = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(modeName)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM // 如果存储的值无效，返回默认值
        }
    }
}