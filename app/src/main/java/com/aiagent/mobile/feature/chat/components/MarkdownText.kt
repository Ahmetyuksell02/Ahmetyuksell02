package com.aiagent.mobile.feature.chat.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class MdBlock {
    data class Paragraph(val raw: String) : MdBlock()
    data class Heading(val raw: String, val level: Int) : MdBlock()
    data class CodeFence(val code: String, val language: String) : MdBlock()
    data class BulletItem(val raw: String, val depth: Int) : MdBlock()
    data class NumberItem(val raw: String, val num: Int) : MdBlock()
    data class Blockquote(val raw: String) : MdBlock()
    data object Hr : MdBlock()
}

fun parseMarkdownBlocks(text: String): List<MdBlock> {
    val blocks = mutableListOf<MdBlock>()
    val lines = text.lines()
    var i = 0
    while (i < lines.size) {
        val line = lines[i]
        when {
            line.startsWith("```") -> {
                val lang = line.removePrefix("```").trim()
                val codeSb = StringBuilder()
                i++
                while (i < lines.size && !lines[i].startsWith("```")) {
                    if (codeSb.isNotEmpty()) codeSb.append('\n')
                    codeSb.append(lines[i])
                    i++
                }
                blocks.add(MdBlock.CodeFence(codeSb.toString(), lang))
            }
            line.matches(Regex("^#{1,6} .*")) -> {
                val level = line.indexOfFirst { it != '#' }
                blocks.add(MdBlock.Heading(line.drop(level + 1), level))
            }
            line.matches(Regex("^(\\*\\*\\*|---|___)\\s*$")) -> {
                blocks.add(MdBlock.Hr)
            }
            line.matches(Regex("^([-*+]) .*")) -> {
                val depth = 0
                blocks.add(MdBlock.BulletItem(line.drop(2), depth))
            }
            line.matches(Regex("^  ([-*+]) .*")) -> {
                blocks.add(MdBlock.BulletItem(line.drop(4), 1))
            }
            line.matches(Regex("^\\d+\\. .*")) -> {
                val dotIdx = line.indexOf('.')
                val num = line.substring(0, dotIdx).toIntOrNull() ?: 1
                blocks.add(MdBlock.NumberItem(line.drop(dotIdx + 2), num))
            }
            line.startsWith("> ") -> {
                blocks.add(MdBlock.Blockquote(line.drop(2)))
            }
            line.isBlank() -> { /* skip blank lines */ }
            else -> {
                blocks.add(MdBlock.Paragraph(line))
            }
        }
        i++
    }
    return blocks
}

fun applyInlineMarkdown(
    raw: String,
    codeBackground: Color,
    codeContent: Color
): AnnotatedString = buildAnnotatedString {
    var cursor = 0
    val text = raw
    while (cursor < text.length) {
        when {
            text.startsWith("***", cursor) -> {
                val end = text.indexOf("***", cursor + 3)
                if (end != -1) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic))
                    append(text.substring(cursor + 3, end))
                    pop()
                    cursor = end + 3
                } else { append(text[cursor]); cursor++ }
            }
            text.startsWith("**", cursor) -> {
                val end = text.indexOf("**", cursor + 2)
                if (end != -1) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(text.substring(cursor + 2, end))
                    pop()
                    cursor = end + 2
                } else { append(text[cursor]); cursor++ }
            }
            text.startsWith("~~", cursor) -> {
                val end = text.indexOf("~~", cursor + 2)
                if (end != -1) {
                    pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                    append(text.substring(cursor + 2, end))
                    pop()
                    cursor = end + 2
                } else { append(text[cursor]); cursor++ }
            }
            (text[cursor] == '*' || text[cursor] == '_') && cursor + 1 < text.length -> {
                val marker = text[cursor]
                val end = text.indexOf(marker, cursor + 1)
                if (end != -1) {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(text.substring(cursor + 1, end))
                    pop()
                    cursor = end + 1
                } else { append(text[cursor]); cursor++ }
            }
            text[cursor] == '`' -> {
                val end = text.indexOf('`', cursor + 1)
                if (end != -1) {
                    pushStyle(SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = codeBackground,
                        color = codeContent,
                        fontSize = 13.sp
                    ))
                    append(text.substring(cursor + 1, end))
                    pop()
                    cursor = end + 1
                } else { append(text[cursor]); cursor++ }
            }
            else -> { append(text[cursor]); cursor++ }
        }
    }
}

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified
) {
    val blocks = remember(text) { parseMarkdownBlocks(text) }
    val codeBackground = MaterialTheme.colorScheme.surfaceVariant
    val codeContent = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier) {
        blocks.forEachIndexed { index, block ->
            if (index > 0) Spacer(modifier = Modifier.height(4.dp))
            when (block) {
                is MdBlock.Heading -> {
                    val headingStyle = when (block.level) {
                        1 -> MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        2 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        3 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        else -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = applyInlineMarkdown(block.raw, codeBackground, codeContent),
                        style = headingStyle,
                        color = if (color != Color.Unspecified) color else headingStyle.color
                    )
                    if (block.level <= 2) {
                        Spacer(modifier = Modifier.height(2.dp))
                        HorizontalDivider(color = onSurfaceVariant.copy(alpha = 0.2f))
                    }
                }
                is MdBlock.CodeFence -> {
                    ChatCodeBlock(code = block.code, language = block.language)
                }
                is MdBlock.BulletItem -> {
                    val indent = (block.depth * 16).dp
                    val annotated = applyInlineMarkdown(block.raw, codeBackground, codeContent)
                    Text(
                        text = buildAnnotatedString {
                            append("• ")
                            append(annotated)
                        },
                        style = style,
                        color = color,
                        modifier = Modifier.padding(start = indent)
                    )
                }
                is MdBlock.NumberItem -> {
                    val annotated = applyInlineMarkdown(block.raw, codeBackground, codeContent)
                    Text(
                        text = buildAnnotatedString {
                            append("${block.num}. ")
                            append(annotated)
                        },
                        style = style,
                        color = color
                    )
                }
                is MdBlock.Blockquote -> {
                    Text(
                        text = applyInlineMarkdown(block.raw, codeBackground, codeContent),
                        style = style.copy(fontStyle = FontStyle.Italic),
                        color = onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp)
                    )
                }
                is MdBlock.Hr -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
                is MdBlock.Paragraph -> {
                    Text(
                        text = applyInlineMarkdown(block.raw, codeBackground, codeContent),
                        style = style,
                        color = color
                    )
                }
            }
        }
    }
}
