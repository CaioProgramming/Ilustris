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
            theme: Int
        ) {

            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser == null) {
                activity.startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder()
                        .setLogo(R.mipmap.ic_launcher)
                        .setAvailableProviders(loginProviders)
                        .setTheme(theme)
                        .build(), RC_SIGN_IN
                )
            }
        }
    }

}