package com.halloapp.tools

import java.util.*

class StringValidator() {
    fun validateString(locale: Locale, baseString: StringResource, localizedString: StringResource): String? {
        val baseFormatter = parseFormatter(baseString.text)
        val localizedFormatter = parseFormatter(localizedString.text)

        baseFormatter.sortBy { it.index }
        localizedFormatter.sortBy { it.index }

        if (baseFormatter.size != localizedFormatter.size) {
            return "${locale.identifier} / ${baseString.name} mismatched number of parameters"
        }
        for (i in 0 until baseFormatter.size) {
            val base = baseFormatter[i]
            val local = localizedFormatter[i]
            if (base != local) {
                return "${locale.identifier} / ${baseString.name} has mismatched parameter at index $i"
            }
        }
        return null
    }

    private fun parseFormatter(str: String): ArrayList<FormatSpecifier> {
        val al: ArrayList<FormatSpecifier> = ArrayList()
        var i = 0
        val len: Int = str.length
        while (i < len) {
            val nextPercent: Int = str.indexOf('%', i)
            i = if (str[i] != '%') {
                val plainTextEnd = if (nextPercent == -1) len else nextPercent
                plainTextEnd
            } else {
                val fsp = FormatSpecifierParser(str, i + 1)
                al.add(fsp.formatSpecifier)
                fsp.endIdx
            }
        }
        return al
    }
}

private class FormatSpecifier(val index: String?, val flags: String, val width: String?, val precision: String?, val tT: String?, val conv: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FormatSpecifier

        if (index != other.index) return false
        if (flags != other.flags) return false
        if (width != other.width) return false
        if (precision != other.precision) return false
        if (tT != other.tT) return false
        if (conv != other.conv) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index?.hashCode() ?: 0
        result = 31 * result + flags.hashCode()
        result = 31 * result + (width?.hashCode() ?: 0)
        result = 31 * result + (precision?.hashCode() ?: 0)
        result = 31 * result + (tT?.hashCode() ?: 0)
        result = 31 * result + conv.hashCode()
        return result
    }

}

private class FormatSpecifierParser(private val format: String, var endIdx: Int) {
    val formatSpecifier: FormatSpecifier
    private var index: String? = null
    private var flags: String
    private var width: String? = null
    private var precision: String? = null
    private var tT: String? = null
    private val conv: String
    private fun nextInt(): String {
        val strBegin = endIdx
        while (nextIsInt()) {
            advance()
        }
        return format.substring(strBegin, endIdx)
    }

    private fun nextIsInt(): Boolean {
        return !isEnd && Character.isDigit(peek())
    }

    private fun peek(): Char {
        if (isEnd) {
            throw UnknownFormatConversionException("End of String")
        }
        return format[endIdx]
    }

    private fun advance(): Char {
        if (isEnd) {
            throw UnknownFormatConversionException("End of String")
        }
        return format[endIdx++]
    }

    private fun back(len: Int) {
        endIdx -= len
    }

    private val isEnd: Boolean
        private get() = endIdx == format.length

    companion object {
        private const val FLAGS = ",-(+# 0<"
    }

    init {
        // Index
        if (nextIsInt()) {
            val nint = nextInt()
            if (peek() == '$') {
                index = nint
                advance()
            } else if (nint[0] == '0') {
                // This is a flag, skip to parsing flags.
                back(nint.length)
            } else {
                // This is the width, skip to parsing precision.
                width = nint
            }
        }
        // Flags
        flags = ""
        while (width == null && FLAGS.indexOf(peek()) >= 0) {
            flags += advance()
        }
        // Width
        if (width == null && nextIsInt()) {
            width = nextInt()
        }
        // Precision
        if (peek() == '.') {
            advance()
            if (!nextIsInt()) {
                throw IllegalFormatPrecisionException(peek().toInt())
            }
            precision = nextInt()
        }
        // tT
        if (peek() == 't' || peek() == 'T') {
            tT = advance().toString()
        }
        // Conversion
        conv = advance().toString()
        formatSpecifier = FormatSpecifier(index, flags, width, precision, tT, conv)
    }
}
