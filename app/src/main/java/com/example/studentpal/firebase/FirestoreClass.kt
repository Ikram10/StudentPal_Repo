package com.example.studentpal.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.studentpal.common.Constants
import com.example.studentpal.model.entities.Event
import com.example.studentpal.model.entities.Post
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.remote.UsersDatabase.getCurrentUserId
import com.example.studentpal.view.MainActivity
import com.example.studentpal.view.PostsActivity
import com.example.studentpal.view.events.AssignFriendsActivity
import com.example.studentpal.view.events.CreateBoardActivity
import com.example.studentpal.view.events.EditEventActivity
import com.example.studentpal.view.events.EventInfoActivity
import com.example.studentpal.view.friends.FriendsActivity
import com.example.studentpal.view.friends.ViewFriendProfile
import com.example.studentpal.view.messages.ChatLogActivity.Companion.TAG
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()


    fun getFriendsList(activity: FriendsActivity) {
        val friendsList = arrayListOf<String>()
        //First query for friend request documents where current user is receiver
        mFireStore
            .collection(Constants.FRIENDSHIPS)
            .whereEqualTo(Constants.RECEIVER, getCurrentUserId())
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    for (dc: DocumentChange in snapshot.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val userID: String? = dc.document
                                .get(Constants.SENDER) as String?
                            friendsList.add(userID!!)
                        }
                    }
                }
            }
        mFireStore
            .collection(Constants.FRIENDSHIPS)
            .whereEqualTo(Constants.SENDER, getCurrentUserId())
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    for (dc: DocumentChange in snapshot.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val userID: String =
                                dc.document.data[Constants.RECEIVER] as String
                            friendsList.add(userID)
                        }
                    }
                }
                activity.setUpUsersListFromId(friendsList)
                //Then query for friend request documents where current user is receive
            }

    }


    //My code
    fun incrementFriendsCount(user: User?, friend: User?) {

        mFireStore.collection(Constants.USERS).document(user!!.id)
            .update(Constants.NUMBER_FRIENDS, FieldValue.increment(1)).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("Increment Success", "${user.name} friend count = ${user.numFriends}")
                } else {
                    Log.d("Increment Failed", "${user.name} friend count = ${user.numFriends}")
                }
            }

        mFireStore.collection(Constants.USERS).document(friend!!.id)
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

    fun decrementFriendsCount(user: User?, friend: User?) {
        mFireStore.collection(Constants.USERS).document(user!!.id)
            .update(Constants.NUMBER_FRIENDS, FieldValue.increment(-1)).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("Decrement Success", "${user.name} friend count = ${user.numFriends}")
                } else {
                    Log.d("Decrement Failed", "${user.name} friend count = ${user.numFriends}")
                }
            }

        mFireStore.collection(Constants.USERS).document(friend!!.id)
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


    fun fetchUsersById(activity: FriendsActivity, list: ArrayList<String>) {
        val usersList = arrayListOf<User>()
        for (item in list) {
            mFireStore
                .collection(Constants.USERS)
                .whereEqualTo(Constants.ID, item)
                .get()
                .addOnCompleteListener {
                    for (doc in it.result.documents) {
                        val user = doc.toObject(User::class.java)
                        usersList.add(user!!)
                    }
                    activity.setUpFriendsList(usersList)
                }
        }
    }


    /**
     * ******************************************************************** EVENTS FIRESTORE FUNCTIONS ***************************************************************************************
     */

    fun updateBoardDetails(
        activity: EditEventActivity,
        boardHashMap: HashMap<String, Any>,
        boardDocumentId: String
    ) {
        mFireStore.collection(Constants.BOARDS)
            .document(boardDocumentId)
            .update(boardHashMap)
            .addOnSuccessListener {
                activity.hideProgressDialog()
                Log.d("UpdateEvent: ", "Event Updated successfully")
                Toast.makeText(
                    activity,
                    "You have successfully updated your event",
                    Toast.LENGTH_LONG
                ).show()

            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.d("UpdateEvent:", "Event Update Failed")
            }

    }

    /* This method responsible for retrieving the events list from Firestore
 * A user is assigned an event when they create it or if someone else has assigned them to it,
 * This method gets all the events the user has been assigned to
 */
    fun getBoardsList(activity: MainActivity) {
        //this statement queries the boards collection where the assignedTo array contains the current user id
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get()
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, it.documents.toString())
                val boardsList: ArrayList<Event> = ArrayList()
                //Adds every document queried to the boardsList arraylist
                for (document in it.documents) {
                    //converts all documents queried into a Event object
                    val board = document.toObject(Event::class.java)!!
                    board.documentID = document.id
                    boardsList.add(board)
                }
                //if query is successful, the events are populated into the main activity
                activity.populateBoardsListToUI(boardsList)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "error while getting events list")
            }
    }


    /*
 * This method creates a new event in cloud Firestore
 * A boards collection is created, which generates a single document for each event
 * The event document data is filled using the event parameter
 */
    fun createBoard(activity: CreateBoardActivity, event: Event) {
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(event, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(activity.javaClass.simpleName, "Event created successfully")
                Toast.makeText(activity, "Event created successfully", Toast.LENGTH_SHORT)
                    .show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener {
                Log.e(activity.javaClass.simpleName, "Error while creating event")
            }

    }

    //retrieves the board in Firestore by querying its Document id
    fun getBoardDetails(activity: EventInfoActivity, boardDocumentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(boardDocumentId)
            .get()
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, it.toString())
                //converts the queried board document to a Event object and passes it to the boardDetails()
                activity.boardDetails(it.toObject(Event::class.java)!!)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "error while getting events list")
            }
    }

    fun getAssignedFriendsListDetails(activity: Activity, assignedTo: ArrayList<String>) {
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, it.documents.toString())
                val usersList: ArrayList<User> = ArrayList()
                // Convert all the document snapshots to the object using the user data model class.
                for (doc in it.documents) {

                    val user = doc.toObject(User::class.java)
                    usersList.add(user!!)
                }

                if (activity is AssignFriendsActivity) {
                    activity.setUpFriendsList(usersList)

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

    //retrieves friend details by querying for their email in firestore
    fun getFriendDetails(activity: AssignFriendsActivity, email: String) {
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener {
                if (it.documents.size > 0) {
                    /* Emails are unique
                 * If an email exists in Firestore there can only be one (one email per user)
                 */
                    val user = it.documents[0].toObject(User::class.java)
                    activity.friendDetails(user!!)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No friend found with the entered email")
                }
            }
    }

    fun assignMemberToEvent(activity: AssignFriendsActivity, event: Event, user: User) {
        //hash map of assigned to field in Firestore (Event documents)
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = event.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(event.documentID)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.friendAssignedSuccess(user)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while assigning friend")
            }
    }

    fun deleteEvent(activity: EditEventActivity, event: Event) {
        mFireStore.collection(Constants.BOARDS)
            .document(event.documentID)
            .delete()
            .addOnSuccessListener {
                activity.hideProgressDialog()
                Toast.makeText(activity, "Event deleted successfully", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(activity, "Error deleting event", Toast.LENGTH_LONG).show()
            }
    }

    fun getEventHost(activity: EventInfoActivity, userId: String) {
        mFireStore
            .collection(Constants.USERS)
            .document(userId)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    val user = it.toObject(User::class.java)
                    activity.setHost(user!!)
                }
            }
    }


    /**
     * Upload PostsActivity
     *
     */



    fun getImagePostsList(activity: PostsActivity) {
        mFireStore.collection(Constants.POSTS)
            .whereEqualTo(Constants.ID, getCurrentUserId())
            .addSnapshotListener { snapshot, e ->
                val postList = arrayListOf<Post>()
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    for (dc: DocumentChange in snapshot.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val post = dc.document.toObject(Post::class.java)
                            postList.add(post)
                        }
                    }
                    activity.populatePostListToUI(postList)
                }
            }

    }

    fun getFriendsPosts(activity: ViewFriendProfile, id: String?) {
        mFireStore
            .collection(Constants.POSTS)
            .whereEqualTo(Constants.ID, id)
            .addSnapshotListener { snapshot, e ->
                val postList = arrayListOf<Post>()
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    for (dc: DocumentChange in snapshot.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val post = dc.document.toObject(Post::class.java)
                            postList.add(post)
                        }
                        if (dc.type == DocumentChange.Type.REMOVED) {
                            val post = dc.document.toObject(Post::class.java)
                            postList.remove(post)
                        }
                    }
                    //activity.populatePostListToUI(postList)
                }


            }
    }



    }


