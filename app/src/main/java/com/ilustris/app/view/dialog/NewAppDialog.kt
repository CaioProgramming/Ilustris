package com.ilustris.app.view.dialog

import android.app.Activity
import android.net.Uri
import android.view.View
import com.bumptech.glide.Glide
import com.ilustris.app.AppDTO
import com.ilustris.app.R
import com.ilustris.app.databinding.AddNewAppLayoutBinding
import com.ilustris.ui.alert.BaseAlert
import com.ilustris.ui.alert.DialogStyles

class NewAppDialog(
    private val activity: Activity,
    private var appDTO: AppDTO = AppDTO(),
    private val launchPicker: () -> Unit,
    private val onSaveApp: (AppDTO) -> Unit
) :
    BaseAlert(
        activity,
        R.layout.add_new_app_layout,
        DialogStyles.BOTTOM_NO_BORDER
    ) {

    private var addNewAppLayoutBinding: AddNewAppLayoutBinding? = null


    override fun View.configure() {
        addNewAppLayoutBinding = AddNewAppLayoutBinding.bind(view)
        addNewAppLayoutBinding?.run {
            appNameEditText.setText(appDTO.name)
            appIconImageView.setOnClickListener {
                openPicker()
            }
            appDescriptionEditText.setText(appDTO.name)
            Glide.with(context).load(appDTO.icon).placeholder(R.drawable.ic_square_7)
                .into(appIconImageView)
            appLinkEditText.setText(appDTO.url)
            saveAppButton.setOnClickListener {
                onSaveApp.invoke(appDTO.apply {
                    appDTO.description = appDescriptionEditText.text.toString()
                    appDTO.url = appLinkEditText.text.toString()
                    appDTO.name = appNameEditText.text.toString()
                })
                dialog.dismiss()
            }

        }

    }

    private fun requestPermissions() {

    }

    private fun checkPermissions(): Boolean {
        return false
    }

    private fun openPicker() {
        launchPicker()
    }

    fun updateIcon(uri: Uri) {
        appDTO.icon = uri.path.toString()
        addNewAppLayoutBinding?.run {
            Glide.with(context).load(uri).placeholder(R.drawable.ic_round_star_border_24)
                .into(appIconImageView)
        }
    }


}