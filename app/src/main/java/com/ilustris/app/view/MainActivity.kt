package com.ilustris.app.view

import android.app.Activity
import android.os.Bundle
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ilustris.animations.fadeIn
import com.ilustris.animations.fadeOut
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
import com.silent.ilustriscore.core.contract.DataError
import com.silent.ilustriscore.core.model.ViewModelBaseState
import com.silent.ilustriscore.core.utilities.delayedFunction

class MainActivity : AppCompatActivity() {

    lateinit var mainBinding: ActivityMainBinding
    private var expanded = true
    private val viewModel by lazy {
        IlustrisViewModel(application)
    }
    private var newAppDialog: NewAppDialog? = null
    private val loginResultAct = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        onLoginResult(result)
    }

    private val pickAppIconResult =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                newAppDialog?.updateIcon(uri)
            } else {
                getView().showSnackBar(
                    "Ocorreu um erro ao selecionar o ícone do app, tente novamente",
                    actionText = "Ok",
                    action = {

                    },
                    backColor = ERROR_COLOR
                )
            }
        }


    private fun launchLogin(appLogo: Int, theme: Int, loginProviders: List<AuthUI.IdpConfig>) {
        loginResultAct.launch(
            AuthUI.getInstance().createSignInIntentBuilder()
                .setLogo(appLogo)
                .setAvailableProviders(loginProviders)
                .setTheme(theme)
                .build()
        )
    }

    private fun onLoginResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == Activity.RESULT_OK && result.idpResponse != null) {
            viewModel.getAllData()
        } else {
            setupError(DataError.Auth)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        mainBinding.getInTouch.setOnClickListener {
            ContactDialog(this).buildDialog()
        }
        observeViewModel()
        observeAppBarOffset()
        viewModel.getAllData()
    }

    private fun showNewAppDialog() {
        newAppDialog = NewAppDialog(this, AppDTO(), {
            pickAppIconResult.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) { newApp ->
            viewModel.saveApp(newApp)
        }.apply {
            buildDialog()
        }
    }

    private fun observeAppBarOffset() {
        mainBinding.appbar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            expanded = verticalOffset == 0
        }
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
                    setupError(it.dataException)
                }

                ViewModelBaseState.LoadCompleteState -> {
                    delayedFunction(3000) {
                        mainBinding.appbar.setExpanded(false, true)
                    }
                }

                ViewModelBaseState.LoadingState -> {
                    delayedFunction(1000) {
                        mainBinding.errorContainer.fadeOut()
                        mainBinding.appbar.setExpanded(true, true)
                    }
                }

                else -> Unit
            }
        }
    }

    private fun setupError(dataException: DataError) {
        mainBinding.run {
            errorMessage.text = dataException.message
            errorButton.text =
                if (dataException == DataError.Auth) "Login" else "Tentar novamente"
            errorButton.setOnClickListener {
                if (dataException == DataError.Auth) login()
                else viewModel.getAllData()
            }
            errorContainer.fadeIn()
            mainBinding.appbar.setExpanded(false, true)
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
        })
        mainBinding.appsRecyclerView.adapter = appsAdapter
        if (expanded) {
            viewModel.updateViewState(ViewModelBaseState.LoadCompleteState)
        }
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