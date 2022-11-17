package com.example.officetracker.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.officetracker.databinding.ActivitySignUpBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivitySignUpBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mAuth = FirebaseAuth.getInstance()
        mBinding.alreadySignedIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        mBinding.signUpBtn.setOnClickListener {
            val name = mBinding.name.text.toString()
            val email = mBinding.email.text.toString()
            val password = mBinding.password.text.toString()
            val confirmPassword = mBinding.confirmPassword.text.toString()
            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    mBinding.progress.visibility = View.VISIBLE
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful) {
                            mBinding.progress.visibility = View.GONE
                            val intent = Intent(this, SignInActivity::class.java)
                            intent.putExtra("email",mBinding.email.text.toString())
                            startActivity(intent)
                        } else {
                            mBinding.progress.visibility = View.GONE
                            Snackbar.make(
                                mBinding.root,
                                it.exception.toString(),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    Snackbar.make(
                        mBinding.root,
                        "password and confirm password should be same",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } else {
                Snackbar.make(mBinding.root, "please fill all fields", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}