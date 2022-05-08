package com.example.studentpal.model.remote

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.studentpal.common.Constants
import com.example.studentpal.model.entities.User
import com.example.studentpal.view.MainActivity
import com.example.studentpal.view.MyProfileActivity
import com.example.studentpal.view.PostsActivity
import com.example.studentpal.view.friends.FindFriends
import com.example.studentpal.view.registration.SignInActivity
import com.example.studentpal.view.registration.SignUpActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.tasks.await


/** For hosting the Database code, Kotlin object is used.
 *  This makes our Firebase service a singleton, making only a single instance of this service available.
 *  This provides easy access to functions defined here from anywhere in our code.
 */
object UsersDatabase {
    private const val TAG = "UsersDatabase"
    private val db = FirebaseFirestore.getInstance().collection(Constants.USERS)

    /**
     * This function retrieves the Firestore document of the current user by using getCurrentUserId().get()
     * It loads the document information into the activity
     * readBoardsList will only read and load the events for the current user stored in Firestore if the boolean is true
     */
    fun loadUserData(activity: Activity, readBoardsList: Boolean = false) {
        val mFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
        mFireStore.collection(Constants.USERS)
            // The document id is the current user's id
            .document(getCurrentUserId()).get()

            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, it.toString())
                // Here we have received the document snapshot which is converted into the User Data model object.
                val loggedInUser = it.toObject(User::class.java)

                when (activity) {
                    is SignInActivity -> {
                        if (loggedInUser != null) {
                            activity.signInSuccess(loggedInUser)
                        }
                    }
                    is MainActivity -> {
                        if (loggedInUser != null) {
                            activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                        }
                    }
                    is MyProfileActivity -> {
                        if (loggedInUser != null) {
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
                    is PostsActivity -> {
                        if (loggedInUser != null) {
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
                }
            }
            .addOnFailureListener {
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Error while getting logged in users details")
            }
    }

    suspend fun fetchCurrentUser(id: String): User? {
            //A reference to currently signed in user in Firestore document
            val docRef = db.document(id)
            return try {

            docRef.get().await().toObject(User::class.java)

            } catch (e: Exception) {

            Log.e(TAG, "Error getting user details", e)
            null
        }
    }

    // My code: Retrieves all users apart from current user from the database
    fun getAllUsers(activity: FindFriends) {
        db.whereNotEqualTo("id", getCurrentUserId())
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    val userList = arrayListOf<User>()
                    if (error != null) {
                        Log.w("Firestore Error", "Listen failed.")
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val user = dc.document.toObject(User::class.java)
                            userList.add(user)

                        }
                    }
                    activity.setUpUsersList(userList)
                }
            })
    }

    /**
     * This method registers a new user into the cloud Firestore
     * A Users collection is created, which generates a single document for each registered user
     * The users document data is filled using the userInfo parameter
     * Only one user document per registered will be permitted
     */
    fun registerUser(activity: SignUpActivity, userInfo: User) {
        // SetOptions.merge() ensures only one user account is created in Firestore for each User id
        db.document(getCurrentUserId()).set(
            userInfo,
            SetOptions.merge()
        )
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener {
                Log.e(activity.javaClass.simpleName, "Error registering user")
            }
    }

    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""

        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }

    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {
        db.document(getCurrentUserId()).update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile data updated successfully")
                Toast.makeText(activity, "Profile updated successfully", Toast.LENGTH_LONG).show()
                when (activity) {
                    is MainActivity -> {
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfileActivity -> {
                        activity.profileUpdateSuccess()
                    }
                }

            }.addOnFailureListener {

                when (activity) {
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.", it
                )

                Toast.makeText(activity, "Error updating profile", Toast.LENGTH_LONG).show()
            }

    }



}