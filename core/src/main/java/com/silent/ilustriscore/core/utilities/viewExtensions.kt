package com.silent.ilustriscore.core.utilities

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar
import com.silent.ilustriscore.R

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
    val params = this.view.layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(12, 12, 12, 12)
    this.view.layoutParams = params
    this.view.background = ContextCompat.getDrawable(context, R.drawable.bg_snackbar)
    ViewCompat.setElevation(this.view, 6f)
    show()
}

fun showSnackBar(context: Context,
                 message: String,
                 backColor: Int = Color.BLACK,
                 textColor: Int = Color.WHITE,
                 rootView: View) {
    if (context is Activity) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
                .setTextColor(textColor)
                .setBackgroundTint(backColor)
                .config()
    }


}
