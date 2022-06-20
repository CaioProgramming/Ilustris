package com.ilustris.ui.alert

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import com.google.android.material.bottomsheet.BottomSheetDialog

abstract class BaseAlert(
    var context: Context,
    private val layout: Int,
    val style: DialogStyles = DialogStyles.DEFAULT_NO_BORDER,
    private val onShowDialog: (() -> Unit)? = null,
    private val onDismiss: (() -> Unit)? = null
) : DialogInterface.OnShowListener, DialogInterface.OnDismissListener {


    val dialog = if (style == DialogStyles.BOTTOM_NO_BORDER) BottomSheetDialog(context) else Dialog(
        context,
        style.resource
    )
    val view: View by lazy {
        LayoutInflater.from(context).inflate(layout, null, false).rootView
    }


    abstract fun View.configure()

    fun buildDialog() {
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCanceledOnTouchOutside(true)
            setOnShowListener(this@BaseAlert)
            setOnDismissListener(this@BaseAlert)
            setContentView(view)
            view.configure()
            dialog.show()
        }
    }

    override fun onShow(p0: DialogInterface?) {
        onShowDialog?.invoke()
    }

    override fun onDismiss(p0: DialogInterface?) {
        onDismiss?.invoke()
    }

}