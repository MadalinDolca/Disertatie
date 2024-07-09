package com.madalin.disertatie.core.domain.extension

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formats this [Long] timestamp to a `HH:mm:ss` string.
 */
fun Long.asTime(): String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(this)

/**
 * Formats this [Long] timestamp to a `dd.MM.yyyy` string.
 */
fun Long.asDate(): String = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(this)

/**
 * Formats this [Long] timestamp to a `dd.MM.yyyy HH:mm:ss` string.
 */
fun Long.asDateAndTime(): String = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(this)

/**
 * Formats this [Long] UNIX UTC timestamp to a `dd.MM.yyyy HH:mm` string.
 */
fun Long.utcUnixAsDateAndTime(): String {
    val date = Date(this * 1000)
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}

/**
 * Formats this [Long] UNIX UTC timestamp to a `dd.MM` string.
 */
fun Long.utcUnixAsDayAndMonth(): String {
    val date = Date(this * 1000)
    val format = SimpleDateFormat("dd.MM", Locale.getDefault())
    return format.format(date)
}

/**
 * Formats this [Long] timestamp to a duration representation string.
 */
fun Long.asDuration(): String {
    val hours = this / (1000 * 60 * 60)
    val remainingSeconds = this % (1000 * 60 * 60)
    val minutes = remainingSeconds / (1000 * 60)
    val seconds = remainingSeconds % (1000 * 60) / 1000

    val hourString = if (hours > 0) "%02d" + "h" else ""
    val minuteString = if (minutes > 0) "%02d" + "m" else ""
    val secondString = if (seconds > 0) "%02d" + "s" else ""

    return if (hourString.isNotEmpty()) String.format("$hourString $minuteString $secondString", hours, minutes, seconds)
    else if (minuteString.isNotEmpty()) String.format("$minuteString $secondString", minutes, seconds)
    else String.format(secondString, seconds)
}