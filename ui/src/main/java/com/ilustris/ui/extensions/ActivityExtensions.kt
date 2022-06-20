package com.ilustris.ui.extensions

import android.view.View
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.getView(): View = findViewById<View>(android.R.id.content).rootView

fun AppCompatActivity.hideBackButton() {
    val activity: AppCompatActivity = this
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
}

fun AppCompatActivity.showSupportActionBar() {
    val activity: AppCompatActivity = this
    activity.supportActionBar?.show()
}