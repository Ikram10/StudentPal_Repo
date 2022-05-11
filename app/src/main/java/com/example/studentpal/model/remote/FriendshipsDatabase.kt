package com.example.studentpal.model.remote

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.example.studentpal.common.Constants
import com.example.studentpal.model.entities.FriendRequest
import com.example.studentpal.model.fcm.NotificationData
import com.example.studentpal.model.fcm.PushNotification
import com.example.studentpal.model.entities.User
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

object FriendshipsDatabase {
    private const val TAG = "FriendshipsDatabase"

    // Friendship firestore collection
    private val db = FirebaseFirestore.getInstance().collection(Constants.FRIENDSHIPS)

    // Friend-Request firestore collection
    private val requestDB = FirebaseFirestore.getInstance().collection(Constants.FRIEND_REQUEST)

    //my code
    suspend fun getRequests(): List<User> {
        val requestsList  =
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

    //my code
    suspend fun getFriendsList(): List<User> {
        //First query for friendShip documents where current user is receiver
        val recieverStringList = db
            .whereEqualTo(Constants.RECEIVER, getCurrentUserId())
            .get()
            .await()
            .documents
            .mapNotNull {
                it.data?.get(Constants.SENDER) as String
            }
        val senderStringList = db
            .whereEqualTo(Constants.SENDER, getCurrentUserId())
            .get()
            .await()
            .documents.mapNotNull {
                it.data?.get(Constants.RECEIVER) as String
            }
        val friendsStringList = senderStringList + recieverStringList

        return fetchUsersById(friendsStringList)
    }

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

                    currentState.value = FriendsProfileViewModel.AccountStates.SENT_REQUEST
                    val notification = NotificationData(
                        "Friend Request",
                        "${currentUser.value!!.name} sent a friend request"
                    )
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

    // Deletes the Friend request document from firestore when current user is the sender
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

    // Deletes the Friend request document from firestore when current user is the receiver
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

    fun createFriendship(activity: Activity, friend: User, currentUser: User) {
        requestDB
            .whereEqualTo(Constants.SENDER, friend.id)
            .whereEqualTo(Constants.RECEIVER, getCurrentUserId()).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (doc in it.result.documents) {
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
                                            Toast.LENGTH_LONG
                                        )
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

    // Searches Firestore to check if the current user and friend have a Friendship document
    fun searchFriendship(friendDetails: User?) {
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
                            }

                            else -> {}
                        }
                    }
                }


            }

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
                                currentState.value = FriendsProfileViewModel.AccountStates.FRIEND
                            }
                            DocumentChange.Type.MODIFIED -> {

                            }
                            DocumentChange.Type.REMOVED -> {
                            }
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

                                }
                                if (dc.document[FriendsProfileViewModel.STATUS] == FriendsProfileViewModel.DECLINE) {
                                    currentState.value =
                                        FriendsProfileViewModel.AccountStates.DECLINED_REQUEST
                                }
                            }
                            DocumentChange.Type.MODIFIED -> {
                            }
                            DocumentChange.Type.REMOVED -> {
                            }
                        }
                    }
                }
            }


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
                                if (dc.document[FriendsProfileViewModel.STATUS] == FriendsProfileViewModel.PENDING) {
                                    currentState.value =
                                        FriendsProfileViewModel.AccountStates.RECEIVED_REQUEST

                                }
                            }
                            DocumentChange.Type.MODIFIED -> {
                            }
                            DocumentChange.Type.REMOVED -> {
                            }
                        }
                    }
                }
            }
    }


    fun removeFriendship(activity: FriendProfile, friendDetails: User?, currentUser: User?) {
        db
            .whereEqualTo(Constants.SENDER, getCurrentUserId())
            .whereEqualTo(Constants.RECEIVER, friendDetails?.id)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (doc in it.result.documents) {
                        doc.reference.delete().addOnCompleteListener {
                            if (it.isSuccessful) {
                                UsersDatabase.decrementFriendsCount(
                                    currentUser,
                                    friendDetails
                                )
                                Toast.makeText(
                                    activity,
                                    "You unfriended ${friendDetails?.name}",
                                    Toast.LENGTH_LONG
                                ).show()
                                currentState.value = FriendsProfileViewModel.AccountStates.DEFAULT

                            }
                        }
                    }

                }
            }

        db
            .whereEqualTo(Constants.SENDER, friendDetails?.id)
            .whereEqualTo(Constants.RECEIVER, getCurrentUserId())
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (doc in it.result.documents) {
                        doc.reference
                            .delete()
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    UsersDatabase.decrementFriendsCount(
                                        currentUser,
                                        friendDetails
                                    )
                                    Toast.makeText(
                                        activity,
                                        "You unfriended ${friendDetails?.name}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    currentState.value =
                                        FriendsProfileViewModel.AccountStates.DEFAULT
                                }
                            }
                    }

                }
            }
    }

    fun declineRequest(activity: FriendProfile, hashmap: HashMap<String, Any>, friendDetails: User?) {

        requestDB
            .whereEqualTo(Constants.SENDER, friendDetails?.id)
            .whereEqualTo(Constants.RECEIVER, getCurrentUserId())
            .get()
            .addOnCompleteListener {
                for (doc in it.result.documents) {
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
                            currentState.value =
                                FriendsProfileViewModel.AccountStates.DECLINED_REQUEST
                        }
                    }
                }
            }
    }
}




