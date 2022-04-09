package com.example.studentpal.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentpal.R
import com.example.studentpal.adapter.FriendsListItemsAdapter
import com.example.studentpal.databinding.ActivityFriendsBinding
import com.example.studentpal.firebase.FirestoreClass
import com.example.studentpal.models.Board
import com.example.studentpal.models.User
import com.example.studentpal.utils.Constants

class FriendsActivity : BaseActivity() {

    private var binding: ActivityFriendsBinding? = null
    private lateinit var mBoardDetails : Board
    private lateinit var mAssignedFriendsList : ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendsBinding.inflate(layoutInflater)
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
        val toolbar = binding?.toolbarFriendsActivity
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

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
        setUpFriendsList(mAssignedFriendsList)
    }

}