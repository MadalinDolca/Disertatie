package com.madalin.disertatie.core.domain.extension

/**
 * Returns a pretty length representation of this [Int] value.
 */
fun Int.prettyLength(): String {
    return if (this >= 1000) {
        val lengthInKilometers = this / 1000
        "$lengthInKilometers km"
    } else {
        "$this m"
    }
}

/**
 * Returns a pretty length representation of this [Float] value.
 */
fun Float.prettyLength(): String {
    return if (this >= 1000) {
        val lengthInKilometers = this / 1000
        "%.2f km".format(lengthInKilometers)
    } else {
        "%.2f m".format(this)
    }
}