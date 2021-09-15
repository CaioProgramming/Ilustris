package com.ilustris.app.view.dialog

import android.Manifest
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.ilustris.app.AppDTO
import com.ilustris.app.R
import com.ilustris.app.databinding.AddNewAppLayoutBinding
import com.silent.ilustriscore.core.utilities.DialogStyles
import com.silent.ilustriscore.core.view.BaseAlert
import gun0912.tedbottompicker.TedBottomPicker

class NewAppDialog(context: Context, val onSaveApp: (AppDTO) -> Unit) :
    BaseAlert<AddNewAppLayoutBinding>(
        context,
        R.layout.add_new_app_layout,
        DialogStyles.BOTTOM_NO_BORDER
    ), PermissionListener {

    private val appDTO = AppDTO()

    override fun AddNewAppLayoutBinding.configure() {
        appIconImageView.setOnClickListener {
            openPicker()
        }
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

    private fun requestPermissions() {
        TedPermission.with(context)
            .setPermissionListener(this)
            .setDeniedMessage("Se você não aceitar essa permissão não poderá adicionar o ícones...\n\nPor favor ligue as permissões em [Configurações] > [Permissões]")
            .setPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .check()
    }

    private fun checkPermissions(): Boolean {
        val read = TedPermission.isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        val write = TedPermission.isGranted(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return read && write
    }

    private fun openPicker() {
        val act = context as FragmentActivity
        TedBottomPicker.with(act)
            .setPeekHeight(1000)
            .showTitle(false)
            .showCameraTile(false)
            .setSelectMaxCount(1)
            .show {
                it.path?.let { path ->
                    Glide.with(context).load(path).into(viewBind.appIconImageView)
                    appDTO.appIcon = path
                }
            }
    }

    override fun onPermissionGranted() {
        openPicker()
    }

    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
        viewBind.errorMessage.text =
            "Se você não aceitar essa permissão não poderá adicionar o ícone"
    }

}