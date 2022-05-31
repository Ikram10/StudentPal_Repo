package com.example.studentpal.model.remote

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.studentpal.common.Constants
import com.example.studentpal.model.entities.User
import com.example.studentpal.view.events.AssignFriendsActivity
import com.example.studentpal.view.events.EditEventActivity
import com.example.studentpal.view.events.MainActivity
import com.example.studentpal.view.friends.FindFriends
import com.example.studentpal.view.profile.MyProfileActivity
import com.example.studentpal.view.registration.SignInActivity
import com.example.studentpal.view.registration.SignUpActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * This object contains all the functionalities responsible for retrieving and storing data
 * in the users collection in Firestore.
 *
 * The author implemented an object to create a singleton of the database, that can be accessed
 * anywhere in the code
 *
 * [Adapted ]: Parts of this code in this object was adapted from Denis Panjuta's code as it taught
 * the author how to integrate firestore and store and retrieve data into collections
 *
 * @see com.example.studentpal.common.References
 */

object UsersDatabase {
    private const val TAG = "UsersDatabase"

    // users collection in firestore
    private val db = FirebaseFirestore.getInstance().collection(Constants.USERS)

    /**
     * Method retrieves the Firestore document of the current user and
     * loads the document information into the activity
     *
     * @param loadEventsList will only load the events list for the current user
     * if the boolean is true
     */
    fun loadUserData(activity: Activity, loadEventsList: Boolean = false) {
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
                            activity.updateNavigationUserDetails(loggedInUser, loadEventsList)
                        }
                    }
                    is MyProfileActivity -> {
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

    /**
     * [My Code]: Method checks users collection in firestore to see if Username is already taken
     *
     * @return True if no user exists with the same username
     */
    suspend fun isUsernameUnique(username: String) : Boolean {
        return try {
            db
                .whereEqualTo(Constants.USERNAME, username)
                .get()
                .await()
                .documents
                .size == 0
        } catch (e : Exception) {
            Log.e(TAG, e.message, e)
            false
        }
    }

    /**
     * [My Code ]:Retrieves the current user document and converts it to to a User object
     * @return the user being fetched
     */
    suspend fun  fetchCurrentUser(): User? {
            //A reference to currently signed in user in Firestore document
            val docRef = db.document(getCurrentUserId())
            return try {
                // converts the document to a user object and returns it
            docRef.get().await().toObject(User::class.java)

            } catch (e: Exception) {

            Log.e(TAG, "Error getting user details", e)
            null
        }
    }

    /**
     * [My code ]: Retrieves all users apart from current user from the database
     */
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
                        // When a new user is added to the to the database...
                        if (dc.type == DocumentChange.Type.ADDED) {
                            //add the new user to the user list
                            val user = dc.document.toObject(User::class.java)
                            userList.add(user)
                        }
                    }
                    // refreshes the recyclerview by adding the new user item
                    activity.setUpUsersList(userList)
                }
            })
    }

    /**
     * Creates a new user entry in cloud Firestore.
     * Only one user document per registered user id will be permitted
     *
     * @param userInfo contains the user details that will be stored in Database
     */
    fun registerUser(activity: SignUpActivity, userInfo: User) {
        // Ensures only one user account is created in Firestore for each User id
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

    /**
     * Method returns the the user id of the currently logged in user
     */
    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""

        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }

    /**
     * Method updates the users profile data stored in Firestore
     */
    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {
        db.document(getCurrentUserId()).update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile data updated successfully")
                when (activity) {
                    is MainActivity -> {
                        //updates the fcm token in the database
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfileActivity -> {
                        // Confirms profile data has been updated successfully
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

    /**
     * [My Code ]: Method fetches a list of users from firestore
     * @param userStringList a list of user id's of the users being fetched
     * @return the list of users
     */
    suspend fun fetchUsersById(userStringList: List<String>) : List<User> {
        return try {
            val userList = arrayListOf<User>()
            for (item in userStringList) {
                db
                    .whereEqualTo(Constants.ID, item)
                    .get()
                    .await()
                    .documents
                    .mapNotNull {
                        //converts each object retrieved into a User object
                        val user = it.toObject(User::class.java)
                        //add the user object to user list
                        userList.add(user!!)
                    }
            }
            //returns the user list
             userList
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
            // returns empty list
            emptyList()
        }
    }

    /**
     * [My Code ]: Method increments the user's friends count and the friend's friend count by 1
     */
    fun incrementFriendsCount(user: User, friend: User) {
        db
            .document(user.id)
            .update(Constants.NUMBER_FRIENDS, FieldValue.increment(1))
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("Increment Success", "${user.name} friend count = ${user.numFriends}")
                } else {
                    Log.d("Increment Failed", "${user.name} friend count = ${user.numFriends}")
                }
            }
        db
            .document(friend.id)
            .update(Constants.NUMBER_FRIENDS, FieldValue.increment(1)).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(
                        "Increment Success",
                        "${friend.name} friend count = ${friend.numFriends}"
                    )
                } else {
                    Log.d(
                        "Increment Failed",
                        "${friend.name} friend count = ${friend.numFriends}"
                    )
                }
            }
    }
    /**
     * [My Code ]: Method decrements the user's friends count and the friend's friend count by 1
     */
    fun decrementFriendsCount(user: User, friend: User) {
        // retrieve user's document
       db
           .document(user.id)
            .update(Constants.NUMBER_FRIENDS, FieldValue.increment(-1)).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("Decrement Success", "${user.name} friend count = ${user.numFriends}")
                } else {
                    Log.d("Decrement Failed", "${user.name} friend count = ${user.numFriends}")
                }
            }

           //retrieve friend's document
        db
            .document(friend.id)
            .update(Constants.NUMBER_FRIENDS, FieldValue.increment(-1)).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(
                        "Decrement Success",
                        "${friend.name} friend count = ${friend.numFriends}"
                    )
                } else {
                    Log.d(
                        "Decrement Failed",
                        "${friend.name} friend count = ${friend.numFriends}"
                    )
                }
            }
    }

    /**
     *  [My Code ]: Retrieves the event host from database
     *  @param userId the user id of the event host to retrieve their document
     *  @return the event host
     */
   suspend fun getEventHost(userId: String) : User? {
        return try {
            db
                .document(userId)
                .get()
                .await()
                .toObject(User::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting host details", e)
            null
        }
    }

    /**
     * Method retrieves friend details by querying for their username in firestore
     */
    fun getFriendDetails(activity: AssignFriendsActivity, username: String) {
        db
            .whereEqualTo(Constants.USERNAME, username)
            .get()
            .addOnSuccessListener {
                if (it.documents.size > 0) {
                    /* Username's are unique
                    *  If a username exists in Firestore there can only be one (one username per user)
                    */
                    val user = it.documents[0].toObject(User::class.java)
                    activity.friendDetails(user!!)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No friend found with the username provided")
                }
            }
    }

    /**
     * Method retrieves all the users assigned to a specific event
     * @param assignedTo the list of user id's of the users assigned to the event
     */
    fun getAssignedFriendsListDetails(activity: Activity, assignedTo: ArrayList<String>) {
        db
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, it.documents.toString())
                val usersList: ArrayList<User> = ArrayList()

                for (doc in it.documents) {
                    // Convert all the document snapshots to the object using the user data model class.
                    val user = doc.toObject(User::class.java)
                    // add the user object to the user list
                    usersList.add(user!!)
                }

                if (activity is AssignFriendsActivity) {
                    activity.setUpAssignedList(usersList)

                } else
                    if (activity is EditEventActivity)
                        activity.setUpAssignedMembersList(usersList)

            }.addOnFailureListener {
                if (activity is AssignFriendsActivity) {
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName, "error while getting friends list", it)
                } else
                    if (activity is EditEventActivity) {
                        activity.hideProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "error while getting friends list",
                            it
                        )
                    }
            }
    }

    /**
     * [My Code ]: Method responsible for deleting the users firestore and authentication data and makes a call
     * to delete all the events users has created
     */
    fun deleteUserData(userId : String) {
        db
            .document(userId)
            .delete() // Deletes users firestore user document
            .addOnCompleteListener {
                if (it.isSuccessful){
                    Log.d(TAG, "User document deleted")
                } else {
                    Log.d(TAG, "User document failed to delete", it.exception)
                }
            }
    }

}