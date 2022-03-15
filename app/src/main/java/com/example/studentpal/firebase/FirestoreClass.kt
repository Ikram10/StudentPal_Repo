package com.example.studentpal.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.studentpal.activities.MainActivity
import com.example.studentpal.activities.MyProfileActivity
import com.example.studentpal.activities.SignInActivity
import com.example.studentpal.activities.SignUpActivity
import com.example.studentpal.models.User
import com.example.studentpal.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.lang.StringBuilder

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun updateUserProfileData (activity: MyProfileActivity, userHashMap: HashMap<String, Any>){
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).update(userHashMap).addOnSuccessListener {
            Log.i(activity.javaClass.simpleName, "Profile data updated successfully")
            Toast.makeText(activity, "Profile updated successfully", Toast.LENGTH_LONG).show()
            activity.profileUpdateSuccess()

        }.addOnFailureListener{

            activity.hideProgressDialog()

            Log.e(activity.javaClass.simpleName,
            "Error while creating a board.", it)

            Toast.makeText(activity, "Error updating profile", Toast.LENGTH_LONG).show()
        }

    }

    fun loadUserData(activity: Activity) {
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).get().addOnSuccessListener {
            //retrieves information about the user stored in the database
            val loggedInUser = it.toObject(User::class.java)

            when(activity) {
                is SignInActivity -> {
                    if (loggedInUser != null) {
                        activity.signInSuccess(loggedInUser)
                    }
                }
                is MainActivity -> {
                    if (loggedInUser != null) {
                        activity.updateNavigationUserDetails(loggedInUser)
                    }
                }
                is MyProfileActivity -> {
                    if (loggedInUser != null) {
                        activity.setUserDataInUI(loggedInUser)
                    }
                }
            }


        }.addOnFailureListener{
                e ->
            when(activity) {
                is SignInActivity -> {

                        activity.hideProgressDialog()

                }
                is MainActivity -> {
                    activity.hideProgressDialog()
                    }
                }
            }

            Log.e(activity.javaClass.simpleName, "Error writing document")
        }



    fun registerUser(activity : SignUpActivity, userInfo : User){
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).set(userInfo,
            SetOptions.merge()).addOnSuccessListener {
            activity.userRegisteredSuccess()
        }.addOnFailureListener {
            e ->
            Log.e(activity.javaClass.simpleName, "Error registering user")
        }
    }

    fun getCurrentUserId() : String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""

        if(currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }



}