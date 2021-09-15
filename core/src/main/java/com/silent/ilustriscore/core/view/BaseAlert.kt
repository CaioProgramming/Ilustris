package com.silent.ilustriscore.core.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.silent.ilustriscore.core.utilities.DialogStyles

abstract class BaseAlert<V>(
    var context: Context,
    private val layout: Int,
    val style: DialogStyles = DialogStyles.DEFAULT_NO_BORDER,
    private val onShowDialog: (() -> Unit)? = null,
    private val onDismiss: (() -> Unit)? = null
) : DialogInterface.OnShowListener, DialogInterface.OnDismissListener where V : ViewDataBinding {


    val dialog = if (style == DialogStyles.BOTTOM_NO_BORDER) BottomSheetDialog(context) else Dialog(
        context,
        style.resource
    )
    val view: View by lazy {
        LayoutInflater.from(context).inflate(layout, null, false).rootView
    }

    lateinit var viewBind: V

    abstract fun V.configure()

    fun buildDialog() {
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCanceledOnTouchOutside(true)
            setOnShowListener(this@BaseAlert)
            setOnDismissListener(this@BaseAlert)
            setContentView(view)
            viewBind = DataBindingUtil.bind(view)!!
            viewBind.configure()
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