package com.ilustris.ui.auth


import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth


class LoginHelper {

    companion object {
        fun signIn(
            activity: AppCompatActivity,
            loginProviders: List<AuthUI.IdpConfig>,
            theme: Int,
            appLogo: Int,
            resultCodeCallback: (Int) -> Unit
        ) {

            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser == null) {
                val resultActivity = activity.registerForActivityResult(
                    FirebaseAuthUIActivityResultContract()
                ) { result ->
                    var resultCode = result.resultCode
                    if (result.idpResponse == null) {
                        resultCode = Activity.RESULT_CANCELED
                    }
                    resultCodeCallback(resultCode)
                }
                resultActivity.launch(
                    AuthUI.getInstance().createSignInIntentBuilder()
                        .setLogo(appLogo)
                        .setAvailableProviders(loginProviders)
                        .setTheme(theme)
                        .build()
                )

            }
        }
    }

}