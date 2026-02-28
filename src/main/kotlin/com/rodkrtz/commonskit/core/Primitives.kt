package com.rodkrtz.commonskit.core

import java.math.RoundingMode

public fun Double.round(decimals: Int, mode: RoundingMode): Double {
    return this.toBigDecimal().setScale(decimals, mode).toDouble()
}