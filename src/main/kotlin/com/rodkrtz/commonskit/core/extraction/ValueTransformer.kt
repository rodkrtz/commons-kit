package com.rodkrtz.commonskit.core.extraction

import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

public fun interface ValueTransformer<T> {

    public fun transform(value: String): T?

    public companion object {

        public fun string(): ValueTransformer<String> =
            ValueTransformer { it }

        public fun int(): ValueTransformer<Int> =
            ValueTransformer(String::toIntOrNull)

        public fun long(): ValueTransformer<Long> =
            ValueTransformer(String::toLongOrNull)

        public fun boolean(
            trueValues: Set<String> = setOf("true", "sim", "yes", "1"),
            falseValues: Set<String> = setOf("false", "não", "nao", "no", "0")
        ): ValueTransformer<Boolean> {
            val trueSet = trueValues.map(String::lowercase).toSet()
            val falseSet = falseValues.map(String::lowercase).toSet()

            return ValueTransformer { value ->
                when (value.trim().lowercase()) {
                    in trueSet -> true
                    in falseSet -> false
                    else -> null
                }
            }
        }

        public fun bigDecimal(
            decimalSeparator: Char = ',',
            thousandsSeparator: Char = '.'
        ): ValueTransformer<BigDecimal> =
            ValueTransformer { value ->
                value
                    .replace(thousandsSeparator.toString(), "")
                    .replace(decimalSeparator, '.')
                    .replace(Regex("[^0-9.-]"), "")
                    .toBigDecimalOrNull()
            }

        public fun localDate(
            formatter: DateTimeFormatter
        ): ValueTransformer<LocalDate> =
            ValueTransformer { value ->
                runCatching { LocalDate.parse(value, formatter) }.getOrNull()
            }

        public fun <T> custom(
            transformer: (String) -> T?
        ): ValueTransformer<T> =
            ValueTransformer(transformer)
    }
}