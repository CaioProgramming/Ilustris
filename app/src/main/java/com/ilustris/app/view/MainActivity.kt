package com.ilustris.app.view

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.ilustris.app.R
import com.silent.ilustriscore.core.utilities.LoginHelper
import com.silent.ilustriscore.core.utilities.RC_SIGN_IN
import com.silent.ilustriscore.core.utilities.getView
import com.silent.ilustriscore.core.utilities.showSnackBar

class MainActivity : AppCompatActivity() {
    private val mainActBinder by lazy {
        MainActBinder(
            DataBindingUtil.setContentView(
                this,
                R.layout.activity_main
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        login()
    }

    private fun login() {
        val providers = listOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build()
        )
        if (FirebaseAuth.getInstance().currentUser == null) {
            LoginHelper.signIn(this, providers, R.style.Ilustris_Theme, R.mipmap.ic_launcher)
        } else {
            mainActBinder.initView()
        }
    }

    private fun initializeHome() {
        mainActBinder.initView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                initializeHome()
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