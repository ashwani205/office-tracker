package com.example.officetracker.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.officetracker.R
import com.example.officetracker.databinding.ActivitySignInBinding
import com.facebook.*
import com.facebook.CallbackManager.Factory.create
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class SignInActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivitySignInBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mAuth = FirebaseAuth.getInstance()
        mBinding.notHaveAccount.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        mBinding.signInBtn.setOnClickListener {
            val email = mBinding.email.text.toString()
            val password = mBinding.password.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                mBinding.progress.visibility = View.VISIBLE
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        mBinding.progress.visibility = View.GONE
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("email", mBinding.email.text.toString())
                        startActivity(intent)
                    } else {
                        mBinding.progress.visibility = View.GONE
                        Snackbar.make(mBinding.root, it.exception.toString(), Snackbar.LENGTH_LONG)
                            .show()
                    }
                }
            } else {
                Snackbar.make(mBinding.root, "please enter all fields...", Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        // facebook login

        callbackManager = create()
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    // App code
                    handleFacebookAccessToken(result.accessToken)
                }

                override fun onCancel() {
                    // App code
                }

                override fun onError(error: FacebookException) {
                    Snackbar.make(mBinding.root, error.toString(), Snackbar.LENGTH_SHORT).show()
                    if (error is FacebookAuthorizationException) {
                        if (AccessToken.getCurrentAccessToken() != null) {
                            LoginManager.getInstance().logOut();
                        }
                    }
                }
            })
        mBinding.facebookLoginButton.setOnClickListener {
            LoginManager.getInstance()
                .logInWithReadPermissions(this, listOf("public_profile,email"));
        }
        //google sign in
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mBinding.googleSignInBtn.setOnClickListener {
            mBinding.progress.visibility = View.VISIBLE
            val signInIntent = mGoogleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    val account = task.result
                    if (account != null) {
                        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
                        mAuth.signInWithCredential(credentials).addOnCompleteListener {
                            if (it.isSuccessful) {
                                account.displayName?.let { it1 -> account.email?.let { it2 ->
                                } }
                                mBinding.progress.visibility = View.GONE
                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("email", account.email)
                                startActivity(intent)
                            } else {
                                mBinding.progress.visibility = View.GONE
                                Snackbar.make(
                                    mBinding.root,
                                    it.exception.toString(),
                                    Snackbar.LENGTH_LONG
                                )
                                    .show()
                            }
                        }
                    } else {
                        Snackbar.make(
                            mBinding.root,
                            task.exception.toString(),
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }
                }
            }
        }


    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("TAG", "handleFacebookAccessToken:$token")
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(
                    "asasas",
                    mAuth.currentUser?.displayName.toString() + " / ${mAuth.currentUser?.email}/  ${mAuth.currentUser?.phoneNumber} / ${it.result.additionalUserInfo?.username}"
                )
                mAuth.currentUser?.email?.let { it1 ->
                    mAuth.currentUser?.displayName?.let { it2 ->
                    }
                }
                mBinding.progress.visibility = View.GONE
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("email", mAuth.currentUser?.displayName)
                startActivity(intent)
            } else {
                mBinding.progress.visibility = View.GONE
                Snackbar.make(
                    mBinding.root,
                    it.exception.toString(),
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}