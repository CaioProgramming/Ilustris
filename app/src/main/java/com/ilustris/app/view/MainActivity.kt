package com.ilustris.app.view

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.common.util.CollectionUtils.listOf
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ilustris.app.ADDNEWAPP
import com.ilustris.app.AppDTO
import com.ilustris.app.IlustrisViewModel
import com.ilustris.app.R
import com.ilustris.app.appList
import com.ilustris.app.databinding.ActivityMainBinding
import com.ilustris.app.view.adapter.AppsAdapter
import com.ilustris.app.view.dialog.ContactDialog
import com.ilustris.app.view.dialog.NewAppDialog
import com.ilustris.ui.extensions.ERROR_COLOR
import com.ilustris.ui.extensions.getView
import com.ilustris.ui.extensions.showSnackBar
import com.silent.ilustriscore.core.contract.ErrorType
import com.silent.ilustriscore.core.model.ViewModelBaseState
import com.silent.ilustriscore.core.utilities.delayedFunction

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    lateinit var mainBinding: ActivityMainBinding
    private val viewModel by lazy {
        IlustrisViewModel(application)
    }
    private val loginResultAct = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        onLoginResult(result)
    }

    protected fun launchLogin(appLogo: Int, theme: Int, loginProviders: List<AuthUI.IdpConfig>) {
        loginResultAct.launch(
            AuthUI.getInstance().createSignInIntentBuilder()
                .setLogo(appLogo)
                .setAvailableProviders(loginProviders)
                .setTheme(theme)
                .build()
        )
    }

    fun onLoginResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == Activity.RESULT_OK && result.idpResponse != null) {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        mainBinding.getInTouch.setOnClickListener {
            ContactDialog(this).buildDialog()
        }
        observeViewModel()
        viewModel.getAllData()
    }

    private fun showNewAppDialog() {
        NewAppDialog(this) { newApp ->
            viewModel.newAppDTO = newApp
            //viewModel.uploadFile(newApp.appIcon)
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
                    setupRecyclerview(it.dataList as appList)
                }
                is ViewModelBaseState.DataSavedState -> {
                    getView().showSnackBar("App salvo com sucesso")
                    viewModel.getAllData()
                }
                is ViewModelBaseState.DataUpdateState -> getView().showSnackBar("App atualizado com sucesso")

                is ViewModelBaseState.ErrorState -> {
                    if (it.dataException.code == ErrorType.AUTH) login()
                    getView().showSnackBar(
                        backColor = ContextCompat.getColor(this, ERROR_COLOR),
                        message = "Ocorreu um erro inesperado ${it.dataException.code.message}"
                    )
                }

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

                else -> {}
            }
        }
    }

    private fun setupRecyclerview(dataList: appList) {

        val listApp = dataList
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
        launchLogin(R.mipmap.ic_launcher, R.style.Theme_Ilustris, providers)
    }

}