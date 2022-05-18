package com.silent.ilustriscore.core.utilities

import java.text.SimpleDateFormat
import java.util.*

enum class DateFormats(val formatted: String) {
    DD_OF_MM_FROM_YYYY("dd 'de' MMMM 'de' yyyy"),
    DD_MM_YYY("DD/MM/YYY"),
    MM_DD_YYY("MM/DD/YY"),
    M_D_Y("Month D, Yr"),
    EE_D_MMM_YYY("EEE, MMM d, ''yy"),
    EE_DD_MMM_YYY_HH_MM("EEE, d MMM yyyy HH:mm"),
    DD_MM_HH("DD.MM - HH")
}


fun Date.formatDate(format: String? = null): String {
    val fmt = if (format != null) SimpleDateFormat(
        format,
        Locale.getDefault()
    ) else SimpleDateFormat.getDateInstance()
    return fmt.format(this)
}

fun Date.format(format: DateFormats) : String {
    val fmt =  SimpleDateFormat(
        format.formatted,
        Locale.getDefault()
    )
    return fmt.format(this)
}