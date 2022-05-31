package com.example.studentpal.model.remote

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.example.studentpal.common.Constants
import com.example.studentpal.model.entities.FriendRequest
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.fcm.notification.NotificationData
import com.example.studentpal.model.fcm.notification.PushNotification
import com.example.studentpal.model.remote.UsersDatabase.fetchUsersById
import com.example.studentpal.model.remote.UsersDatabase.getCurrentUserId
import com.example.studentpal.model.remote.UsersDatabase.incrementFriendsCount
import com.example.studentpal.view.friends.FriendProfile
import com.example.studentpal.viewmodel.FriendsProfileViewModel
import com.example.studentpal.viewmodel.FriendsProfileViewModel.Companion.FRIEND
import com.example.studentpal.viewmodel.FriendsProfileViewModel.Companion.currentState
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * This object contains all the functionalities responsible for retrieving and storing data
 * in the friendship and friend-requests database.
 *
 * The author implemented an object to create a singleton of the database, that can be accessed
 * throughout the code
 *
 * [My Code ]: The entire code in this object was implemented by the author
 */
object FriendshipsDatabase {

    private const val TAG = "FriendshipsDatabase"

    // Friendship firestore collection
    private val db = FirebaseFirestore.getInstance().collection(Constants.FRIENDSHIPS)

    // Friend-Request firestore collection
    private val requestDB = FirebaseFirestore.getInstance().collection(Constants.FRIEND_REQUEST)

    /**
     * Method retrieves all the users that have sent the current user a friend request.
     *
     * This was implemented to display all the friend requests received by a user in a recyclerview.
     * The author implemented this as a Suspend function to not block the main thread, as the network
     * request proceeds.
     *
     * @return a list of [User]
     *
     * @see [com.example.studentpal.view.friends.RequestsActivity]
     * @see fetchUsersById
     */
    suspend fun getRequests(): List<User> {
        val requestsList =
            requestDB
                .whereEqualTo(Constants.RECEIVER, getCurrentUserId())
                .get()
                .await()
                .documents
                .mapNotNull {
                    it.data?.get(Constants.SENDER) as String
                }
        return fetchUsersById(requestsList)
    }

    /**
     * Method retrieves all the friends of the user from firestore
     *
     * This was implemented to display all the friends of the user in a recyclerview.
     * The author implemented this as a suspend function to not block the main thread, as the network
     * request proceeds.
     *
     * @return a list of [User]
     *
     * @see [com.example.studentpal.view.friends.FriendsActivity]
     * @see fetchUsersById
     */
    suspend fun getFriendsList(): List<User> {
        //First query for friendShip documents where current user is receiver
        val receiverStringList = db
            .whereEqualTo(
                Constants.RECEIVER,
                getCurrentUserId())
            .get()
            .await()
            .documents
            .mapNotNull {
                //converts every document to a string
                it.data?.get(Constants.SENDER) as String
            }
        val senderStringList = db
            .whereEqualTo(
                Constants.SENDER,
                getCurrentUserId())
            .get()
            .await()
            .documents.mapNotNull {
                //converts every document to a string
                it.data?.get(Constants.RECEIVER) as String
            }
        //combines the two strings list
        val friendsStringList = senderStringList + receiverStringList
        return fetchUsersById(friendsStringList)
    }

    /**
     * Method stores the friend request information in firestore
     *
     */
    fun storeFriendRequest(
        activity: FriendProfile,
        friendRequestData: FriendRequest,
        friendDetails: LiveData<User>,
        currentUser: LiveData<User>
    ) {
        requestDB.document()
            .set(friendRequestData, SetOptions.merge())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(
                        activity,
                        "You have sent a friend Request to ${friendDetails.value?.name}",
                        Toast.LENGTH_LONG
                    ).show()

                    // update the current state to sent request
                    currentState.value = FriendsProfileViewModel.AccountStates.SENT_REQUEST
                    val notification = NotificationData(
                        "Friend Request",
                        "${currentUser.value!!.name} sent a friend request"
                    )

                    // notifies the recipient of the friend request
                    activity.sendNotification(
                        PushNotification(
                            notification,
                            friendDetails.value!!.fcmToken
                        )
                    )
                } else {
                    Log.e(javaClass.simpleName, it.exception.toString())
                    Toast.makeText(
                        activity,
                        "Error sending friend request to ${friendDetails.value?.name}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    /**
     *  Deletes the Friend request document from firestore when current user is the sender
     */
    fun deleteSenderFriendRequest(activity: FriendProfile, friendUser: User?) {
        requestDB.whereEqualTo(Constants.SENDER, getCurrentUserId())
            .whereEqualTo(Constants.RECEIVER, friendUser!!.id)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    //change profile state back to default
                    currentState.value = FriendsProfileViewModel.AccountStates.DEFAULT
                    //delete friend request document
                    for (doc in it.result.documents) {
                        doc.reference.delete()
                        Toast.makeText(
                            activity,
                            "You have cancelled friend request",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                } else {
                    Log.e(javaClass.simpleName, it.exception.toString())
                    Toast.makeText(
                        activity,
                        "Error cancelling friend request",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    /**
     *  Deletes the Friend request document from firestore when current user is the receiver
     */
    fun deleteReceiverFriendRequest(activity: Activity, friendUser: User?) {
        requestDB.whereEqualTo(Constants.SENDER, friendUser!!.id)
            .whereEqualTo(Constants.RECEIVER, getCurrentUserId())
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    //change profile state back to default
                    currentState.value = FriendsProfileViewModel.AccountStates.DEFAULT
                    //delete friend request document
                    for (doc in it.result.documents) {
                        doc.reference.delete()
                        Toast.makeText(
                            activity,
                            "You have declined friend request from ${friendUser.name}",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                } else {
                    Log.e(javaClass.simpleName, it.exception.toString())
                    Toast.makeText(
                        activity,
                        "Error sending friend request to ${friendUser.name}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    /**
     * Creates a friendship document in firestore between the two users
     */
    fun createFriendship(
        activity:
        Activity,
        friend: User,
        currentUser: User) {
        /*
         * Retrieve the friend request document between the two users
         * Delete the document, because users are now friends
         */
        requestDB
            .whereEqualTo(Constants.SENDER, friend.id)
            .whereEqualTo(Constants.RECEIVER, getCurrentUserId()).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (doc in task.result.documents) {
                        doc
                            .reference
                            .delete()
                            .addOnSuccessListener {
                                // Create HASHMAP to that stores the Friendship information
                                val hashMap = HashMap<String, Any>()
                                hashMap[Constants.STATUS] = FRIEND
                                hashMap[Constants.SENDER] = friend.id
                                hashMap[Constants.RECEIVER] = getCurrentUserId()
                                db.document()
                                    .set(hashMap, SetOptions.merge())
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            //Increments number of friends by 1
                                            incrementFriendsCount(
                                                currentUser,
                                                friend
                                            )
                                            Toast.makeText(
                                                activity,
                                                "You added a Friend",
                                                Toast.LENGTH_LONG)
                                                .show()
                                            currentState.value =
                                                FriendsProfileViewModel.AccountStates.FRIEND
                                        }
                                    }
                            }
                    }
                }
            }
    }

    /**
     * [My Code ]: Method searches Firestore to check if the current user and friend have a Friendship
     * document and updates states of the account appropriately.
     *
     * @see [FriendsProfileViewModel]
     */
    fun searchFriendship(friendDetails: User?) {
        /* First searches database to check is the current user and other user are Friends
         * where the current user was the friend request sender
         */
        db
            .whereEqualTo(Constants.SENDER, getCurrentUserId())
            .whereEqualTo(Constants.RECEIVER, friendDetails?.id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    for (dc: DocumentChange in snapshot.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                // Set the current state of profile to Friend
                                currentState.value = FriendsProfileViewModel.AccountStates.FRIEND
                                return@addSnapshotListener
                            }
                            else -> {}
                        }
                    }
                }
            }

        /* Searches database to check is the current user and other user are Friends
         * where the current user was the friend request receiver
         */
        db
            .whereEqualTo(Constants.SENDER, friendDetails?.id)
            .whereEqualTo(Constants.RECEIVER, getCurrentUserId())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    for (dc: DocumentChange in snapshot.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                // Set the current state of profile to Friend
                                currentState.value = FriendsProfileViewModel.AccountStates.FRIEND
                                return@addSnapshotListener
                            }
                            else -> {}
                        }
                    }
                }
            }

        /* Query friend request documents where current user is request sender
         * And friend user is request receiver
         */
        requestDB
            .whereEqualTo(Constants.SENDER, getCurrentUserId())
            .whereEqualTo(Constants.RECEIVER, friendDetails?.id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    for (dc: DocumentChange in snapshot.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                if (dc.document[FriendsProfileViewModel.STATUS] == FriendsProfileViewModel.PENDING) {
                                    /* Set current state of profile to "SENT_REQUEST"
                                     * If a friend-request document exists between user and friend
                                     */
                                    currentState.value =
                                        FriendsProfileViewModel.AccountStates.SENT_REQUEST
                                    return@addSnapshotListener

                                }
                                if (dc.document[FriendsProfileViewModel.STATUS] == FriendsProfileViewModel.DECLINE) {
                                    /* Set current state of profile to "DECLINED_REQUEST"
                                     * If a friend-request had been declined
                                     */
                                    currentState.value =
                                        FriendsProfileViewModel.AccountStates.DECLINED_REQUEST
                                    return@addSnapshotListener
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }

        /* Query friend request documents where current user is request receiver
         * And friend user is request sender
         */
        requestDB
            .whereEqualTo(Constants.SENDER, friendDetails?.id)
            .whereEqualTo(Constants.RECEIVER, getCurrentUserId())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    for (dc: DocumentChange in snapshot.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                /* Set current state of profile to "PENDING"
                                 * If a friend-request is waiting to be accepted or declined
                                 */
                                if (dc.document[FriendsProfileViewModel.STATUS] == FriendsProfileViewModel.PENDING) {
                                    currentState.value =
                                        FriendsProfileViewModel.AccountStates.RECEIVED_REQUEST
                                    return@addSnapshotListener

                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
    }

    /**
     * [My Code ]: Method deletes Friendship document between two users from firestore
     *
     * @param friendDetails The friends account details
     * @param currentUser the current users account details
     */
    fun removeFriendship(
        activity: FriendProfile,
        friendDetails: User?,
        currentUser: User?
    ) {

        // Search database for document where current user is sender
        db
            .whereEqualTo(Constants.SENDER, getCurrentUserId())
            .whereEqualTo(Constants.RECEIVER, friendDetails?.id)
            .get()
            .addOnCompleteListener { task ->
                // Delete document if found
                if (task.isSuccessful) {
                    for (doc in task.result.documents) {
                        doc.reference.delete().addOnCompleteListener {
                            if (it.isSuccessful) {
                                UsersDatabase.decrementFriendsCount(
                                    currentUser!!,
                                    friendDetails!!
                                )
                                Toast.makeText(
                                    activity,
                                    "You unfriended ${friendDetails.name}",
                                    Toast.LENGTH_LONG
                                ).show()
                                // Modify account state to default, because users are not friends anymore
                                currentState.value =
                                    FriendsProfileViewModel.AccountStates.DEFAULT

                            }
                        }
                    }

                }
            }
        // Search database for document where current user is receiver
        db
            .whereEqualTo(Constants.SENDER, friendDetails?.id)
            .whereEqualTo(Constants.RECEIVER, getCurrentUserId())
            .get()
            .addOnCompleteListener { task ->
                // Delete document if found
                if (task.isSuccessful) {
                    for (doc in task.result.documents) {
                        doc.reference
                            .delete()
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    UsersDatabase.decrementFriendsCount(
                                        currentUser!!,
                                        friendDetails!!
                                    )
                                    Toast.makeText(
                                        activity,
                                        "You unfriended ${friendDetails.name}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    // Modify account state to default, because users are not friends anymore
                                    currentState.value =
                                        FriendsProfileViewModel.AccountStates.DEFAULT
                                }
                            }
                    }
                }
            }
    }

    /**
     * [My Code ] :Method retrieves the document storing the friend request and updates the status to decline
     */
    fun declineRequest(
        activity: FriendProfile,
        hashmap: HashMap<String, Any>,
        friendDetails: User?
    ) {
        // Search firestore for friend request document where current user received the friend request.
        requestDB
            .whereEqualTo(Constants.SENDER, friendDetails?.id)
            .whereEqualTo(Constants.RECEIVER, getCurrentUserId())
            .get()
            .addOnCompleteListener { task ->
                for (doc in task.result.documents) {
                    doc
                        .reference
                        .update(hashmap)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    activity,
                                    "You have declined friend request from ${friendDetails?.name}",
                                    Toast.LENGTH_LONG
                                ).show()
                                // change account stated to decline request
                                currentState.value =
                                    FriendsProfileViewModel.AccountStates.DECLINED_REQUEST
                            }
                        }
                }
            }
    }
}




