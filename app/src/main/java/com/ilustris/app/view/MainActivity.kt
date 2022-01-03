package com.ilustris.app.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.common.util.CollectionUtils.listOf
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ilustris.app.*
import com.ilustris.app.view.adapter.AppsAdapter
import com.ilustris.app.view.dialog.ContactDialog
import com.ilustris.app.view.dialog.NewAppDialog
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.model.ViewModelBaseState
import com.silent.ilustriscore.core.utilities.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val viewModel = IlustrisViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getInTouch.setOnClickListener {
            ContactDialog(this).buildDialog()
        }
        observeViewModel()
        viewModel.getAllData()
        login()
    }

    private fun showNewAppDialog() {
        NewAppDialog(this) { newApp ->
            viewModel.newAppDTO = newApp
            viewModel.uploadFile(newApp.appIcon)
        }.buildDialog()
    }

    private fun observeViewModel() {
        viewModel.viewModelState.observe(this, {
            when (it) {
                ViewModelBaseState.DataDeletedState -> {
                    getView().showSnackBar("App removido com sucesso")
                    viewModel.getAllData()
                }
                is ViewModelBaseState.DataListRetrievedState -> {
                    setupRecyclerview(it.dataList)
                }
                is ViewModelBaseState.DataSavedState -> {
                    getView().showSnackBar("App salvo com sucesso")
                    viewModel.getAllData()
                }
                is ViewModelBaseState.DataUpdateState -> getView().showSnackBar("App atualizado com sucesso")

                is ViewModelBaseState.ErrorState -> getView().showSnackBar(
                    backColor = ContextCompat.getColor(this, R.color.md_red500),
                    message = "Ocorreu um erro inesperado"
                )

                is ViewModelBaseState.FileUploadedState -> {
                    viewModel.saveData(viewModel.newAppDTO.apply {
                        this.appIcon = it.downloadUrl.toString()
                    })
                }

                is ViewModelBaseState.DataRetrievedState -> TODO()
                ViewModelBaseState.RequireAuth -> {
                    login()
                }
            }
        })
    }

    private fun setupRecyclerview(dataList: List<BaseBean>) {
        delayedFunction(3000) {
            appbar.setExpanded(false, true)
        }
        val listApp = dataList as appList
        val appList = ArrayList(listApp.sortedBy { data -> data.url.isEmpty() })
        if (viewModel.isAuthenticated()) {
            appList.add(AppDTO(id = ADDNEWAPP))
        } else {
            viewModel.checkAuth()
        }
        val appsAdapter = AppsAdapter(appList, {
            showNewAppDialog()
        }, {
            requestAppDelete(it)
        }, { appDTO ->
            NewAppDialog(this, appDTO) {
                viewModel.editData(it)
            }.buildDialog()
        })
        appsRecyclerView.adapter = appsAdapter
    }

    private fun requestAppDelete(it: AppDTO) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Atenção")
            .setMessage("Tem certeza que deseja excluir esse app?")
            .setPositiveButton(
                "Cancelar"
            ) { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Confirmar") { dialog, which ->
                viewModel.deleteData(it.id)
                dialog.dismiss()
            }.show()
    }

    private fun login() {
        val providers = listOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build()
        )
        if (viewModel.currentUser != null) {
            LoginHelper.signIn(this, providers, R.style.Ilustris_Theme, R.mipmap.ic_launcher)
        } else {
            viewModel.getAllData()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                viewModel.getAllData()
            } else {
                if (response != null) {
                    getView().showSnackBar("Ocorreu um erro ao realizar o login, tente novamente",
                        actionText = "Ok", action = {
                            login()
                        })

                }
            }
        }
    }
}