package com.ahmetyuksell.agent.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toReadableDate(): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    return when {
        diff < 60_000 -> "just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> toReadableDate()
    }
}

fun String.truncate(maxLength: Int, ellipsis: String = "…"): String =
    if (length <= maxLength) this else take(maxLength - ellipsis.length) + ellipsis

fun <T> Flow<T>.catchAndLog(tag: String = "Flow"): Flow<T> =
    catch { e -> Timber.tag(tag).e(e, "Flow error") }
