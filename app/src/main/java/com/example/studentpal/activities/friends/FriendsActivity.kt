package com.example.studentpal.activities.friends

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentpal.R
import com.example.studentpal.activities.BaseActivity
import com.example.studentpal.adapter.FriendsListAdapter
import com.example.studentpal.adapter.UsersAdapter
import com.example.studentpal.databinding.ActivityFriendsBinding
import com.example.studentpal.firebase.FirestoreClass
import com.example.studentpal.models.User

class FriendsActivity : BaseActivity() {
    var binding : ActivityFriendsBinding? = null
    private var friendsIDList : ArrayList<String>? = null
    private var friendsList : ArrayList<User>? = null
   private var friendsAdapter: FriendsListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setupActionBar()

        FirestoreClass().getFriendsList(this)
    }
    fun setUpFriendsList(list: ArrayList<User>){
        friendsList = list
        binding?.rvFriends?.layoutManager = GridLayoutManager(this, 2)
        binding?.rvFriends?.setHasFixedSize(true)

        friendsAdapter = FriendsListAdapter(this, friendsList!!)
        binding?.rvFriends?.adapter = friendsAdapter
    }

    fun setUpUsersListFromId(list: ArrayList<String>){
        friendsIDList = list
        hideProgressDialog()

        FirestoreClass().fetchUsersById(this,friendsIDList!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarFriends)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = "Friends"
        }
        binding?.toolbarFriends?.setNavigationOnClickListener{
            onBackPressed()
        }

    }
}