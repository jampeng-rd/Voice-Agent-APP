package com.jam.voiceagent.ui.util

import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val taipeiZoneId: ZoneId = ZoneId.of("Asia/Taipei")
private val displayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun formatServerTimestampToTaipei(
    rawTimestamp: String?,
    fallback: String = "時間未知"
): String {
    if (rawTimestamp.isNullOrBlank()) return fallback
    return runCatching {
        OffsetDateTime
            .parse(rawTimestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            .atZoneSameInstant(taipeiZoneId)
            .format(displayFormatter)
    }.getOrElse { fallback }
}
