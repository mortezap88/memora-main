package com.example.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

// Box-specific high-visibility colors for quoted words/phrases
val QuotedHighlightRed = Color(0xFFEF4444)
val QuotedHighlightYellow = Color(0xFFFBBF24)
val QuotedHighlightPurple = Color(0xFFA855F7)
val QuotedHighlightColor = QuotedHighlightRed

/**
 * Builds an AnnotatedString that highlights all text enclosed strictly in double quotes ("..." or “...”) in red bold font.
 * Also handles markdown bold (**...**).
 */
fun buildAnnotatedStringWithQuotes(
    text: String,
    quoteColor: Color = QuotedHighlightColor,
    defaultColor: Color = Color.Unspecified
): AnnotatedString {
    // Regex matching:
    // 1. Double quotes strictly: "[^"]+" or “[^”]+” or ”[^“]+”
    // 2. Markdown bold: \*\*[^*]+\*\*
    val pattern = Regex("""("[^"]+"|[“”][^“”]+[“”]|\*\*[^*]+\*\*)""")

    return buildAnnotatedString {
        var currentIndex = 0
        pattern.findAll(text).forEach { matchResult ->
            val range = matchResult.range
            if (range.first > currentIndex) {
                append(text.substring(currentIndex, range.first))
            }

            val matchedValue = matchResult.value
            if (matchedValue.startsWith("**") && matchedValue.endsWith("**") && matchedValue.length >= 4) {
                val inner = matchedValue.substring(2, matchedValue.length - 2)
                val isQuoted = (inner.startsWith("\"") && inner.endsWith("\"")) ||
                               (inner.startsWith("“") && inner.endsWith("”"))
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = if (isQuoted) quoteColor else defaultColor))
                append(inner)
                pop()
            } else {
                // Strictly double-quoted text -> Highlight in Red with bold weight for high visibility
                pushStyle(SpanStyle(color = quoteColor, fontWeight = FontWeight.Bold))
                append(matchedValue)
                pop()
            }

            currentIndex = range.last + 1
        }

        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}

@Composable
fun FormattedQuotedText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    lineHeight: TextUnit = 20.sp,
    color: Color = MaterialTheme.colorScheme.onSurface,
    quoteColor: Color = QuotedHighlightColor,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    val annotatedString = remember(text, quoteColor, color) {
        buildAnnotatedStringWithQuotes(text, quoteColor = quoteColor, defaultColor = color)
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        fontSize = fontSize,
        lineHeight = lineHeight,
        color = color,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}
