package com.example.studentpal.model.remote

import android.util.Log
import android.widget.Toast
import com.example.studentpal.common.Constants
import com.example.studentpal.model.entities.Event
import com.example.studentpal.model.entities.User
import com.example.studentpal.view.events.AssignFriendsActivity
import com.example.studentpal.view.events.CreateEventActivity
import com.example.studentpal.view.events.EditEventActivity
import com.example.studentpal.view.events.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
/**
 * This object contains all the functionalities responsible for retrieving and storing data
 * in the Events collection in Firestore
 *
 * The author implemented an object to create a singleton of the database, that can be accessed
 * throughout the code
 *
 * The code in this object has been adapted from Denis Panjuta's trello Clone (see references)
 * @see com.example.studentpal.common.References
 */
object EventDatabase {
    private const val TAG = "EventsDatabase"
    // Reference to events collection
    private val db = FirebaseFirestore.getInstance().collection(Constants.EVENTS)

    /**
     * Method responsible for assigning a user to an event
     *
     * @param event required to query the specific event document from firestore
     * @param user the user that is being assigned to the event
     */
    fun assignMemberToEvent(
        activity: AssignFriendsActivity,
        event: Event, user: User) {
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

    /**
     * method deletes a specific event from firestore
     *
     * @param event required to retrieve the specific event's document
     */
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

    /**
     * method deletes all the users events created from firestore
     *
     * @param userId required to retrieve all the users events created
     */
    fun deleteUserEventsData(userId: String) {
        db
            .whereEqualTo(Constants.CREATOR_ID, userId)
            .get()
            .addOnSuccessListener { snapshot ->
                // if event documents found, delete them all
                    if (snapshot.size() > 0){
                        for (doc in snapshot.documents){
                            doc.reference.delete().addOnCompleteListener {
                                Log.d(TAG, "${doc.data.toString()} deleted")
                            }.addOnFailureListener{
                                Log.d(TAG, "${doc.data.toString()} failed to delete", it.cause)
                            }

                        }

                    }
                }.addOnFailureListener {
                Log.d(TAG, "Event Documents failed to delete", it.cause)
            }
            }

    /**
     * Method responsible for updating the event details in the Firestore
     *
     * @param eventHashMap stores all the event information changes
     * @param eventDocumentId required to retrieve the specific event document
     */
    fun updateEventDetails(
        activity: EditEventActivity,
        eventHashMap: HashMap<String, Any>,
        eventDocumentId: String
    ) {
       db
            .document(eventDocumentId)
            .update(eventHashMap) // Modifies the event details
            .addOnSuccessListener {
                Log.d("UpdateEvent: ", "Event Updated successfully")
                Toast.makeText(
                    activity,
                    "You have successfully updated your event",
                    Toast.LENGTH_LONG
                ).show()
                activity.eventModifiedSuccessfully()

            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.d("UpdateEvent:", "Event Update Failed")
            }

    }

    /** This method responsible for retrieving the events list from Firestore
     *  A user is assigned an event when they create it or if someone else has assigned them to it,
     *  This method gets all the events the user has been assigned to
     */
    fun getEventsList(activity: MainActivity) {
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
                activity.populateEventsListToUI(boardsList)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "error while getting events list")
            }
    }


    /**
     * This method creates a new event in cloud Firestore
     * A boards collection is created, which generates a single document for each event
     * The event document data is filled using the event parameter
     */
    fun storeEvent(activity: CreateEventActivity, event: Event) {
        db
            .document()
            .set(event, SetOptions.merge()) // merges the document to ensure one document is stored in firestore
            .addOnSuccessListener {
                Log.d(activity.javaClass.simpleName, "Event created successfully")
                Toast.makeText(activity, "Event created successfully", Toast.LENGTH_SHORT)
                    .show()
                activity.eventCreatedSuccessfully()
            }.addOnFailureListener {
                Log.e(activity.javaClass.simpleName, "Error while creating event")
            }

    }


}