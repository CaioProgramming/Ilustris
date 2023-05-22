package com.ilustris.app.view.dialog

import android.content.Context
import android.view.View
import com.bumptech.glide.Glide
import com.ilustris.app.AppDTO
import com.ilustris.app.R
import com.ilustris.app.databinding.AddNewAppLayoutBinding
import com.ilustris.ui.alert.BaseAlert
import com.ilustris.ui.alert.DialogStyles

class NewAppDialog(
    context: Context,
    private var appDTO: AppDTO = AppDTO(),
    private val onSaveApp: (AppDTO) -> Unit
) :
    BaseAlert(
        context,
        R.layout.add_new_app_layout,
        DialogStyles.BOTTOM_NO_BORDER
    ) {

    private var addNewAppLayoutBinding: AddNewAppLayoutBinding? = null

    override fun View.configure() {
        addNewAppLayoutBinding = AddNewAppLayoutBinding.bind(view)
        addNewAppLayoutBinding?.run {
            appNameEditText.setText(appDTO.appName)
            appIconImageView.setOnClickListener {
                openPicker()
            }
            appDescriptionEditText.setText(appDTO.appName)
            Glide.with(context).load(appDTO.appIcon).placeholder(R.drawable.ic_square_7)
                .into(appIconImageView)
            appLinkEditText.setText(appDTO.url)
            saveAppButton.setOnClickListener {
                onSaveApp.invoke(appDTO.apply {
                    appDTO.description = appDescriptionEditText.text.toString()
                    appDTO.url = appLinkEditText.text.toString()
                    appDTO.appName = appNameEditText.text.toString()
                })
                dialog.dismiss()
            }
            if (checkPermissions()) {
                openPicker()
            } else {
                requestPermissions()
            }
        }

    }

    private fun requestPermissions() {

    }

    private fun checkPermissions(): Boolean {
        return false
    }

    private fun openPicker() {

    }


}