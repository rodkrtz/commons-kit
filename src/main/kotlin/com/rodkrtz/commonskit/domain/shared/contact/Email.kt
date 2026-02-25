package com.rodkrtz.commonskit.domain.shared.contact

@JvmInline
public value class Email(public val value: String) : Contact {
    init {
        require(value.isNotBlank()) {
            "Email cannot be blank"
        }
        require(EMAIL_REGEX.matches(value)) {
            "Invalid email format: $value"
        }
        require(value.length <= 320) {
            "Email is too long (max 320 characters)"
        }
    }

    /**
     * Returns the domain part of the email (after @).
     *
     * @return The domain string (e.g., "example.com")
     */
    public fun domain(): String = value.substringAfter('@')

    /**
     * Returns the local part of the email (before @).
     *
     * @return The local part string (e.g., "user")
     */
    public fun localPart(): String = value.substringBefore('@')

    /**
     * Returns the email in lowercase for case-insensitive comparison.
     *
     * @return Lowercase version of the email
     */
    public fun normalized(): String = value.lowercase()

    override fun toString(): String = value

    public companion object {
        /** RFC 5322 simplified regex for email validation */
        private val EMAIL_REGEX = Regex(
            """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"""
        )

        /**
         * Attempts to create an Email, returning null if invalid.
         *
         * @param value The email string to parse
         * @return Email instance if valid, null otherwise
         */
        public fun tryParse(value: String): Email? {
            return try {
                Email(value)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }
}