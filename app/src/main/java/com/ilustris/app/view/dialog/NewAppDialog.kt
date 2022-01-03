package com.ilustris.app.view.dialog

import android.Manifest
import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.ilustris.app.AppDTO
import com.ilustris.app.R
import com.silent.ilustriscore.core.utilities.DialogStyles
import com.silent.ilustriscore.core.view.BaseAlert
import gun0912.tedbottompicker.TedBottomPicker
import kotlinx.android.synthetic.main.add_new_app_layout.view.*

class NewAppDialog(
    context: Context,
    private var appDTO: AppDTO = AppDTO(),
    val onSaveApp: (AppDTO) -> Unit
) :
    BaseAlert(
        context,
        R.layout.add_new_app_layout,
        DialogStyles.BOTTOM_NO_BORDER
    ), PermissionListener {


    override fun View.configure() {

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
                    Glide.with(context).load(path).into(view.appIconImageView)
                    appDTO.appIcon = path
                }
            }
    }

    override fun onPermissionGranted() {
        openPicker()
    }

    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
        view.error_message.text =
            "Se você não aceitar essa permissão não poderá adicionar o ícone"
    }

}