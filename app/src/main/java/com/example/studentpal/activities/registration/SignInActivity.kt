package com.example.studentpal.activities.registration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.studentpal.R
import com.example.studentpal.activities.BaseActivity
import com.example.studentpal.activities.MainActivity
import com.example.studentpal.databinding.ActivitySignInBinding
import com.example.studentpal.models.User
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : BaseActivity() {


    private lateinit var auth: FirebaseAuth
    private var binding: ActivitySignInBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        auth = FirebaseAuth.getInstance()

        setupActionBar()

        binding?.btnSignIn?.setOnClickListener {
            signInRegisteredUser()
        }

        binding?.btnForgotPassword?.setOnClickListener {
            startActivity(Intent(this, PasswordResetActivity::class.java))
        }

    }


    private fun signInRegisteredUser() {
        val email: String = binding?.etEmailSignIn?.text.toString().trim { it <= ' ' }
        val password: String = binding?.etPasswordSignIn?.text.toString().trim { it <= ' ' }

        if (validateForm(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) {
                    hideProgressDialog()
                    if (it.isSuccessful) {
                        val user = auth.currentUser
                        if (user!!.isEmailVerified) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Sign in", "signInWithEmail: success ")
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Email is not verified, check email",
                                Toast.LENGTH_LONG
                            ).show()
                            user.sendEmailVerification()
                        }

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Sign in", "signInWithEmail:failure", it.exception)
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_LONG
                        ).show()

                    }
                }
        }

    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {
            invalidEmail(email) -> {
                false
            }
            invalidPassword(password) -> {
                false
            }
            else -> {
                return true
            }
        }
    }

    private fun invalidPassword(password: String): Boolean {
        if (password.isEmpty()) {
            showErrorSnackBar(
                "Please enter a password"
            )
            return true
        } else
            if (password.length < 6) {
                showErrorSnackBar(
                    "Weak password: Use at least 6 characters"
                )
                return true
            }
        return false
    }

    private fun invalidEmail(email: String): Boolean {
        if (email.isEmpty()) {
            showErrorSnackBar("Please enter an email")
            return true
        } else
            if (!isEmailFormatValid(email)) {
                showErrorSnackBar("Email is in the wrong format")
                return true
            }
        return false
    }

    //checks if email is in the correct format
    private fun isEmailFormatValid(email: CharSequence?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email!!).matches()
    }


    //activates the back button and implements its functionality
    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarSignInActivity)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding?.toolbarSignInActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun signInSuccess(user: User) {
        hideProgressDialog()
        if (auth.currentUser?.isEmailVerified == true) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            Toast.makeText(
                this,
                "${user.email} is not verified, please check email",
                Toast.LENGTH_LONG
            ).show()
        }

    }

}