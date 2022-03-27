package com.example.studentpal.messages

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.activities.BaseActivity
import com.example.studentpal.adapter.UserAdapter
import com.example.studentpal.databinding.ActivityNewMessageBinding
import com.example.studentpal.models.User
import com.example.studentpal.utils.Constants
import com.google.firebase.firestore.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem

class NewMessageActivity : BaseActivity() {
    private var binding: ActivityNewMessageBinding? = null

    private var actionBar: ActionBar? = null
    private var toolbar: androidx.appcompat.widget.Toolbar? = null

    private var db: FirebaseFirestore? = null
    private var recyclerView: RecyclerView? = null
    var users: ArrayList<User>? = null

    var user: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMessageBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        recyclerView = binding?.recyclerViewNewMessage
        recyclerView?.layoutManager = GridLayoutManager(this, 2)
        recyclerView?.setHasFixedSize(true)


        val adapter = GroupAdapter<GroupieViewHolder>()
        binding!!.recyclerViewNewMessage.adapter = adapter

        users = arrayListOf()

        fetchUsers()
        
        listenForMessage()




    }

    private fun listenForMessage() {
        val reference = FirebaseFirestore.getInstance().collection(Constants.MESSAGES)
        
        reference.addSnapshotListener { value, error ->  }
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

    //Companion object enables the users name to be passed to another activity. The other activity needs to pass the USER_KEY
    companion object {
        const val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers() {
        //my code: Chose to integrate Firestore instead of Realtime database
        db = FirebaseFirestore.getInstance()

        //Retrieves and adds Snapshot listener to all users except currently logged in user
        db!!.collection(Constants.USERS).whereNotEqualTo("id", getCurrentUserID())
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    val adapter = GroupAdapter<GroupieViewHolder>()
                    if (error != null) {
                        Log.w("Firestore Error", "Listen failed.")
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            user = dc.document.toObject(User::class.java)
                            adapter.add(UserItem(user!!))
                        }
                    }

                        adapter.setOnItemClickListener { item, view ->

                            val userItem = item as UserItem

                            val intent = Intent(this@NewMessageActivity, ChatLogActivity::class.java)
                            //intent passes the users name to the chat log activity
                            intent.putExtra(USER_KEY, userItem.user)
                            startActivity(intent)
                        }
                        recyclerView?.adapter = adapter

                    }
            })
    }

    class UserItem (val user: User): Item<GroupieViewHolder>(){
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.findViewById<TextView>(R.id.cv_username).text = user.name

            viewHolder.itemView.findViewById<TextView>(R.id.cv_email).text = user.email
            viewHolder.itemView.findViewById<TextView>(R.id.civ_status).text = user.status

            Glide
                .with(viewHolder.root)
                .load(user.image)
                .placeholder(R.drawable.ic_user_place_holder)
                .into(viewHolder.itemView.findViewById(R.id.iv_profile_image))

            //My code
            when(user.status) {
                "Available" -> {
                    viewHolder.itemView.findViewById<TextView>(R.id.civ_status).setTextColor(ContextCompat.getColor(viewHolder.root.context, R.color.available))
                }
                "Unavailable" -> {
                    viewHolder.itemView.findViewById<TextView>(R.id.civ_status).setTextColor(ContextCompat.getColor(viewHolder.root.context, R.color.unavailable))
                }
            }
        }

        override fun getLayout(): Int {
            return R.layout.item_profile
        }



    }


}
