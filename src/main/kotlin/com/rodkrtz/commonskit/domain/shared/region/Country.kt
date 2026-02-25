package com.rodkrtz.commonskit.domain.shared.region

public enum class Country(
    public val alpha3: String,
    public val alpha2: String,
    public val countryName: String
) : Region {
    BRA("BRA", "BR", "Brazil"),
    USA("USA", "US", "United States"),

    UNK("UNK", "UW", "Unknown");


    public companion object {

        public fun fromString(value: String) : Country? {
            return fromCodeAlpha3(value)
                ?: fromCodeAlpha2(value)
                ?: fromCountryName(value)
        }

        public fun fromCodeAlpha3(code: String): Country? {
            return entries.find {
                it.alpha3.equals(code, ignoreCase = true)
            }
        }

        public fun fromCodeAlpha2(code: String): Country? {
            return entries.find {
                it.alpha2.equals(code, ignoreCase = true)
            }
        }

        public fun fromCountryName(name: String): Country? {
            return entries.find {
                it.countryName.equals(name, ignoreCase = true)
            }
        }

    }
}