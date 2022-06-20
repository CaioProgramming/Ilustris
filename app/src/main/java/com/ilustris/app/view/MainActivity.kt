package com.ilustris.app.view

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.common.util.CollectionUtils.listOf
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ilustris.app.*
import com.ilustris.app.databinding.ActivityMainBinding
import com.ilustris.app.view.adapter.AppsAdapter
import com.ilustris.app.view.dialog.ContactDialog
import com.ilustris.app.view.dialog.NewAppDialog
import com.ilustris.ui.auth.LoginHelper
import com.ilustris.ui.extensions.ERROR_COLOR
import com.ilustris.ui.extensions.getView
import com.ilustris.ui.extensions.showSnackBar
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.model.ViewModelBaseState
import com.silent.ilustriscore.core.utilities.delayedFunction

class MainActivity : AppCompatActivity() {

    lateinit var mainBinding: ActivityMainBinding
    private val viewModel by lazy {
        IlustrisViewModel(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        mainBinding.getInTouch.setOnClickListener {
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
        viewModel.viewModelState.observe(this) {
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
                ViewModelBaseState.LoadCompleteState -> {
                    delayedFunction(3000) {
                        mainBinding.appbar.setExpanded(false, true)
                    }
                }
                ViewModelBaseState.LoadingState -> {
                    delayedFunction(1000) {
                        mainBinding.appbar.setExpanded(true, true)
                    }
                }
            }
        }
    }

    private fun setupRecyclerview(dataList: List<BaseBean>) {

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
        mainBinding.appsRecyclerView.adapter = appsAdapter
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
        if (viewModel.getUser() != null) {
            LoginHelper.signIn(
                this,
                providers,
                R.style.Ilustris_Theme,
                R.mipmap.ic_launcher
            ) { resultCode ->
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.getAllData()
                } else {
                    getView().showSnackBar(
                        "Ocorreu um erro ao realizar o login, tente novamente",
                        actionText = "Ok", action = {
                            login()
                        }, backColor = getColor(ERROR_COLOR)
                    )

                }
            }
        } else {
            viewModel.getAllData()
        }
    }

}