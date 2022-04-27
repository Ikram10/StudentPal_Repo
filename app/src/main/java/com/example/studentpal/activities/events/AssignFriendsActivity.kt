package com.example.studentpal.activities.events

import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentpal.R
import com.example.studentpal.activities.BaseActivity
import com.example.studentpal.activities.messages.ChatLogActivity
import com.example.studentpal.adapter.FriendsAssignedAdapter
import com.example.studentpal.databinding.ActivityAssignFriendsBinding
import com.example.studentpal.fcm.RetrofitInstance
import com.example.studentpal.firebase.FirestoreClass
import com.example.studentpal.models.Board
import com.example.studentpal.models.NotificationData
import com.example.studentpal.models.PushNotification
import com.example.studentpal.models.User
import com.example.studentpal.utils.Constants
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class AssignFriendsActivity : BaseActivity() {

    private var binding: ActivityAssignFriendsBinding? = null
    private lateinit var mBoardDetails : Board
    private lateinit var mAssignedFriendsList : ArrayList<User>

    /* Notifies the app if any changes were made
     * Purpose is to avoid reloading the callback activity if no changes were made in this activity
     */
    private var anyChangesMade : Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignFriendsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        //retrieves the Event details passed from the main activity
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getAssignedFriendsListDetails(this, mBoardDetails.assignedTo)
        }

        setupActionBar()

    }

    private fun setupActionBar() {
        val toolbar = binding?.toolbarAssignFriendsActivity
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = "Assign Friends: ${mBoardDetails.name}"

        }
        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setUpFriendsList(list: ArrayList<User>){
        mAssignedFriendsList = list
        hideProgressDialog()

        binding?.rvFriendsActivity?.layoutManager = LinearLayoutManager(this)
        binding?.rvFriendsActivity?.setHasFixedSize(true)

        val adapter = FriendsAssignedAdapter(this, list)
        binding?.rvFriendsActivity?.adapter = adapter
    }

    fun friendDetails(user: User){
        //adds the friends user id to the assigned to array list
        mBoardDetails.assignedTo.add(user.id)
        FirestoreClass().assignMemberToEvent(this,mBoardDetails,user)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add_friend, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_add_friend -> {
                dialogSearchFriend()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchFriend(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_friend)
        dialog.findViewById<TextView>(R.id.tv_add).setOnClickListener {

            val email = dialog.findViewById<AppCompatEditText>(R.id.et_search_friend_email).text.toString().trim {
                //removes spaces when searching for a friend
                it <= ' '
            }

            if (email.isNotEmpty()){
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getFriendDetails(this, email)
            } else {
                Toast.makeText(this, "Please enter friends email address", Toast.LENGTH_LONG).show()
            }
        }
        dialog.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    /* ensures the user(friend) is assigned to the assigned array list
     * Only called when a new friend has been assigned to the event
     */
    fun friendAssignedSuccess(user: User){
        hideProgressDialog()
        mAssignedFriendsList
        mAssignedFriendsList.add(user)
        //reloads the activity
        anyChangesMade = true
        setUpFriendsList(mAssignedFriendsList)
        
        val notification = NotificationData(
            "Event Invite",
            "You have received an Event invite from ${mAssignedFriendsList[0].name}").also {
            sendNotification(PushNotification(it, user.fcmToken))
        }


    }

    //reloads the activity when user clicks the back button if any changes made in this activity
    override fun onBackPressed() {
        if (anyChangesMade) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }


    // Sends notification to firebase server
    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {

            //network request: Post request
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d(ChatLogActivity.TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.e(ChatLogActivity.TAG, response.errorBody().toString())
            }
        } catch(e: Exception) {

            Log.e(ChatLogActivity.TAG, e.toString())

        }
    }

}