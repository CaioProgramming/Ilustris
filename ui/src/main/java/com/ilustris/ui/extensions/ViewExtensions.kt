package com.ilustris.ui.extensions

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Typeface
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun Snackbar.config() {
    ViewCompat.setElevation(this.view, 6f)
    show()
}

fun View.showSnackBar(
    message: String,
    backColor: Int = Color.BLACK,
    textColor: Int = Color.WHITE,
    actionText: String? = null,
    action: (() -> Unit)? = null
) {

    Snackbar.make(context, this, message, Snackbar.LENGTH_LONG).apply {
        setTextColor(textColor)
        setBackgroundTint(backColor)
        actionText?.let {
            setAction(actionText) {
                action?.invoke()
            }
        }
    }.config()


}

fun TextView.getTypeFace(path: String): Typeface {
    return Typeface.createFromAsset(context.assets, path)
}

fun Context.isDarkMode(): Boolean {
    return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_YES -> true
        Configuration.UI_MODE_NIGHT_NO -> false
        Configuration.UI_MODE_NIGHT_UNDEFINED -> false
        else -> false
    }
}

fun Context.getColorResource(resource: Int) = ContextCompat.getColor(this, resource)

/**
@param saturate ratio must be between 0.0 and 1.0
 **/
fun ImageView.setSaturation(saturateRatio: Float) {
    val matrix = ColorMatrix().apply {
        setSaturation(saturateRatio)
    }
    val filter = ColorMatrixColorFilter(matrix)
    colorFilter = filter
}
