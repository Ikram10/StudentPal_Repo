package com.example.studentpal.view.friends

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentpal.R
import com.example.studentpal.databinding.ActivityRequestsBinding
import com.example.studentpal.model.entities.User
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.view.adapter.RequestsAdapter
import com.example.studentpal.viewmodel.PostsViewModel
import com.example.studentpal.viewmodel.RequestsViewModel
/**
 * This activity is responsible for displaying friend requests a user receives.
 *
 * It enables users accept or reject a friend request and implements the MVVM design pattern
 *
 * The entire code in this activity belongs to the author.
 */
class RequestsActivity : BaseActivity() {

    private var binding: ActivityRequestsBinding? = null
    // ViewModel
    private var viewModel: RequestsViewModel? = null

    private var requestsAdapter: RequestsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        //Initialises view model
        viewModel = ViewModelProvider(this)[RequestsViewModel::class.java]

        // observes changes made to the list of users that have sent a friend request
        viewModel!!.requestList.observe(this) {
            // Populates the recycler view containing the list of friend request
            setUpRequestList(it)
        }

        setupActionBar()
    }

    /**
     *  A method to display all posts uploaded by the user.
     *  Its configures the recycler view, by setting the layout manager and passes the user list
     *  to the adapter.
     *
     * This method is called whenever the friend request list changes.
     *
     * @param list a list of users who have sent a friend request to current user
     *
     * @see [RequestsViewModel.requestList]
     */
    private fun setUpRequestList(list: List<User>) {
        hideProgressDialog()
        binding?.rvRequests?.layoutManager = LinearLayoutManager(this)
        binding?.rvRequests?.setHasFixedSize(true)
        requestsAdapter =
            RequestsAdapter(this, list as ArrayList<User>)
        binding?.rvRequests?.adapter = requestsAdapter
    }

    /**
     * This code has been reused from Denis Panjuta's trello clone
     *
     * @see com.example.studentpal.common.References
     */
    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarRequests)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = "Friend Requests"
        }
        binding?.toolbarRequests?.setNavigationOnClickListener {
            onBackPressed()
        }

    }
}