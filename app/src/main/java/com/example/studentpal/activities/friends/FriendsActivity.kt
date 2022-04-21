package com.example.studentpal.activities.friends

import android.os.Bundle
import com.example.studentpal.R
import com.example.studentpal.activities.BaseActivity
import com.example.studentpal.databinding.ActivityFriendsBinding

class FriendsActivity : BaseActivity() {
    var binding : ActivityFriendsBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setupActionBar()
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