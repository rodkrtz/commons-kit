package com.rodkrtz.commonskit.domain.shared.contact

import com.rodkrtz.commonskit.domain.shared.region.Country

public interface Phone : Contact {
    public val value: String
    public val country: Country

    public fun onlyDigits(input: CharSequence?): String? {
        if (input.isNullOrBlank()) return null
        val buf = CharArray(input.length)
        var p = 0
        for (c in input) if (c in '0'..'9') buf[p++] = c
        if (p == 0) return null
        return String(buf, 0, p)
    }
}