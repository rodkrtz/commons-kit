package com.rodkrtz.commonskit.domain.shared.privacy

public enum class DisplayMode {
    /** ex: 12345678901 */
    RAW,

    /** ex: 123.456.789-01 */
    FORMATTED,

    /** ex: 123.***.***-01 */
    MASKED;
}