package com.rodkrtz.commonskit.core

public fun String.formatEscaped(vararg args: String): String =
    format(*args.map {
        Regex.escape(it)
    }.toTypedArray())