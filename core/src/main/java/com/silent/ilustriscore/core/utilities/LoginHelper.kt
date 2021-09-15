package com.silent.ilustriscore.core.utilities

import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.silent.ilustriscore.R

class LoginHelper {

    companion object {
        fun signIn(
            activity: AppCompatActivity,
            loginProviders: List<AuthUI.IdpConfig>,
            theme: Int,
            appLogo: Int,
        ) {

            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser == null) {
                activity.startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder()
                        .setLogo(appLogo)
                        .setAvailableProviders(loginProviders)
                        .setTheme(theme)
                        .build(), RC_SIGN_IN
                )
            }
        }
    }

}