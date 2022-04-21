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
import com.example.studentpal.adapter.FriendsListItemsAdapter
import com.example.studentpal.databinding.ActivityAssignFriendsBinding
import com.example.studentpal.firebase.FirestoreClass
import com.example.studentpal.models.Board
import com.example.studentpal.models.User
import com.example.studentpal.utils.Constants
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

        val adapter = FriendsListItemsAdapter(this, list)
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

        SendNotificationToUserAsyncTask(mBoardDetails.name, user.fcmToken).execute()
    }

    //reloads the activity when user clicks the back button if any changes made in this activity
    override fun onBackPressed() {
        if (anyChangesMade) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    private inner class SendNotificationToUserAsyncTask (val eventName: String, val token : String)
        : AsyncTask<Any, Void, String> (){

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog(resources.getString(R.string.please_wait))
        }

        override fun doInBackground(vararg params: Any?): String {
            var result : String

            var connection : HttpURLConnection? = null

            try {
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"

                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION, "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                )

                connection.useCaches = false

                val wr = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
                dataObject.put(Constants.FCM_KEY_TITLE,
                    "Event Request: $eventName")
                dataObject.put(Constants.FCM_KEY_MESSAGE,
                    "You have been sent an event request from ${mAssignedFriendsList[0].name}")

                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)

                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()

                val httpResult : Int = connection.responseCode

                if (httpResult == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(
                        InputStreamReader(inputStream)
                    )
                    val sb = StringBuilder()
                    var line: String?
                    try {
                        while (reader.readLine().also { line = it } != null){
                            sb.append(line+"\n")
                        }
                    }catch (e: IOException) {
                        e.printStackTrace()
                    }finally {
                        try {
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    result = sb.toString()
                } else {
                    result = connection.responseMessage
                }
            } catch (e: SocketTimeoutException) {
                result = "Connection Timeout"
            } catch (e : Exception){
                result = "Error: " + e.message
            } finally {
                connection?.disconnect()
            }

            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            hideProgressDialog()
            if (result != null) {
                Log.e("JSON Response Result", result)
            }
        }

    }

}