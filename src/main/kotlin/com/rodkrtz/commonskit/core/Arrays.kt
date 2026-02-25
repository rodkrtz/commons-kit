package com.rodkrtz.commonskit.core

import kotlin.random.Random

public object Arrays {

    @JvmStatic
    public fun generateRandoms(size: Int): IntArray {
       return IntArray(size) { Random.Default.nextInt(0, 10) }
    }
}