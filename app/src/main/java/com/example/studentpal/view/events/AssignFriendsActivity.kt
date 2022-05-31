package com.example.studentpal.view.events

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentpal.R
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.view.adapter.FriendsAssignedAdapter
import com.example.studentpal.databinding.ActivityAssignFriendsBinding
import com.example.studentpal.model.fcm.notification.RetrofitInstance
import com.example.studentpal.model.entities.Event
import com.example.studentpal.model.fcm.notification.NotificationData
import com.example.studentpal.model.fcm.notification.PushNotification
import com.example.studentpal.model.entities.User
import com.example.studentpal.common.Constants
import com.example.studentpal.model.remote.EventDatabase.assignMemberToEvent
import com.example.studentpal.model.remote.UsersDatabase.getAssignedFriendsListDetails
import com.example.studentpal.model.remote.UsersDatabase.getFriendDetails
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
/**
 * This activity is responsible for assigning users to an event
 *
 * The code displayed was adapted from Denis Panjuta's Trello clone (see references file)
 *
 * All code that was created by the author will be labelled [My Code].
 *
 * Reused code that has been adapted by the author is labeled [Adapted ].
 *
 * @see[com.example.studentpal.common.References]
 */
class AssignFriendsActivity : BaseActivity() {
    private var binding: ActivityAssignFriendsBinding? = null
    private lateinit var mEventDetails : Event // events information
    private lateinit var mAssignedFriendsList : ArrayList<User> //array list of users assigned to the event

    /* Notifies the app if any changes were made
     * Purpose is to avoid reloading the callback activity if no changes were made in this activity
     */
    private var anyChangesMade : Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignFriendsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        //retrieves the Event details passed from the main activity
        if (intent.hasExtra(Constants.EVENT_DETAIL)) {
            //initialises the event details
            mEventDetails = intent.getParcelableExtra(Constants.EVENT_DETAIL)!!
            showProgressDialog(resources.getString(R.string.please_wait))
            // retrieves all the users assigned to the event
            getAssignedFriendsListDetails(this, mEventDetails.assignedTo)
        }

        setupActionBar()

    }

    /**
     * method sets up the action bar
     */
    private fun setupActionBar() {
        val toolbar = binding?.toolbarAssignFriendsActivity
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = "Assign Friends: ${mEventDetails.name}"

        }
        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    /**
     * method sets up the recycler view that will display the list of assigned members
     */
    fun setUpAssignedList(list: ArrayList<User>){
        mAssignedFriendsList = list
        hideProgressDialog()

        //Linear layout recycler view
        binding?.rvFriendsActivity?.layoutManager = LinearLayoutManager(this)
        binding?.rvFriendsActivity?.setHasFixedSize(true)
        //initialises the adapter
        val adapter = FriendsAssignedAdapter(this, list)
        binding?.rvFriendsActivity?.adapter = adapter
    }

    /**
     * method assigns user to the events assigned to arraylist
     */
    fun friendDetails(user: User){
        //adds the friends user id to the assigned to array list
        mEventDetails.assignedTo.add(user.id)
        assignMemberToEvent(this,mEventDetails,user)
    }

    /**
     * Adds a add user button to the action bar
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add_friend, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Opens a search user dialog when add user button is selected.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_add_friend -> {
                dialogSearchUser()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * [Adapted ]:Method responsible for displaying a search dialog to search for users
     * The author implemented a username search in the search dialog
     */
    private fun dialogSearchUser(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_friend)
        dialog.findViewById<TextView>(R.id.tv_add).setOnClickListener {
            /* My Code: retrieves the username entered in the search dialog
             * Author implemented a Username search
             */
            val username =
                dialog
                    .findViewById<AppCompatEditText>(R.id.et_search_friend_username)
                    .text.toString().trim {
                //removes spaces when searching for a friend
                it <= ' '
            }
            if (username.isNotEmpty()){
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                    // passes the username searched and adds it to the arraylist if found in Firestore
                getFriendDetails(this, username)
            } else {
                Toast.makeText(this,
                    "Please enter a username",
                    Toast.LENGTH_LONG).show()
            }
        }
        dialog
            .findViewById<TextView>(R.id.tv_cancel)
            .setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    /** Method ensures the user(friend) is assigned to the assigned array list
     *  Only called when a new friend has been assigned to the event
     */
    fun friendAssignedSuccess(user: User){
        hideProgressDialog()
        //Adds the user to the array list
        mAssignedFriendsList.add(user)
        //reloads the activity
        anyChangesMade = true
        setUpAssignedList(mAssignedFriendsList)
        //Sends a notification to the user who has been assigned to event
        NotificationData(
            "Event Invite",
            "Event invite from ${mAssignedFriendsList[0].name}").also {
            sendNotification(PushNotification(it, user.fcmToken))
        }
    }

    /**
     * reloads the activity when user clicks the back button if any changes made in this activity
     */
    override fun onBackPressed() {
        if (anyChangesMade) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }


    /**
     *  Sends notification to firebase server
     */
    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            //network request: Post request
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch(e: Exception) {

            Log.e(TAG, e.toString())

        }
    }

    companion object {
        private const val TAG = "AssignFriendActivity"
    }
}