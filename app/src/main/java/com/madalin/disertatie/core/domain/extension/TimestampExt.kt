package com.madalin.disertatie.core.domain.extension

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Formats this [Long] timestamp to a `HH:mm:ss` string.
 */
fun Long.asTime() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(this)

/**
 * Formats this [Long] timestamp to a `dd.MM.yyyy` string.
 */
fun Long.asDate() = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(this)

/**
 * Formats this [Long] timestamp to a `dd.MM.yyyy HH:mm:ss` string.
 */
fun Long.asDateAndTime() = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(this)