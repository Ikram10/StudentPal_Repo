package com.example.studentpal.messages

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.app.ActionBar
import androidx.core.view.get
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentpal.R
import com.example.studentpal.activities.BaseActivity
import com.example.studentpal.adapter.UserAdapter
import com.example.studentpal.databinding.ActivityNewMessageBinding
import com.example.studentpal.models.User
import com.example.studentpal.utils.Constants
import com.google.firebase.firestore.*

class NewMessageActivity : BaseActivity() {
    private var binding: ActivityNewMessageBinding? = null

    private var actionBar: ActionBar? = null
    private var toolbar: androidx.appcompat.widget.Toolbar? = null

    private var db: FirebaseFirestore? = null
    private var recyclerView: RecyclerView? = null
    var users: ArrayList<User>? = null
    var usersAdapter: UserAdapter? = null
    var user: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMessageBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        recyclerView = binding?.recyclerViewNewMessage
        recyclerView?.layoutManager = GridLayoutManager(this, 2)
        recyclerView?.setHasFixedSize(true)




        users = arrayListOf()
        usersAdapter = UserAdapter(this, users!!)

        recyclerView?.adapter = usersAdapter

        //  setupActionBar()
        eventChangeListener()


    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val menuItem: MenuItem? = menu?.findItem(R.id.action_search)

        val searchView = menuItem?.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = "Search Username or Email"
        actionBar = supportActionBar
        actionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.logo_text_color)))
        val queryListener: SearchView.OnQueryTextListener

        return super.onCreateOptionsMenu(menu)
    }


    private fun eventChangeListener() {
        //my code: Chose to integrate Firestore instead of Realtime database
        db = FirebaseFirestore.getInstance()

        //Retrieves and adds Snapshot listener to all users except currently logged in user
        db!!.collection(Constants.USERS).whereNotEqualTo("id", getCurrentUserID())
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.w("Firestore Error", "Listen failed.")
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            users?.add(dc.document.toObject(User::class.java))
                        }
                    }
                    usersAdapter?.notifyDataSetChanged()
                }
            })
    }


//    private fun setupActionBar() {
//        toolbar = binding?.toolbarNewMessage
//        setSupportActionBar(toolbar)
//
//        val actionBar = supportActionBar
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true)
//            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
//            actionBar.title = "Select User"
//        }
//        toolbar?.setNavigationOnClickListener {
//            onBackPressed()
//        }
//    }

}
