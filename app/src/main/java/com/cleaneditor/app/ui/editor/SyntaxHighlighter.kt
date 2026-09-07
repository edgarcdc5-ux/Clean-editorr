package com.cleaneditor.app.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.cleaneditor.app.data.model.EditorFileType

/** Lightweight syntax highlighting without changing the underlying editable text. */
object SyntaxHighlighter {
    private val keywordStyle = SpanStyle(color = Color(0xFF7C4DFF))
    private val stringStyle = SpanStyle(color = Color(0xFF2E7D32))
    private val numberStyle = SpanStyle(color = Color(0xFF1565C0))
    private val commentStyle = SpanStyle(color = Color(0xFF757575))
    private val tagStyle = SpanStyle(color = Color(0xFFC62828))
    private val propertyStyle = SpanStyle(color = Color(0xFF00838F))

    fun highlight(text: String, type: EditorFileType): AnnotatedString {
        if (text.isEmpty()) return AnnotatedString("")
        val builder = AnnotatedString.Builder(text)
        when (type) {
            EditorFileType.JSON -> highlightJson(text, builder)
            EditorFileType.JAVASCRIPT -> highlightJavaScript(text, builder)
            EditorFileType.HTML, EditorFileType.XML -> highlightMarkup(text, builder)
            EditorFileType.CSS -> highlightCss(text, builder)
            EditorFileType.MARKDOWN -> highlightMarkdown(text, builder)
            EditorFileType.YAML -> highlightYaml(text, builder)
            EditorFileType.CSV, EditorFileType.TXT, EditorFileType.LOG -> Unit
        }
        return builder.toAnnotatedString()
    }

    fun visualTransformation(type: EditorFileType): VisualTransformation = VisualTransformation { text ->
        TransformedText(highlight(text.text, type), OffsetMapping.Identity)
    }

    private fun highlightJson(text: String, builder: AnnotatedString.Builder) {
        applyRegex(builder, text, Regex("\"(?:\\\\.|[^\"])*\"(?=\\s*:)") , propertyStyle)
        applyRegex(builder, text, Regex("\"(?:\\\\.|[^\"])*\""), stringStyle)
        applyRegex(builder, text, Regex("\\b(?:true|false|null)\\b"), keywordStyle)
        applyRegex(builder, text, Regex("-?\\b\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?\\b"), numberStyle)
    }

    private fun highlightJavaScript(text: String, builder: AnnotatedString.Builder) {
        applyRegex(builder, text, Regex("//[^\\n]*|/\\*[\\s\\S]*?\\*/"), commentStyle)
        applyRegex(builder, text, Regex("\"(?:\\\\.|[^\"])*\"|'(?:\\\\.|[^'\\\\])*'|`(?:\\\\.|[^`\\\\])*`"), stringStyle)
        applyRegex(builder, text, Regex("\\b(?:const|let|var|function|return|if|else|for|while|class|new|import|from|export|async|await|true|false|null|undefined)\\b"), keywordStyle)
        applyRegex(builder, text, Regex("\\b\\d+(?:\\.\\d+)?\\b"), numberStyle)
    }

    private fun highlightMarkup(text: String, builder: AnnotatedString.Builder) {
        applyRegex(builder, text, Regex("</?[A-Za-z][^>]*>|<!--[\\s\\S]*?-->"), tagStyle)
        applyRegex(builder, text, Regex("\\b[A-Za-z_:][-A-Za-z0-9_:.]*(?=\\s*=)"), propertyStyle)
        applyRegex(builder, text, Regex("\"[^\"]*\"|'[^']*'"), stringStyle)
        applyRegex(builder, text, Regex("<!--[\\s\\S]*?-->"), commentStyle)
    }

    private fun highlightCss(text: String, builder: AnnotatedString.Builder) {
        applyRegex(builder, text, Regex("/\\*[\\s\\S]*?\\*/"), commentStyle)
        applyRegex(builder, text, Regex("#[0-9A-Fa-f]{3,8}\\b|\\b\\d+(?:\\.\\d+)?(?:px|em|rem|%|vh|vw|s|ms)?\\b"), numberStyle)
        applyRegex(builder, text, Regex("[A-Za-z-]+(?=\\s*:)") , propertyStyle)
    }

    private fun highlightMarkdown(text: String, builder: AnnotatedString.Builder) {
        applyRegex(builder, text, Regex("(?m)^#{1,6}\\s+.*$|\\*\\*[^*]+\\*\\*|__[^_]+__"), keywordStyle)
        applyRegex(builder, text, Regex("`[^`]+`"), stringStyle)
        applyRegex(builder, text, Regex("(?m)^\\s*>.*$|(?m)^\\s*[-*+]\\s+"), commentStyle)
    }

    private fun highlightYaml(text: String, builder: AnnotatedString.Builder) {
        applyRegex(builder, text, Regex("(?m)^\\s*#.*$"), commentStyle)
        applyRegex(builder, text, Regex("\\b(?:true|false|null|yes|no)\\b"), keywordStyle)
        applyRegex(builder, text, Regex("\"[^\"]*\"|'[^']*'"), stringStyle)
        applyRegex(builder, text, Regex("(?m)^\\s*[A-Za-z_][\\w.-]*(?=\\s*:)") , propertyStyle)
        applyRegex(builder, text, Regex("\\b\\d+(?:\\.\\d+)?\\b"), numberStyle)
    }

    private fun applyRegex(builder: AnnotatedString.Builder, text: String, regex: Regex, style: SpanStyle) {
        regex.findAll(text).forEach { match -> builder.addStyle(style, match.range.first, match.range.last + 1) }
    }
}
