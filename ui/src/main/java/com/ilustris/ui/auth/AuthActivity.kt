package com.ilustris.ui.auth

import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult

abstract class AuthActivity : AppCompatActivity() {

    abstract fun onLoginResult(result: FirebaseAuthUIAuthenticationResult)
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

}