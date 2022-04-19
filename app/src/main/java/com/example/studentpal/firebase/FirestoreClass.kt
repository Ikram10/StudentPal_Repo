package com.example.studentpal.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.studentpal.activities.*
import com.example.studentpal.models.Board
import com.example.studentpal.models.User
import com.example.studentpal.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).update(userHashMap)
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
     * This method registers a new user into the cloud Firestore
     * A Users collection is created, which generates a single document for each registered user
     * The users document data is filled using the userInfo parameter
     * Only one user document per registered will be permitted
     */
    fun registerUser(activity: SignUpActivity, userInfo: User) {
        // SetOptions.merge() ensures only one user account is created in Firestore for each User id
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).set(
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


    //My code: retrieves all users apart from current user from the database
    fun getAllUsers(activity: FindFriends) {
        mFireStore
            .collection(Constants.USERS)
            .whereNotEqualTo("id", getCurrentUserId())
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
                    activity.setUpFriendsList(userList)
                }
            })





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
                val boardsList: ArrayList<Board> = ArrayList()
                //Adds every document queried to the boardsList arraylist
                for (document in it.documents) {
                    //converts all documents queried into a Board object
                    val board = document.toObject(Board::class.java)!!
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
     * This method creates a new board in cloud Firestore
     * A boards collection is created, which generates a single document for each board
     * The board document data is filled using the board parameter
     */
    fun createBoard(activity: CreateBoardActivity, board: Board) {
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(activity.javaClass.simpleName, "Board created successfully")
                Toast.makeText(activity, "Board created successfully", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener {
                Log.e(activity.javaClass.simpleName, "Error while creating board")
            }

    }

    //retrieves the board in Firestore by querying its Document id
    fun getBoardDetails(activity: EventInfoActivity, boardDocumentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(boardDocumentId)
            .get()
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, it.toString())
                //converts the queried board document to a Board object and passes it to the boardDetails()
                activity.boardDetails(it.toObject(Board::class.java)!!)
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

                if (activity is FriendsActivity) {
                    activity.setUpFriendsList(usersList)

                } else
                    if (activity is EditEventActivity)
                        activity.setUpAssignedMembersList(usersList)

            }.addOnFailureListener {
                if (activity is FriendsActivity) {
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName, "error while getting friends list", it)
                } else
                    if (activity is EditEventActivity) {
                        activity.hideProgressDialog()
                        Log.e(activity.javaClass.simpleName, "error while getting friends list", it)
                    }
            }
    }

    //retrieves friend details by querying for their email in firestore
    fun getFriendDetails(activity: FriendsActivity, email: String) {
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

    fun assignMemberToEvent(activity: FriendsActivity, board: Board, user: User) {
        //hash map of assigned to field in Firestore (Event documents)
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentID)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.friendAssignedSuccess(user)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while assigning friend")
            }
    }

    fun deleteEvent(activity: EditEventActivity, board: Board) {
        mFireStore.collection(Constants.BOARDS)
            .document(board.documentID)
            .delete()
            .addOnSuccessListener {
                activity.hideProgressDialog()
                Toast.makeText(activity, "Event deleted successfully", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Toast.makeText(activity, "Error deleting event", Toast.LENGTH_LONG).show()
            }
    }


}