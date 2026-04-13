package com.rodkrtz.commonskit.core

public fun String.formatEscaped(vararg args: String): String =
    format(*args.map {
        Regex.escape(it)
    }.toTypedArray())

/**
 * Removes Unicode BOM and zero-width/invisible formatting characters from a string.
 *
 * Useful when content comes from camera scanners, clipboard or external pages where
 * a leading BOM (`\uFEFF`) or hidden separators can break URL parsing.
 */
public fun String.removeBomAndInvisibleChars(): String {
    var foundInvisible = false
    for (char in this) {
        if (char.isBomOrInvisibleFormatting()) {
            foundInvisible = true
            break
        }
    }
    if (!foundInvisible) return this

    val cleaned = StringBuilder(length)
    for (char in this) {
        if (!char.isBomOrInvisibleFormatting()) {
            cleaned.append(char)
        }
    }
    return cleaned.toString()
}

private fun Char.isBomOrInvisibleFormatting(): Boolean =
    this == '\uFEFF' ||
        this == '\u200B' ||
        this == '\u200C' ||
        this == '\u200D' ||
        this == '\u2060'
