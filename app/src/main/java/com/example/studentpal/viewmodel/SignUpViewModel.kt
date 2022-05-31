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

/**
 * This class is responsible for executing [SignUpActivity] business logic
 *
 * The code displayed was adapted from Denis Panjuta's Trello clone (see references file).
 * However alterations were made to the code to suit the project's requirements.
 *
 * For instance, Kotlin Coroutines were embedded to allow the author to write asynchronous code and
 * structural changes were made to implement the MVVM design pattern
 * which required architectural principles to be implemented.
 *
 * An email verification feature was also implemented with the help of the Firebase documentation.
 * [Firebase Documentation](https://firebase.google.com/docs/auth/admin/email-action-links)
 *
 * All code that was created by the author is labeled with [My Code].
 *
 * @see[com.example.studentpal.common.References]
 * @see[signUpUser]
 * @see [sendVerificationEmail]
 */

class SignUpViewModel : ViewModel(){

    /**
     * Method retrieves the current date
     */
    private fun getCurrentDate(): String {
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat.getDateInstance()
        return formatter.format(date)
    }

    /**
     * Method ensures Sign-Up form details are valid before authenticating users.
     *
     * The author revised this method by adding a unique username field
     * @see isUsernameUnique
     * @see invalidUsername
     *
     */
    suspend fun signUpUser(
        activity: SignUpActivity,
        name: String, email: String,
        password: String,
        username: String) {
        val mAuth = FirebaseAuth.getInstance()
        if (validateForm(activity,name, email, password, username)) {
            // Shifting execution of the code to the main thread
            withContext(Dispatchers.Main) {
                activity.showProgressDialog("Please Wait")
            }
                mAuth
                    .createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val firebaseUser: FirebaseUser = it.result!!.user!!
                            val registeredEmail = firebaseUser.email!!
                            val dateJoined = getCurrentDate()
                            // User object constructed to be stored in Firestore
                            val user = User(
                                firebaseUser.uid,
                                name,
                                registeredEmail,
                                dateJoined,
                                username = username)
                            // Verification email sent to the provided Sign Up email
                            sendVerificationEmail(activity,firebaseUser, user)
                        } else {
                            Log.e(TAG, "Registration failed", it.exception)
                            // If a user with the same credentials already exists, registration will fail
                            Toast.makeText(activity, "Registration failed", Toast.LENGTH_LONG).show()
                            activity.hideProgressDialog()
                        }
                    }
            }
    }

    /**
     * Method responsible for sending verification email to the FirebaseUser
     */
    private fun sendVerificationEmail(
        activity: SignUpActivity, 
        fUser: FirebaseUser, 
        user: User) {
        fUser
            .sendEmailVerification()
            .addOnSuccessListener {
            Toast.makeText(activity,
                "Email verification link sent to ${fUser.email}",
                Toast.LENGTH_LONG
            ).show()
            // Creates a document in Firestore populating the fields with user details
            registerUser(activity, user)
        }.addOnFailureListener {
            Log.e(javaClass.simpleName, 
                "error sending verification link")
            Toast.makeText(activity, 
                "Could not send email verification link",
                Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Method ensures Sign-Up form information provided is valid
     *
     */
    private suspend fun validateForm(
        activity: SignUpActivity,
        name: String,
        email: String,
        password: String,
        username: String): Boolean {
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
     * This method checks if the entered username is invalid.
     * A call to the database is made to determine if username is unique
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
        if (!isUsernameUnique(username)) {
            activity.showErrorSnackBar("Username already taken")
            return true
        }
        return false
    }

    /**
     * This method checks if the entered password is invalid.
     *
     * @return True if no password is entered or if password is less than 6 characters
     */
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

    /**
     * This method checks if the entered email is invalid.
     *
     * @return True if no email is entered or if email is in the wrong format
     * */
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

    /**
     * Method checks if email is in the correct format
     */
    private fun isEmailFormatValid(email: CharSequence?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email!!).matches()
    }

    companion object {
        const val TAG = "SignUpViewModel"
    }

}