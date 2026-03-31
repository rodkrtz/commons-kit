package com.rodkrtz.commonskit.core.extraction

public fun interface ValueNormalizer {

    public fun normalize(value: String?): String?

    public companion object {
        private val WHITESPACE_REGEX: Regex = Regex("\\s+")

        public fun default(): ValueNormalizer =
            ValueNormalizer { value ->
                value
                    ?.trim()
                    ?.replace(WHITESPACE_REGEX, " ")
                    ?.removePrefix(":")
                    ?.trim()
                    ?.ifBlank { null }
            }

        public fun identity(): ValueNormalizer =
            ValueNormalizer { it }
    }
}