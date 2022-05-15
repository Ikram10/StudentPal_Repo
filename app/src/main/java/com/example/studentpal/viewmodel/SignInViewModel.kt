package com.example.studentpal.viewmodel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import com.example.studentpal.view.events.MainActivity
import com.example.studentpal.view.registration.SignInActivity
import com.google.firebase.auth.FirebaseAuth
/**
 * This activity is responsible for executing [SignInActivity] business logic
 *
 * The code displayed was adapted from Denis Panjuta's Trello clone (see references file).
 * However alterations were made to the code to suit the project's requirements. For instance,
 * Kotlin Coroutines were embedded to allow the author to write asynchronous code and
 * structural changes were made to implement the MVVM design pattern
 * which required architectural principles to be implemented.
 *
 * All code that was created by the author is labeled with [My Code].

 * @see[com.example.studentpal.common.References]
 *
 *
 */
class SignInViewModel : ViewModel() {

    lateinit var auth: FirebaseAuth

    /**
     * Method handles Sign-in using the email and password entered
     */
    fun signInRegisteredUser(activity: SignInActivity, email: String, password: String ) {
        auth = FirebaseAuth.getInstance()
        if (validateForm(activity,email, password)) {
            activity.showProgressDialog("Please Wait")
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity) {
                    activity.hideProgressDialog()
                    if (it.isSuccessful) {
                        val user = auth.currentUser
                        // Checks if current user has verified their email
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

    /**
     * A method that validates  all Sign-In form entries
     */
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

    /**
     * A method that ensures a password has been entered
     */
    private fun invalidPassword(activity: SignInActivity,password: String): Boolean {
        if (password.isEmpty()) {
            activity.showErrorSnackBar("Please enter a password")
            return true
        } else
            if (password.length < 6) {
                activity.showErrorSnackBar("Weak password: Use at least 6 characters")
                return true
            }
        return false
    }

    /**
     * Ensures an email is provided and in the correct format
     */
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

    /**
     * Checks if email is in the correct format
     */
    private fun isEmailFormatValid(email: CharSequence?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email!!).matches()
    }
}