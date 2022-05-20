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
/**
 * This activity is responsible for displaying all the users friends in a grid layout.
 *
 * The users will be able to navigate to a friends profile or chat log with the friend after
 * clicking the friend's profile card.
 *
 * [My Code ]: The entire code in this activity belongs to the author.
 */
class FriendsActivity : BaseActivity() {

    var binding : ActivityFriendsBinding? = null

    private lateinit var viewModel : FriendsViewModel

    private var friendsAdapter: FriendsListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        // initialises the view model
        viewModel = ViewModelProvider(this)[FriendsViewModel::class.java]

        // observes the friend list for any changes made, e.g additions
        viewModel.friendsList.observe(this) {
            /* Populates the UI with the friends list
             * This code is executed whenever the friends list is altered
             */
            setUpFriendsList(it as ArrayList<User>)
        }

        setupActionBar()

    }
    /**
     *  A method to display the user's friends list.
     *
     *  It configures the recycler view, by setting the layout manager and passes the user list
     *  to the adapter.
     *
     * This method is called whenever the friend list changes to display the changes.
     *
     * @param friendsList a list of users that are friends with the current user
     *
     * @see [FriendsViewModel.friendsList]
     */
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