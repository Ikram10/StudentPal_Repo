package com.example.studentpal.model.remote

import android.util.Log
import android.widget.Toast
import com.example.studentpal.common.Constants
import com.example.studentpal.model.entities.Event
import com.example.studentpal.model.entities.User
import com.example.studentpal.view.events.MainActivity
import com.example.studentpal.view.events.AssignFriendsActivity
import com.example.studentpal.view.events.CreateEventActivity
import com.example.studentpal.view.events.EditEventActivity
import com.example.studentpal.view.events.EventInfoActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object EventDatabase {
    val db = FirebaseFirestore.getInstance().collection(Constants.EVENTS)

    fun assignMemberToEvent(activity: AssignFriendsActivity, event: Event, user: User) {
        //hash map of assigned to field in Firestore (Event documents)
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = event.assignedTo

       db
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
        db
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

    fun updateBoardDetails(
        activity: EditEventActivity,
        boardHashMap: HashMap<String, Any>,
        boardDocumentId: String
    ) {
       db
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
       db
            .whereArrayContains(Constants.ASSIGNED_TO, UsersDatabase.getCurrentUserId())
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
    fun storeEvent(activity: CreateEventActivity, event: Event) {
        db
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
        db
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


}