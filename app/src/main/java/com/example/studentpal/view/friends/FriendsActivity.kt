package com.example.studentpal.view.friends

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.studentpal.R
import com.example.studentpal.databinding.ActivityFriendsBinding
import com.example.studentpal.model.entities.User
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.view.adapter.FriendsListAdapter
import com.example.studentpal.viewmodel.FriendsViewModel

class FriendsActivity : BaseActivity() {
    var binding : ActivityFriendsBinding? = null

    private lateinit var viewModel : FriendsViewModel

    private var friendsAdapter: FriendsListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        viewModel = ViewModelProvider(this)[FriendsViewModel::class.java]
        viewModel.friendsList.observe(this) {
            // Populates the UI with the posts list
            setUpFriendsList(it as ArrayList<User>)
        }
        setupActionBar()

    }
    private fun setUpFriendsList(friendsList: ArrayList<User>){
        binding?.rvFriends?.layoutManager = GridLayoutManager(this, 2)
        binding?.rvFriends?.setHasFixedSize(true)

        friendsAdapter = FriendsListAdapter(this, friendsList)
        binding?.rvFriends?.adapter = friendsAdapter
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