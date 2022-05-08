package com.example.studentpal.view.friends

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentpal.R
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.view.adapter.UsersAdapter
import com.example.studentpal.databinding.ActivityFindFriendsBinding
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.remote.UsersDatabase.getAllUsers

class FindFriends : BaseActivity() {
    var binding : ActivityFindFriendsBinding? = null
    private var userList : ArrayList<User>? = null
    private var usersAdapter: UsersAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindFriendsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        setupActionBar()

        getAllUsers(this)
    }


    fun setUpUsersList(list: ArrayList<User>){
        userList = list
        hideProgressDialog()

        binding?.rvFindFriends?.layoutManager = LinearLayoutManager(this)
        binding?.rvFindFriends?.setHasFixedSize(true)

        usersAdapter = UsersAdapter(this, list)
        binding?.rvFindFriends?.adapter = usersAdapter
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val item : MenuItem = menu.findItem(R.id.action_search)
        val searchView: SearchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                usersAdapter?.filter?.filter(newText)
                return false
            }

        })

        return super.onCreateOptionsMenu(menu)
    }


    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarFindFriends)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = "Find Friends"
        }
        binding?.toolbarFindFriends?.setNavigationOnClickListener{
            onBackPressed()
        }

    }
}