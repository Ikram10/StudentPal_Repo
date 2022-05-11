package com.example.studentpal.view.friends

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentpal.R
import com.example.studentpal.databinding.ActivityRequestsBinding
import com.example.studentpal.model.entities.User
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.view.adapter.RequestsAdapter
import com.example.studentpal.viewmodel.RequestsViewModel

class RequestsActivity : BaseActivity() {
    private var binding: ActivityRequestsBinding? = null
    // ViewModel
    private var viewModel: RequestsViewModel? = null
    private var requestsAdapter: RequestsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        viewModel = ViewModelProvider(this)[RequestsViewModel::class.java]
        viewModel!!.requestList.observe(this) {
            setUpRequestList(it)
        }

        setupActionBar()
    }

    private fun setUpRequestList(list: List<User>) {
        hideProgressDialog()
        binding?.rvRequests?.layoutManager = LinearLayoutManager(this)
        binding?.rvRequests?.setHasFixedSize(true)
        requestsAdapter =
            RequestsAdapter(this, list as ArrayList<User>)
        binding?.rvRequests?.adapter = requestsAdapter

    }


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