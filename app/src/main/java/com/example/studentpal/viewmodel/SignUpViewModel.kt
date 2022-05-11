package com.example.studentpal.viewmodel

import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.remote.UsersDatabase.isUsernameUnique
import com.example.studentpal.model.remote.UsersDatabase.registerUser
import com.example.studentpal.view.registration.SignUpActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class SignUpViewModel : ViewModel(){

    // My Code: This code will retrieve the current date and get the date the user first sign up to StudentPal
    private fun getCurrentDate(): String {
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat.getDateInstance()

        return formatter.format(date)
    }

    // method responsible for authenticating users and ensuring validation form is entered correctly
    suspend fun authenticateUser(activity: SignUpActivity, name: String, email: String, password: String, username: String) {
        val mAuth = FirebaseAuth.getInstance()
        if (validateForm(activity,name, email, password, username)) {
            // shifting execution of the block
            withContext(Dispatchers.Main) {
                activity.showProgressDialog("Please Wait")
            }
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val firebaseUser: FirebaseUser = it.result!!.user!!
                            val registeredEmail = firebaseUser.email!!
                            val dateJoined = getCurrentDate()
                            //User object constructed to be stored in Firestore
                            val user = User(firebaseUser.uid, name, registeredEmail, dateJoined, username = username)
                            //Verification email will be sent to the sign up email
                            sendVerificationEmail(activity,firebaseUser, user)
                        } else {
                            //if a user with the same credentials already exists, registration will fail
                            Toast.makeText(activity, "Registration failed", Toast.LENGTH_LONG).show()
                            activity.hideProgressDialog()
                        }
                    }

            }

    }

    //method responsible for sending verification email to the FirebaseUser trying to sign up
    private fun sendVerificationEmail(
        activity: SignUpActivity,fUser: FirebaseUser, user: User) {
        fUser.sendEmailVerification().addOnSuccessListener {
            Toast.makeText(activity,
                "Email verification link sent to ${fUser.email}",
                Toast.LENGTH_LONG
            ).show()
            //creates a document in Firestore populating the fields with user details
            registerUser(activity, user)
        }.addOnFailureListener {
            Log.e(javaClass.simpleName, "error sending verification link")
            Toast.makeText(activity, "Could not send email verification link", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun validateForm(activity: SignUpActivity, name: String, email: String, password: String, username: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                activity.showErrorSnackBar("Please enter a name")
                false
            }
            invalidUsername(activity,username) -> {
                false
            }
            invalidEmail(activity,email) -> {
                false
            }
            invalidPassword(activity,password) -> {
                false
            }

            else -> {
                return true
            }
        }
    }

    /**
     * This method checks if the entered username is invalid
     *
     * @return True if no username is entered or if username is not unique
     */
    private suspend fun invalidUsername(
        activity: SignUpActivity,
        username: String): Boolean {
        if (username.isEmpty()) {
            activity.showErrorSnackBar("Please enter a Username")
            return true
        }
        /* Makes a call to the Users database
         * Ensures username is unique and does not exist
         */
        if (!isUsernameUnique(username)) {
            activity.showErrorSnackBar("Username already taken")
            return true
        }
        return false
    }

    private fun invalidPassword(
        activity: SignUpActivity, password: String): Boolean {
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

    private fun invalidEmail(activity: SignUpActivity,email: String): Boolean {
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