package com.silent.ilustriscore.core.utilities

import android.content.Context
import android.graphics.Typeface
import java.text.SimpleDateFormat
import java.util.*

object TextUtils {

    fun getTypeFace(context: Context, path: String): Typeface {
        return Typeface.createFromAsset(context.assets, path)
    }

}