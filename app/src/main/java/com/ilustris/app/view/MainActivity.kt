package com.ilustris.app.view

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.ilustris.app.*
import com.ilustris.app.view.adapter.AppsAdapter
import com.ilustris.app.view.dialog.NewAppDialog
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.model.ViewModelBaseActions
import com.silent.ilustriscore.core.model.ViewModelBaseState
import com.silent.ilustriscore.core.utilities.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val viewModel = IlustrisViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        observeViewModel()
        login()
    }

    private fun setupView() {

    }

    private fun showNewAppDialog() {
        NewAppDialog(this) { newApp ->
            viewModel.newAppDTO = newApp
            viewModel.dispatchViewAction(ViewModelBaseActions.UploadFileAction(newApp.appIcon))
        }.buildDialog()
    }

    private fun observeViewModel() {
        viewModel.viewModelState.observe(this, {
            when (it) {
                ViewModelBaseState.DataDeletedState -> {
                    getView().showSnackBar("App removido com sucesso")
                }
                is ViewModelBaseState.DataListRetrievedState -> {
                    setupRecyclerview(it.dataList)
                }
                is ViewModelBaseState.DataSavedState -> {
                    getView().showSnackBar("App salvo com sucesso")
                }
                is ViewModelBaseState.DataUpdateState -> getView().showSnackBar("App atualizado com sucesso")

                is ViewModelBaseState.ErrorState -> getView().showSnackBar(
                    backColor = ContextCompat.getColor(this, R.color.md_red500),
                    message = "Ocorreu um erro inesperado"
                )

                is ViewModelBaseState.FileUploadedState -> {
                    viewModel.dispatchViewAction(ViewModelBaseActions.SaveDataAction(viewModel.newAppDTO.apply {
                        this.appIcon = it.downloadUrl.toString()
                    }))
                }

                else -> {}
            }
        })
    }

    private fun setupRecyclerview(dataList: List<BaseBean>) {
        if (appsRecyclerView.childCount == 0) {
            delayedFunction(3000) {
                appbar.setExpanded(false, true)
            }
        }
        val listApp = dataList as appList
        val appList = ArrayList(listApp.sortedBy { data -> data.url.isEmpty() })
        appList.add(AppDTO(id = ADDNEWAPP))
        val appsAdapter = AppsAdapter(appList) {
            showNewAppDialog()
        }
        appsRecyclerView.adapter = appsAdapter
    }

    private fun login() {
        val providers = listOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build()
        )
        if (FirebaseAuth.getInstance().currentUser == null) {
            LoginHelper.signIn(this, providers, R.style.Ilustris_Theme, R.mipmap.ic_launcher)
        } else {
            viewModel.dispatchViewAction(ViewModelBaseActions.GetAllDataAction)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                viewModel.dispatchViewAction(ViewModelBaseActions.GetAllDataAction)
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