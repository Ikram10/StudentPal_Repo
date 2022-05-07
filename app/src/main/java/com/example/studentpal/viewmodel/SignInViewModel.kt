package com.example.studentpal.viewmodel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import com.example.studentpal.view.MainActivity
import com.example.studentpal.view.registration.SignInActivity
import com.google.firebase.auth.FirebaseAuth

class SignInViewModel : ViewModel() {
    lateinit var auth: FirebaseAuth

    fun signInRegisteredUser(activity: SignInActivity, email: String, password: String ) {
        auth = FirebaseAuth.getInstance()
        if (validateForm(activity,email, password)) {
            activity.showProgressDialog("Please Wait")
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity) {
                    activity.hideProgressDialog()
                    if (it.isSuccessful) {
                        val user = auth.currentUser
                        if (user!!.isEmailVerified) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Sign in", "signInWithEmail: success ")
                            startActivity(activity,Intent(activity, MainActivity::class.java), Bundle())
                        } else {
                            Toast.makeText(
                                activity,
                                "Email is not verified, check email",
                                Toast.LENGTH_LONG
                            ).show()
                            user.sendEmailVerification()
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Sign in", "signInWithEmail:failure", it.exception)
                        Toast.makeText(
                            activity, "Authentication failed.",
                            Toast.LENGTH_LONG
                        ).show()

                    }
                }
        }

    }

    private fun validateForm(activity: SignInActivity,email: String, password: String): Boolean {
        return when {
            invalidEmail(activity,email) -> {
                false
            }
            invalidPassword(activity = SignInActivity(),password) -> {
                false
            }
            else -> {
                return true
            }
        }
    }

    private fun invalidPassword(activity: SignInActivity,password: String): Boolean {
        if (password.isEmpty()) {
            activity.showErrorSnackBar(
                "Please enter a password"
            )
            return true
        } else
            if (password.length < 6) {
                activity.showErrorSnackBar(
                    "Weak password: Use at least 6 characters"
                )
                return true
            }
        return false
    }

    private fun invalidEmail(activity: SignInActivity,email: String): Boolean {
        if (email.isEmpty()) {
            activity.showErrorSnackBar("Please enter an email")
            return true
        } else
            if (!isEmailFormatValid(email)) {
                activity.showErrorSnackBar("Email is in the wrong format")
                return true
            }
        return false
    }

    //checks if email is in the correct format
    private fun isEmailFormatValid(email: CharSequence?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email!!).matches()
    }
}