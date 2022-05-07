package com.example.studentpal.model.remote
import android.app.Activity
import android.util.Log
import com.example.studentpal.common.Constants
import com.example.studentpal.model.entities.User
import com.example.studentpal.view.friends.FindFriends
import com.example.studentpal.view.friends.ViewFriendProfile
import com.example.studentpal.view.messages.ChatLogActivity
import com.example.studentpal.view.messages.LatestMessagesActivity
import com.example.studentpal.view.registration.SignUpActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*


/** For hosting the Database code, Kotlin object is used.
 *  This makes our Firebase service a singleton, making only a single instance of this service available.
 *  This provides easy access to functions defined here from anywhere in our code.
 */
object UserDatabase {
    private const val TAG = "UserDatabase"
    private val db = FirebaseFirestore.getInstance().collection(Constants.USERS)

    fun fetchCurrentUser(activity: Activity) {
        //retrieves currently signed in users id
        val uid = FirebaseAuth.getInstance().uid
        //A reference to currently signed in user in Firestore
        val ref = db.document(uid.toString())

        //listener listens to modifications made to currently signed in users document
        ref.addSnapshotListener(object : EventListener<DocumentSnapshot> {
            override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.")
                    return
                }
                if (value != null) {
                    when (activity) {
                        is LatestMessagesActivity -> {
                            LatestMessagesActivity.currentUser = value.toObject(User::class.java)
                        }
                        is ViewFriendProfile -> {
                            ViewFriendProfile.currentUser = value.toObject(User::class.java)
                        }
                        is ChatLogActivity -> {
                            ChatLogActivity.currentUser = value.toObject(User::class.java)

                        }
                    }
                }
            }

        })
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
}