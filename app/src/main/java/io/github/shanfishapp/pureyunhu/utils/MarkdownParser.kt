package io.github.shanfishapp.pureyunhu.utils

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/**
 * 简单的Markdown解析器
 */
object MarkdownParser {

    @Composable
    fun parseMarkdown(
        markdown: String,
        modifier: Modifier = Modifier,
        navController: NavController? = null
    ) {
        val annotatedString = buildAnnotatedString {
            var i = 0
            while (i < markdown.length) {
                // 检查粗体 **text**
                if (i + 3 < markdown.length && markdown.substring(i, i + 2) == "**" &&
                    markdown.substring(i + 2, i + 4) != "**") {
                    val end = markdown.indexOf("**", i + 2)
                    if (end != -1) {
                        val boldText = markdown.substring(i + 2, end)
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        append(boldText)
                        pop()
                        i = end + 2
                        continue
                    }
                }

                // 检查斜体 *text* 或 _text_
                if (i + 1 < markdown.length) {
                    if ((markdown[i] == '*' || markdown[i] == '_') &&
                        i + 2 < markdown.length && markdown[i + 1] != markdown[i]) {
                        val char = markdown[i]
                        val end = markdown.indexOf(char, i + 1)
                        if (end != -1) {
                            val italicText = markdown.substring(i + 1, end)
                            pushStyle(SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                            append(italicText)
                            pop()
                            i = end + 1
                            continue
                        }
                    }
                }

                // 检查标题 #, ##, ### - 修改字体大小逻辑
                if (i == 0 || markdown[i - 1] == '\n') {
                    var headerLevel = 0
                    var j = i
                    while (j < markdown.length && markdown[j] == '#') {
                        headerLevel++
                        j++
                    }
                    if (headerLevel in 1..3 && j < markdown.length && markdown[j] == ' ') {
                        val end = markdown.indexOf('\n', j)
                        val headerText = if (end != -1) markdown.substring(j + 1, end) else markdown.substring(j + 1)

                        // 根据标题级别设置合适的字体大小
                        val fontSize = when (headerLevel) {
                            1 -> 24.sp   // 一级标题
                            2 -> 20.sp   // 二级标题
                            3 -> 18.sp   // 三级标题
                            else -> 16.sp
                        }

                        pushStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = fontSize
                            )
                        )
                        append(headerText)
                        pop()

                        // 添加换行
                        append("\n")

                        i = if (end != -1) end + 1 else markdown.length
                        continue
                    }
                }

                // 检查链接 [text](url)
                if (i + 1 < markdown.length && markdown[i] == '[') {
                    val linkTextEnd = markdown.indexOf(']', i)
                    if (linkTextEnd != -1 && linkTextEnd + 1 < markdown.length && markdown[linkTextEnd + 1] == '(') {
                        val urlEnd = markdown.indexOf(')', linkTextEnd + 2)
                        if (urlEnd != -1) {
                            val linkText = markdown.substring(i + 1, linkTextEnd)
                            val url = markdown.substring(linkTextEnd + 2, urlEnd)

                            // 添加URL注解，以便点击时可以跳转
                            pushStringAnnotation("URL", url)
                            pushStyle(SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline))
                            append(linkText)
                            pop()

                            i = urlEnd + 1
                            continue
                        }
                    }
                }

                // 检查行内代码 `code`
                if (i < markdown.length && markdown[i] == '`') {
                    val end = markdown.indexOf('`', i + 1)
                    if (end != -1) {
                        val codeText = markdown.substring(i + 1, end)
                        pushStyle(SpanStyle(background = Color.LightGray, fontFamily = FontFamily.Monospace))
                        append(codeText)
                        pop()
                        i = end + 1
                        continue
                    }
                }

                // 添加普通字符
                append(markdown[i])
                i++
            }
        }

        ClickableText(
            text = annotatedString,
            modifier = modifier,
            onClick = { offset ->
                annotatedString.getStringAnnotations("URL", offset, offset).firstOrNull()?.let { annotation ->
                    // 如果提供了NavController，可以在这里处理链接点击
                    navController?.let {
                        // 这里可以添加导航逻辑，例如：
                        // it.navigate(annotation.item)
                    }
                }
            }
        )
    }
}