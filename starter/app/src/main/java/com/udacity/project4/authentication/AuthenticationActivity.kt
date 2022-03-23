package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.udacity.project4.R
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityAuthenticationBinding

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityAuthenticationBinding.inflate(LayoutInflater.from(this))
        setContentView(viewBinding.root)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            startActivity(Intent(this, RemindersActivity::class.java))
            finish()
        } else {
            viewBinding.welcomeText.visibility = View.VISIBLE
            viewBinding.actionLogin.visibility = View.VISIBLE
            viewBinding.progressLoader.visibility = View.GONE
        }

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        viewBinding.actionLogin.setOnClickListener {
            signInLauncher.launch(
                AuthUI.getInstance().createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(providers)
                    .setLogo(R.drawable.map)
                    .setTheme(R.style.LoginScreenTheme)
                    .build()
            )
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        //val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            startActivity(Intent(this, RemindersActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Login Failed, Please try again!", Toast.LENGTH_SHORT).show()
        }
    }

}
