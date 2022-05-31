package com.example.studentpal.view.messages

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.studentpal.R
import com.example.studentpal.common.Constants
import com.example.studentpal.databinding.ActivityLatestMessagesBinding
import com.example.studentpal.model.entities.ChatMessage
import com.example.studentpal.model.entities.User
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.view.friends.FriendsActivity
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

/**
 * This activity is responsible for displaying each latest message item. Implements the
 * GroupieViewHolder to simplify adapter implementation.
 *
 * The code adapted from Brian Voong's "Kotlin Firebase Messenger" Tutorial (see references file)
 * However, the author  evolved the code produced by Voong to accommodate the Server implementation.
 * For instance, the tutorial implemented the Realtime database as a database solution to store messages, but StudentPal
 * used Cloud Firestore.
 *
 * All code that was adapted by the author will be labelled [My Code].
 *
 * @see[com.example.studentpal.common.References]
 */
class LatestMessagesActivity : BaseActivity() {
    private var binding: ActivityLatestMessagesBinding? = null
    private var toolbar: androidx.appcompat.widget.Toolbar? = null
    private val adapter = GroupAdapter<GroupieViewHolder>()

    //hashmap to store the latest messages
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

        // sends user to the chat log with the selected latest message
        adapter.setOnItemClickListener{ item, _ ->
            Log.d(TAG, "Latest message item clicked")
            val intent = Intent(this, ChatLogActivity::class.java)
            val row = item as LatestMessageRow
            //pass the chat partner as an intent to the chat log activity
            intent.putExtra(Constants.USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }

        listenForLatestMessages()

        setupActionBar()
    }

    /**
     * Method listens for new messages being sent and adds them or updates the latest messages recycler view.
     * Author reused the code implemented by Voong (Kotlin Firebase Messenger)
     *
     * @see[com.example.studentpal.common.References]
     */
    private fun listenForLatestMessages() {
        // current user id
        val fromId = getCurrentUserID()
        // latest-message database reference
        val ref =
            FirebaseDatabase.getInstance(
                "https://studentpal-8f3d3-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("/latest-messages/$fromId")

        ref.addChildEventListener(object : ChildEventListener {
            //listens for new messages added
            override fun onChildAdded(snapshot: DataSnapshot,
                                      previousChildName: String?) {
                val chatMessage = snapshot
                    .getValue(ChatMessage::class.java) ?: return
                latestMessageMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessage()

            }
            //listens for changes made to messages in the database
            override fun onChildChanged(
                snapshot: DataSnapshot,
                previousChildName: String?) {
                val chatMessage = snapshot
                    .getValue(ChatMessage::class.java) ?: return
                latestMessageMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessage()
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    /**
     * method refreshes the recyclerview to update the latest messages displayed
     */
    private fun refreshRecyclerViewMessage() {
        adapter.clear()
        // Adds every item in the hashmap to the recyclerview
        latestMessageMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }


    /** Sets the the action bar.
     *
     *  Denis Panjuta code (Denis Panjuta)
     *
     *  @see[com.example.studentpal.common.References]
     */
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_message -> {
                startActivity(Intent(this, FriendsActivity::class.java))
            }

        }
        return super.onOptionsItemSelected(item)
    }
}