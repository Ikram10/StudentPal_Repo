package com.example.studentpal.messages

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.studentpal.R
import com.example.studentpal.activities.BaseActivity
import com.example.studentpal.databinding.ActivityLatestMessagesBinding
import com.example.studentpal.messages.NewMessageActivity.Companion.USER_KEY
import com.example.studentpal.models.ChatMessage
import com.example.studentpal.models.User
import com.example.studentpal.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

//kotlinMessenger code
class LatestMessagesActivity : BaseActivity() {
    private var binding: ActivityLatestMessagesBinding? = null
    private var toolbar: androidx.appcompat.widget.Toolbar? = null
    private val adapter = GroupAdapter<GroupieViewHolder>()

    val latestMessageMap = HashMap<String, ChatMessage>()


    //creating a global variable so this currentUser field can be accessed anywhere
    companion object {
        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLatestMessagesBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding!!.recyclerViewLatestMessages.adapter = adapter

        //sends user to the chat log with selected latest message
        adapter.setOnItemClickListener{ item, view ->
            Log.d(TAG, "Latest message item clicked")
            val intent = Intent(this, ChatLogActivity::class.java)
            val row = item as LatestMessageRow
            //pass the chat partner as an intent to the chat log activity
            intent.putExtra(USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }

        listenForLatestMessages()

        fetchCurrentUser()

        setupActionBar()
    }


    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref =
            FirebaseDatabase.getInstance("https://studentpal-8f3d3-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("/latest-messages/$fromId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                latestMessageMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessage()

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                latestMessageMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessage()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun refreshRecyclerViewMessage() {
        adapter.clear()
        latestMessageMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

    //This method
    private fun fetchCurrentUser() {
        //retrieves currently signed in users id
        val uid = FirebaseAuth.getInstance().uid
        //A reference to currently signed in user in Firestore
        val ref =
            FirebaseFirestore.getInstance().collection(Constants.USERS).document(uid.toString())

        //listener listens to modifications made to currently signed in users document
        ref.addSnapshotListener(object : EventListener<DocumentSnapshot> {
            override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.w("Firestore Error", "Listen failed.")
                    return
                }
                if (value != null) {
                    currentUser = value.toObject(User::class.java)
                    Log.d("Latest Messages", "Current User ${currentUser?.image}")
                }
            }

        })
    }


    //Denis Panjuta code.
    private fun setupActionBar() {
        toolbar = binding?.toolbarLatestMessages
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = "Latest Messages"

        }
        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_message -> {
                startActivity(Intent(this, NewMessageActivity::class.java))
            }
            R.id.menu_sign_out -> {
                signOutUser()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}